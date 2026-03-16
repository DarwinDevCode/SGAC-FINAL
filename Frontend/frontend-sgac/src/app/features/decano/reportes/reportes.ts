import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { DecanoService } from '../../../core/services/decano-service';
import { AuthService } from '../../../core/services/auth-service';
import { 
  DecanoReporteCarreraDTO, 
  DecanoReporteCoordinadorDTO, 
  ConvocatoriaReporteDTO 
} from '../../../core/dto/decano';
import { AsignaturaDTO } from '../../../core/dto/asignatura';
import { CoordinadorPostulanteReporteDTO } from '../../../core/dto/coordinador';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-reportes-decano',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './reportes.html',
  styleUrls: ['./reportes.css']
})
export class ReportesDecanoComponent implements OnInit {
  private decanoService = inject(DecanoService);
  private authService = inject(AuthService);

  // Estado
  loading = false;
  errorMensaje = '';
  idFacultad?: number;

  // Selección de reporte
  tipoReporte: 'carreras' | 'asignaturas' | 'convocatorias' | 'postulantes' | 'coordinadores' = 'carreras';

  // Datos
  reporteCarrerasData: DecanoReporteCarreraDTO[] = [];
  reporteAsignaturasData: AsignaturaDTO[] = [];
  reporteConvocatoriasData: ConvocatoriaReporteDTO[] = [];
  reportePostulantesData: CoordinadorPostulanteReporteDTO[] = [];
  reporteCoordinadoresData: DecanoReporteCoordinadorDTO[] = [];

  // Filtros
  filtro = '';

  ngOnInit(): void {
    const user = this.authService.getUser();
    console.log('ReportesDecanoComponent.ngOnInit - User from getUser():', user);
    if (user && user.idUsuario) {
      console.log('User found with idUsuario:', user.idUsuario);
      this.cargarDatosDecano(user.idUsuario);
    } else {
      console.error('No authenticated user or idUsuario found in localStorage!');
      this.errorMensaje = 'No se pudo identificar al usuario actual.';
    }
  }

  cargarDatosDecano(idUsuario: number): void {
    this.decanoService.obtenerDecanoPorUsuario(idUsuario).subscribe({
      next: (decano) => {
        this.idFacultad = decano.idFacultad;
        console.log('Decano data loaded:', decano);
        console.log('Setting idFacultad to:', this.idFacultad);
        this.cambiarReporte();
      },
      error: () => this.errorMensaje = 'No se pudo obtener la información del decano.'
    });
  }

  cambiarReporte(): void {
    console.log('cambiarReporte() called, idFacultad:', this.idFacultad, 'tipoReporte:', this.tipoReporte);
    if (this.idFacultad === undefined || this.idFacultad === null) {
      console.warn('idFacultad is null or undefined, skipping report load.');
      return;
    }
    this.loading = true;
    this.filtro = '';

    const observer = {
      next: (data: any) => {
        this.asignarData(data);
        this.loading = false;
      },
      error: () => {
        this.errorMensaje = 'Error al cargar los datos del reporte.';
        this.loading = false;
      }
    };

    switch (this.tipoReporte) {
      case 'carreras':
        this.decanoService.obtenerReporteCarreras(this.idFacultad).subscribe(observer);
        break;
      case 'asignaturas':
        this.decanoService.obtenerReporteAsignaturas(this.idFacultad).subscribe(observer);
        break;
      case 'convocatorias':
        this.decanoService.obtenerReporteConvocatorias(this.idFacultad).subscribe(observer);
        break;
      case 'postulantes':
        this.decanoService.obtenerReportePostulantes(this.idFacultad).subscribe(observer);
        break;
      case 'coordinadores':
        this.decanoService.obtenerReporteCoordinadores(this.idFacultad).subscribe(observer);
        break;
    }
  }

  private asignarData(data: any): void {
    switch (this.tipoReporte) {
      case 'carreras': this.reporteCarrerasData = data; break;
      case 'asignaturas': this.reporteAsignaturasData = data; break;
      case 'convocatorias': this.reporteConvocatoriasData = data; break;
      case 'postulantes': this.reportePostulantesData = data; break;
      case 'coordinadores': this.reporteCoordinadoresData = data; break;
    }
  }

