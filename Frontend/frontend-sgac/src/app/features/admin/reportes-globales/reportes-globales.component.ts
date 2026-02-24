import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { 
  AdminReporteService, 
  UsuarioGlobalDTO, 
  PersonalGlobalDTO, 
  PostulanteGlobalDTO, 
  AyudanteGlobalDTO 
} from '../../../core/services/admin-reporte-service';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { AuthService } from '../../../core/services/auth-service';
import { FacultadDTO } from '../../../core/dto/facultad';
import { CarreraDTO } from '../../../core/dto/carrera';
import { PeriodoAcademicoDTO } from '../../../core/dto/periodo-academico';
import { Observable } from 'rxjs';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

export type ReportType = 'USUARIOS' | 'PERSONAL' | 'POSTULANTES' | 'AYUDANTES';

@Component({
  selector: 'app-reportes-globales',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './reportes-globales.html',
  styleUrls: ['./reportes-globales.css']
})
export class ReportesGlobalesComponent implements OnInit {
  // Services
  private adminReporteService = inject(AdminReporteService);
  private catalogosService    = inject(CatalogosService);
  private authService         = inject(AuthService);

  // Component State
  tipoReporte: ReportType = 'USUARIOS';
  loading = false;
  
  // Data
  reportData: any[] = [];
  filteredData: any[] = [];

  // Filters
  filtros = {
    rol: '',
    estado: '',
    facultad: 0,
    carrera: 0,
    tipoPersonal: '',
    asignatura: '',
    periodo: ''
  };

  // Catalogs
  listas = {
    roles: ['ADMINISTRADOR', 'DECANO', 'COORDINADOR', 'DOCENTE', 'ESTUDIANTE', 'AYUDANTE_CATEDRA'],
    facultades: [] as FacultadDTO[],
    carreras: [] as CarreraDTO[],
    periodos: [] as PeriodoAcademicoDTO[]
  };

  carrerasFiltradas: CarreraDTO[] = [];

  ngOnInit() {
    this.cargarCatalogos();
    this.cargarDatos();
  }

  cargarCatalogos() {
    this.catalogosService.getFacultades().subscribe((data: any[]) => this.listas.facultades = data);
    this.catalogosService.getCarreras().subscribe((data: any[]) => this.listas.carreras = data);
    this.catalogosService.getPeriodos().subscribe((data: any[]) => this.listas.periodos = data);
  }

  cargarDatos() {
    this.loading = true;
    let obs$: Observable<any[]>;

    switch (this.tipoReporte) {
      case 'USUARIOS':    obs$ = this.adminReporteService.getUsuarios(); break;
      case 'PERSONAL':    obs$ = this.adminReporteService.getPersonal(); break;
      case 'POSTULANTES': obs$ = this.adminReporteService.getPostulantes(); break;
      case 'AYUDANTES':   obs$ = this.adminReporteService.getAyudantes(); break;
    }

    obs$!.subscribe({
      next: (data: any[]) => {
        this.reportData = data;
        this.aplicarFiltros();
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error al cargar datos de reporte:', err);
        this.loading = false;
      }
    });
  }

  onTipoReporteChange() {
    this.resetFiltros();
    this.cargarDatos();
  }

  onFacultadChange() {
    if (this.filtros.facultad === 0) {
      this.carrerasFiltradas = [];
      this.filtros.carrera = 0;
    } else {
      this.carrerasFiltradas = this.listas.carreras.filter(c => c.idFacultad === Number(this.filtros.facultad));
      this.filtros.carrera = 0;
    }
    this.aplicarFiltros();
  }

  resetFiltros() {
    this.filtros = {
      rol: '',
      estado: '',
      facultad: 0,
      carrera: 0,
      tipoPersonal: '',
      asignatura: '',
      periodo: ''
    };
    this.carrerasFiltradas = [];
  }

