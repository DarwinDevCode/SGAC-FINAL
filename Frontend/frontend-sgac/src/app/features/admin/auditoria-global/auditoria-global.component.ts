import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { 
  AdminReporteService, 
  AuditoriaGlobalDTO 
} from '../../../core/services/admin-reporte-service';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { FacultadDTO } from '../../../core/dto/facultad';
import { CarreraDTO } from '../../../core/dto/carrera';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-auditoria-global',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './auditoria-global.component.html',
  styleUrls: ['./auditoria-global.component.css']
})
export class AuditoriaGlobalComponent implements OnInit {
  private adminService = inject(AdminReporteService);
  private catalogosService = inject(CatalogosService);

  // Datos
  auditLogs: AuditoriaGlobalDTO[] = [];
  filteredLogs: AuditoriaGlobalDTO[] = [];
  loading = false;

  // Listas para filtros
  listas = {
    facultades: [] as FacultadDTO[],
    carreras: [] as CarreraDTO[],
    roles: ['ADMINISTRADOR', 'DECANO', 'COORDINADOR', 'DOCENTE', 'ESTUDIANTE', 'AYUDANTE_CATEDRA'],
    modulos: [] as string[]
  };

  carrerasFiltradas: CarreraDTO[] = [];

  // Filtros activos
  filtros = {
    facultad: '',
    carrera: '',
    rol: '',
    modulo: '',
    busqueda: ''
  };

  ngOnInit() {
    this.cargarCatalogos();
    this.cargarDatos();
  }

  cargarCatalogos() {
    this.catalogosService.getFacultades().subscribe(res => this.listas.facultades = res);
    this.catalogosService.getCarreras().subscribe(res => this.listas.carreras = res);
  }

  cargarDatos() {
    this.loading = true;
    this.adminService.getAuditoria().subscribe({
      next: (data) => {
        this.auditLogs = data;
        // Extraer módulos únicos de los datos recibidos
        const modulosSet = new Set(data.map(l => l.modulo).filter(m => m));
        this.listas.modulos = Array.from(modulosSet).sort();
        
        this.aplicarFiltros();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar logs de auditoría:', err);
        this.loading = false;
      }
    });
  }

  onFacultadChange() {
    if (this.filtros.facultad) {
      const fac = this.listas.facultades.find(f => f.nombreFacultad === this.filtros.facultad);
      if (fac) {
        this.carrerasFiltradas = this.listas.carreras.filter(c => c.idFacultad === fac.idFacultad);
      } else {
        this.carrerasFiltradas = [];
      }
    } else {
      this.carrerasFiltradas = [];
      this.filtros.carrera = '';
    }
    this.aplicarFiltros();
  }

  aplicarFiltros() {
    this.filteredLogs = this.auditLogs.filter(log => {
      const cumpleFacultad = !this.filtros.facultad || log.facultad === this.filtros.facultad;
      const cumpleCarrera = !this.filtros.carrera || log.carrera === this.filtros.carrera;
      const cumpleRol = !this.filtros.rol || (log.roles && log.roles.includes(this.filtros.rol));
      const cumpleModulo = !this.filtros.modulo || log.modulo === this.filtros.modulo;
      
      const texto = (log.usuario + ' ' + log.accion + ' ' + log.detalle).toLowerCase();
      const cumpleBusqueda = !this.filtros.busqueda || texto.includes(this.filtros.busqueda.toLowerCase());

      return cumpleFacultad && cumpleCarrera && cumpleRol && cumpleModulo && cumpleBusqueda;
    });
  }

  exportarPDF() {
    const doc = new jsPDF('l', 'mm', 'a4');
    const head = [['FECHA', 'USUARIO', 'ROLES', 'FACULTAD', 'CARRERA', 'ACCIÓN', 'MÓDULO', 'DETALLE']];
    
    const data = this.filteredLogs.map(l => [
      new Date(l.fecha).toLocaleString(),
      l.usuario,
      l.roles,
      l.facultad,
      l.carrera,
      l.accion,
      l.modulo,
      l.detalle
    ]);

    doc.setFontSize(18);
    doc.text('Reporte de Auditoría Global - SGAC', 14, 15);
    doc.setFontSize(10);
    doc.text(`Generado el: ${new Date().toLocaleString()}`, 14, 22);
    doc.text(`Filtros: ${this.filtros.facultad || 'Todas'}, ${this.filtros.rol || 'Todos'}`, 14, 28);

    autoTable(doc, {
      head: head,
      body: data,
      startY: 35,
      styles: { fontSize: 7 },
      headStyles: { fillColor: [16, 185, 129] }
    });

    doc.save(`auditoria_global_${new Date().getTime()}.pdf`);
  }
}