  get dataFiltrada(): any[] {
    const f = this.filtro.toLowerCase();
    switch (this.tipoReporte) {
      case 'carreras':
        return this.reporteCarrerasData.filter(c => c.nombreCarrera.toLowerCase().includes(f));
      case 'asignaturas':
        return this.reporteAsignaturasData.filter(a => a.nombreAsignatura.toLowerCase().includes(f) || a.nombreCarrera?.toLowerCase().includes(f));
      case 'convocatorias':
        return this.reporteConvocatoriasData.filter(c => c.nombreAsignatura.toLowerCase().includes(f) || c.nombreCoordinador.toLowerCase().includes(f));
      case 'postulantes':
        return this.reportePostulantesData.filter(p => p.nombreEstudiante.toLowerCase().includes(f) || p.cedula.includes(f) || p.nombreAsignatura.toLowerCase().includes(f));
      case 'coordinadores':
        return this.reporteCoordinadoresData.filter(c => c.nombreCoordinador.toLowerCase().includes(f) || c.carrera.toLowerCase().includes(f));
      default: return [];
    }
  }

  exportarPDF(): void {
    const doc = new jsPDF('l', 'mm', 'a4');
    const data = this.dataFiltrada;
    let headers: string[] = [];
    let rows: any[] = [];
    let title = '';

    switch (this.tipoReporte) {
      case 'carreras':
        title = 'Reporte de Carreras de la Facultad';
        headers = ['Carrera', 'Asignaturas', 'Convocatorias', 'Postulantes'];
        rows = data.map(c => [c.nombreCarrera, c.totalAsignaturas, c.totalConvocatorias, c.totalPostulantes]);
        break;
      case 'asignaturas':
        title = 'Reporte de Asignaturas de la Facultad';
        headers = ['Asignatura', 'Carrera', 'Estado'];
        rows = data.map(a => [a.nombreAsignatura, a.nombreCarrera, a.activo ? 'ACTIVO' : 'INACTIVO']);
        break;
      case 'convocatorias':
        title = 'Reporte de Convocatorias de la Facultad';
        headers = ['ID', 'Asignatura', 'Carrera', 'Coordinador', 'Vigencia', 'Postulantes', 'Estado'];
        rows = data.map(c => [c.idConvocatoria, c.nombreAsignatura, c.nombreCarrera, c.nombreCoordinador, `${c.fechaInicio} - ${c.fechaFin}`, c.numeroPostulantes, c.estado]);
        break;
      case 'postulantes':
        title = 'Listado General de Postulantes - Facultad';
        headers = ['ID', 'Estudiante', 'Asignatura', 'Fecha', 'Méritos (20)', 'Oposición (20)', 'Total (40)', 'Estado'];
        rows = data.map(p => [p.cedula, p.nombreEstudiante, p.nombreAsignatura, p.fechaPostulacion, p.puntajeMeritos, p.puntajeOposicion, p.puntajeTotal, p.estadoEvaluacion]);
        break;
      case 'coordinadores':
        title = 'Reporte de Desempeño de Coordinadores';
        headers = ['Coordinador', 'Carrera', 'Convocatorias', 'Postulantes', 'Estado'];
        rows = data.map(c => [c.nombreCoordinador, c.carrera, c.convocatoriasCreadas, c.postulantesGestionados, c.estado]);
        break;
    }

    doc.text(title, 14, 15);
    autoTable(doc, {
      head: [headers],
      body: rows,
      startY: 20,
      theme: 'grid',
      headStyles: { fillColor: [22, 163, 74] }
    });
    doc.save(`${this.tipoReporte}_facultad.pdf`);
  }

  exportarExcel(): void {
    const worksheet = XLSX.utils.json_to_sheet(this.dataFiltrada);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Reporte');
    XLSX.writeFile(workbook, `${this.tipoReporte}_facultad.xlsx`);
  }
}
