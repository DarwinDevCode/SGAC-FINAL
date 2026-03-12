import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { CoordinadorConvocatoriaReporteDTO, CoordinadorPostulanteReporteDTO } from '../../../core/dto/coordinador';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-coordinador-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './reportes.html',
  styleUrls: ['./reportes.css']
})
export class ReportesComponent implements OnInit {
  coordinadorService = inject(CoordinadorService);
  authService = inject(AuthService);

  activeTab: 'convocatorias' | 'postulantes' = 'convocatorias';
  loading = false;
  errorMensaje = '';

  convocatorias: CoordinadorConvocatoriaReporteDTO[] = [];
  postulantes: CoordinadorPostulanteReporteDTO[] = [];

  // Paginación y Filtrado simple
  filtroConvocatoria = '';
  filtroPostulante = '';

  ngOnInit() {
    this.cargarDatos();
  }

  cambiarTab(tab: 'convocatorias' | 'postulantes') {
    this.activeTab = tab;
  }

  cargarDatos() {
    this.loading = true;
    this.errorMensaje = '';
    const user = this.authService.getUser();

    if (!user) {
      this.errorMensaje = 'Sesión no encontrada.';
      this.loading = false;
      return;
    }

    // Cargar Convocatorias
    this.coordinadorService.obtenerReporteConvocatoriasPropias(user.idUsuario).subscribe({
      next: (data) => this.convocatorias = data,
      error: (err) => {
        console.error(err);
        this.errorMensaje = 'Error al cargar reporte de convocatorias.';
      }
    });

    // Cargar Postulantes
    this.coordinadorService.obtenerReportePostulantesPropios(user.idUsuario).subscribe({
      next: (data) => {
        this.postulantes = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMensaje = 'Error al cargar reporte de postulantes.';
        this.loading = false;
      }
    });
  }

  // Gets filtered lists
  get convocatoriasFiltradas() {
    if (!this.filtroConvocatoria) return this.convocatorias;
    const filter = this.filtroConvocatoria.toLowerCase();
    return this.convocatorias.filter(c =>
      c.nombreAsignatura.toLowerCase().includes(filter) ||
      c.estado.toLowerCase().includes(filter)
    );
  }

  get postulantesFiltrados() {
    if (!this.filtroPostulante) return this.postulantes;
    const filter = this.filtroPostulante.toLowerCase();
    return this.postulantes.filter(p =>
      p.nombreEstudiante.toLowerCase().includes(filter) ||
      p.cedula.includes(filter) ||
      p.estadoEvaluacion.toLowerCase().includes(filter)
    );
  }

  // ==========================================
  // EXPORTACIONES
  // ==========================================

  exportarConvocatoriasPDF() {
    const doc = new jsPDF();
    doc.text('Reporte de Convocatorias Propias', 14, 15);

    // Formatting data for autotable
    const bodyArgs = this.convocatoriasFiltradas.map(c => [
      c.idConvocatoria,
      c.nombreAsignatura,
      c.nombrePeriodo,
      `${c.fechaInicio} a ${c.fechaFin}`,
      c.cuposAprobados,
      c.numeroPostulantes,
      c.estado
    ]);

    autoTable(doc, {
      startY: 20,
      head: [['ID', 'Asignatura', 'Periodo', 'Vigencia', 'Cupos', 'Postulantes', 'Estado']],
      body: bodyArgs
    });

    doc.save('Reporte_Convocatorias.pdf');
  }

  exportarConvocatoriasExcel() {
    const ws = XLSX.utils.json_to_sheet(this.convocatoriasFiltradas.map(c => ({
      ID: c.idConvocatoria,
      Asignatura: c.nombreAsignatura,
      Carrera: c.nombreCarrera,
      Periodo: c.nombrePeriodo,
      'Fecha Inicio': c.fechaInicio,
      'Fecha Fin': c.fechaFin,
      Cupos: c.cuposAprobados,
      Postulantes: c.numeroPostulantes,
      Estado: c.estado
    })));
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Convocatorias");
    XLSX.writeFile(wb, "Reporte_Convocatorias.xlsx");
  }

  exportarPostulantesPDF() {
    const doc = new jsPDF();
    doc.text('Reporte de Postulantes A Cargo', 14, 15);

    const bodyArgs = this.postulantesFiltrados.map(p => [
      p.cedula,
      p.nombreEstudiante,
      p.nombreAsignatura,
      p.nombrePeriodo,
      new Date(p.fechaPostulacion).toLocaleDateString(),
      p.estadoEvaluacion
    ]);

    autoTable(doc, {
      startY: 20,
      head: [['Cédula', 'Estudiante', 'Asignatura', 'Periodo', 'Fecha Postulación', 'Estado']],
      body: bodyArgs
    });

    doc.save('Reporte_Postulantes.pdf');
  }

  exportarPostulantesExcel() {
    const ws = XLSX.utils.json_to_sheet(this.postulantesFiltrados.map(p => ({
      Cédula: p.cedula,
      Estudiante: p.nombreEstudiante,
      Asignatura: p.nombreAsignatura,
      Periodo: p.nombrePeriodo,
      'Fecha Postulación': new Date(p.fechaPostulacion).toLocaleString(),
      Estado: p.estadoEvaluacion
    })));
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Postulantes");
    XLSX.writeFile(wb, "Reporte_Postulantes.xlsx");
  }
}