  aplicarFiltros() {
    const f = this.filtros;
    
    this.filteredData = this.reportData.filter(item => {
      switch (this.tipoReporte) {
        case 'USUARIOS': {
          const u = item as UsuarioGlobalDTO;
          const matchRol = !f.rol || u.roles.includes(f.rol);
          const matchEstado = !f.estado || u.estado.toLowerCase() === f.estado.toLowerCase();
          return matchRol && matchEstado;
        }
        case 'PERSONAL': {
          const p = item as PersonalGlobalDTO;
          const matchTipo = !f.tipoPersonal || p.cargoContexto.includes(f.tipoPersonal);
          // El filtrado por Facultad/Carrera es un poco más complejo si viene en un solo string
          // pero asumimos que el usuario puede buscar por texto o que personalData tiene IDs (mejorado en SQL)
          // Por ahora filtramos por el nombre de la facultad si está en cargoContexto
          let matchFac = true;
          if (f.facultad > 0) {
            const facNombre = this.listas.facultades.find(fac => fac.idFacultad === Number(f.facultad))?.nombreFacultad;
            matchFac = !facNombre || p.cargoContexto.toLowerCase().includes(facNombre.toLowerCase());
          }
          return matchTipo && matchFac;
        }
        case 'POSTULANTES': {
          const post = item as PostulanteGlobalDTO;
          const matchAsig = !f.asignatura || post.asignatura.toLowerCase().includes(f.asignatura.toLowerCase());
          const matchPer  = !f.periodo || post.periodo === f.periodo;
          return matchAsig && matchPer;
        }
        case 'AYUDANTES': {
          const a = item as AyudanteGlobalDTO;
          const matchFac = true; // Similar a personal si no tenemos el ID
          const matchEst = !f.estado || a.estado === f.estado;
          return matchFac && matchEst;
        }
        default: return true;
      }
    });
  }

  exportar(formato: 'PDF' | 'EXCEL') {
    const title = `Reporte_${this.tipoReporte}_${new Date().toLocaleDateString()}`;
    
    if (formato === 'PDF') {
      this.exportToPDF(title);
    } else {
      this.exportToExcel(title);
    }
  }

  private exportToPDF(filename: string) {
    const doc = new jsPDF('l', 'mm', 'a4');
    doc.setFontSize(18);
    doc.text(`Sistema SGAC - ${this.tipoReporte}`, 14, 20);
    doc.setFontSize(10);
    doc.text(`Fecha de generación: ${new Date().toLocaleString()}`, 14, 28);

    let head: string[][] = [];
    let body: any[][] = [];

    switch (this.tipoReporte) {
      case 'USUARIOS':
        head = [['Usuario', 'Email', 'Roles', 'Estado']];
        body = this.filteredData.map(u => [u.usuario, u.email, u.roles, u.estado]);
        break;
      case 'PERSONAL':
        head = [['Nombre', 'Cargo / Contexto', 'Estado']];
        body = this.filteredData.map(p => [p.nombre, p.cargoContexto, p.estado]);
        break;
      case 'POSTULANTES':
        head = [['Estudiante', 'Cédula', 'Asignatura', 'Periodo', 'Estado']];
        body = this.filteredData.map(p => [p.estudiante, p.cedula, p.asignatura, p.periodo, p.estado]);
        break;
      case 'AYUDANTES':
        head = [['Estudiante', 'Asignatura', 'Docente', 'Horas', 'Estado']];
        body = this.filteredData.map(a => [a.estudiante, a.asignatura, a.docente, a.horas + 'h', a.estado]);
        break;
    }

    autoTable(doc, {
      startY: 35,
      head: head,
      body: body,
      theme: 'striped',
      headStyles: { fillColor: [16, 185, 129] }
    });

    doc.save(`${filename}.pdf`);
  }

  private exportToExcel(filename: string) {
    const ws = XLSX.utils.json_to_sheet(this.filteredData);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Reporte');
    XLSX.writeFile(wb, `${filename}.xlsx`);
  }
}
