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

  // New report types
  tipoReporte: 'convocatorias' | 'postulantes' | 'resultados' = 'convocatorias';
  loading = false;
  errorMensaje = '';

  convocatorias: CoordinadorConvocatoriaReporteDTO[] = [];
  postulantes: CoordinadorPostulanteReporteDTO[] = [];

  // Paginación y Filtrado simple
  filtroConvocatoria = '';
  filtroPostulante = '';
  filtroResultados = '';

  ngOnInit() {
    this.cargarDatos();
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

    // Cargar Postulantes (includes scores now)
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

  get resultadosFiltrados() {
    if (!this.filtroResultados) return this.postulantes;
    const filter = this.filtroResultados.toLowerCase();
    return this.postulantes.filter(p =>
      p.nombreEstudiante.toLowerCase().includes(filter) ||
      p.cedula.includes(filter) ||
      p.nombreAsignatura.toLowerCase().includes(filter)
    );
  }

  // ==========================================
  // EXPORTACIONES
  // ==========================================

  exportarPDF() {
    if (this.tipoReporte === 'convocatorias') this.exportarConvocatoriasPDF();
    else if (this.tipoReporte === 'postulantes') this.exportarPostulantesPDF();
    else if (this.tipoReporte === 'resultados') this.exportarResultadosPDF();
  }

  exportarExcel() {
    if (this.tipoReporte === 'convocatorias') this.exportarConvocatoriasExcel();
    else if (this.tipoReporte === 'postulantes') this.exportarPostulantesExcel();
    else if (this.tipoReporte === 'resultados') this.exportarResultadosExcel();
  }

  private exportarConvocatoriasPDF() {
    const doc = new jsPDF();
    doc.text('Reporte de Convocatorias Propias', 14, 15);

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

  private exportarConvocatoriasExcel() {
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

  private exportarPostulantesPDF() {
    const doc = new jsPDF();
    doc.text('Reporte Nominal de Postulantes', 14, 15);

    const bodyArgs = this.postulantesFiltrados.map(p => [
      p.cedula,
      p.nombreEstudiante,
      p.nombreAsignatura,
      p.nombrePeriodo,
      new Date(p.fechaPostulacion).toLocaleDateString(),
      p.estadoEvaluacion,
      p.puntajeTotal || 0
    ]);

    autoTable(doc, {
      startY: 20,
      head: [['Cédula', 'Estudiante', 'Asignatura', 'Periodo', 'Fecha', 'Estado', 'Puntaje']],
      body: bodyArgs
    });

    doc.save('Reporte_Postulantes.pdf');
  }

  private exportarPostulantesExcel() {
    const ws = XLSX.utils.json_to_sheet(this.postulantesFiltrados.map(p => ({
      Cédula: p.cedula,
      Estudiante: p.nombreEstudiante,
      Asignatura: p.nombreAsignatura,
      Periodo: p.nombrePeriodo,
      'Fecha Postulación': new Date(p.fechaPostulacion).toLocaleString(),
      Estado: p.estadoEvaluacion,
      'Puntaje Total': p.puntajeTotal
    })));
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Postulantes");
    XLSX.writeFile(wb, "Reporte_Postulantes.xlsx");
  }

  private exportarResultadosPDF() {
    const doc = new jsPDF({ orientation: 'landscape' });
    doc.text('Cuadro General de Resultados (Méritos y Oposición)', 14, 15);

    const bodyArgs = this.resultadosFiltrados.map(p => [
      p.cedula,
      p.nombreEstudiante,
      p.nombreAsignatura,
      p.puntajeMeritos || 0,
      p.puntajeOposicion || 0,
      p.puntajeTotal || 0,
      p.estadoEvaluacion
    ]);

    autoTable(doc, {
      startY: 20,
      head: [['Cédula', 'Estudiante', 'Asignatura', 'Méritos', 'Oposición', 'Total', 'Estado']],
      body: bodyArgs,
      theme: 'grid'
    });

    doc.save('Cuadro_Resultados_Finales.pdf');
  }

  private exportarResultadosExcel() {
    const ws = XLSX.utils.json_to_sheet(this.resultadosFiltrados.map(p => ({
      Cédula: p.cedula,
      Estudiante: p.nombreEstudiante,
      Asignatura: p.nombreAsignatura,
      Periodo: p.nombrePeriodo,
      'Puntaje Méritos': p.puntajeMeritos,
      'Puntaje Oposición': p.puntajeOposicion,
      'Total': p.puntajeTotal,
      Estado: p.estadoEvaluacion,
      Observaciones: p.observacionPostulacion
    })));
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Cuadro Resultados");
    XLSX.writeFile(wb, "Cuadro_Resultados.xlsx");
  }
}
