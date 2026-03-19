import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import {
  CoordinadorConvocatoriaReporteDTO,
  CoordinadorPostulanteReporteDTO,
  CoordinadorEstadisticasDTO
} from '../../../core/dto/coordinador';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

export type ReportType =
  | 'CONVOCATORIAS_GENERAL'
  | 'POSTULANTES_GENERAL'
  | 'RESUMEN_EJECUTIVO'
  | 'POSTULANTES_VERSUS'
  | 'POSTULANTES_DESGLOSE'
  | 'CONVOCATORIAS_ESTADO';

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

  activeReport: ReportType = 'CONVOCATORIAS_GENERAL';
  loading = false;
  errorMensaje = '';

  convocatorias: CoordinadorConvocatoriaReporteDTO[] = [];
  postulantes: CoordinadorPostulanteReporteDTO[] = [];
  estadisticas?: CoordinadorEstadisticasDTO;

  filtroTabla = '';

  ngOnInit() {
    this.cargarDatos();
  }

  setReport(type: ReportType) {
    this.activeReport = type;
    this.filtroTabla = '';
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

    // Since we need to join 3 calls manually to not depend on forkJoin syntax specifics
    let resCount = 0;
    const checkDone = () => {
      resCount++;
      if (resCount === 3) this.loading = false;
    };

    this.coordinadorService.obtenerReporteConvocatoriasPropias(user.idUsuario).subscribe({
      next: (data) => { this.convocatorias = data; checkDone(); },
      error: () => { this.errorMensaje = 'Error al cargar reporte convocatorias.'; checkDone(); }
    });

    this.coordinadorService.obtenerReportePostulantesPropios(user.idUsuario).subscribe({
      next: (data) => { this.postulantes = data; checkDone(); },
      error: () => { this.errorMensaje = 'Error al cargar reporte postulantes.'; checkDone(); }
    });

    this.coordinadorService.obtenerEstadisticasPropias(user.idUsuario).subscribe({
      next: (data) => { this.estadisticas = data; checkDone(); },
      error: () => { this.errorMensaje = 'Error al cargar estadísticas.'; checkDone(); }
    });
  }

  // Generic Data Getters for Preview Tables
  get dataVistaPrevia(): any[] {
    const f = this.filtroTabla.toLowerCase();
    switch (this.activeReport) {
      case 'CONVOCATORIAS_GENERAL':
      case 'CONVOCATORIAS_ESTADO':
        return this.convocatorias.filter(c => c.nombreAsignatura.toLowerCase().includes(f) || c.estado.toLowerCase().includes(f));

      case 'POSTULANTES_GENERAL':
      case 'POSTULANTES_VERSUS':
      case 'POSTULANTES_DESGLOSE':
        return this.postulantes.filter(p => p.nombreEstudiante.toLowerCase().includes(f) || p.cedula.includes(f) || p.nombreAsignatura.toLowerCase().includes(f));

      case 'RESUMEN_EJECUTIVO':
        return [];
    }
    return [];
  }

  // ==========================================
  // EXPORTACIONES MASTER
  // ==========================================

  exportar(formato: 'PDF' | 'EXCEL') {
    switch (this.activeReport) {
      case 'CONVOCATORIAS_GENERAL':
        formato === 'PDF' ? this.pdfConvocatoriasGeneral() : this.excelConvocatoriasGeneral(); break;
      case 'POSTULANTES_GENERAL':
        formato === 'PDF' ? this.pdfPostulantesGeneral() : this.excelPostulantesGeneral(); break;
      case 'RESUMEN_EJECUTIVO':
        if (formato === 'PDF') this.pdfResumenEjecutivo(); break;
      case 'POSTULANTES_VERSUS':
        formato === 'PDF' ? this.pdfPostulantesVersus() : this.excelPostulantesVersus(); break;
      case 'POSTULANTES_DESGLOSE':
        formato === 'PDF' ? this.pdfPostulantesDesglose() : this.excelPostulantesDesglose(); break;
      case 'CONVOCATORIAS_ESTADO':
        formato === 'PDF' ? this.pdfConvocatoriasEstado() : this.excelConvocatoriasEstado(); break;
    }
  }

  // 1. Convocatorias General
  private pdfConvocatoriasGeneral() {
    const doc = new jsPDF();
    doc.text('Reporte General de Convocatorias', 14, 15);
    autoTable(doc, {
      startY: 20, head: [['ID', 'Asignatura', 'Periodo', 'Fechas', 'Cupos', 'Postulantes', 'Estado']],
      body: this.convocatorias.map(c => [c.idConvocatoria, c.nombreAsignatura, c.nombrePeriodo, `${c.fechaInicio} a ${c.fechaFin}`, c.cuposAprobados, c.numeroPostulantes, c.estado])
    });
    doc.save('Convocatorias_General.pdf');
  }
  private excelConvocatoriasGeneral() {
    const ws = XLSX.utils.json_to_sheet(this.convocatorias.map(c => ({ ID: c.idConvocatoria, Asignatura: c.nombreAsignatura, Periodo: c.nombrePeriodo, Cupos: c.cuposAprobados, Postulantes: c.numeroPostulantes, Estado: c.estado })));
    this.saveExcel(ws, 'Convocatorias_General');
  }

  // 2. Postulantes General
  private pdfPostulantesGeneral() {
    const doc = new jsPDF();
    doc.text('Reporte General de Postulantes', 14, 15);
    autoTable(doc, {
      startY: 20, head: [['Cédula', 'Estudiante', 'Asignatura', 'Periodo', 'Estado']],
      body: this.postulantes.map(p => [p.cedula, p.nombreEstudiante, p.nombreAsignatura, p.nombrePeriodo, p.estadoEvaluacion])
    });
    doc.save('Postulantes_General.pdf');
  }
  private excelPostulantesGeneral() {
    const ws = XLSX.utils.json_to_sheet(this.postulantes.map(p => ({ Cédula: p.cedula, Estudiante: p.nombreEstudiante, Asignatura: p.nombreAsignatura, Estado: p.estadoEvaluacion })));
    this.saveExcel(ws, 'Postulantes_General');
  }

  // 3. Resumen Ejecutivo (Solo PDF por estética)
  private pdfResumenEjecutivo() {
    if (!this.estadisticas) return;
    const doc = new jsPDF();
    doc.setFontSize(18);
    doc.text('Resumen Ejecutivo del Coordinador', 14, 20);
    doc.setFontSize(12);
    doc.text(`Total Convocatorias: ${this.estadisticas.totalConvocatoriasPropias}`, 14, 30);
    doc.text(`   - Activas: ${this.estadisticas.convocatoriasActivas}`, 14, 38);
    doc.text(`   - Inactivas: ${this.estadisticas.convocatoriasInactivas}`, 14, 46);

    doc.text(`Total Postulantes Recibidos: ${this.estadisticas.totalPostulantesRecibidos}`, 14, 60);
    doc.text(`   - Aprobados: ${this.estadisticas.postulantesAprobados}`, 14, 68);
    doc.text(`   - Rechazados: ${this.estadisticas.postulantesRechazados}`, 14, 76);
    doc.text(`   - Evaluando/Pendientes: ${this.estadisticas.postulantesEnEvaluacion + this.estadisticas.postulantesPendientes}`, 14, 84);

    doc.text('Desglose Rápido por Asignatura:', 14, 100);
    autoTable(doc, {
      startY: 105, head: [['Asignatura', 'Postulantes Recibidos']],
      body: this.estadisticas.postulantesPorConvocatoria.map(p => [p.tituloConvocatoria, p.cantidadPostulantes])
    });
    doc.save('Resumen_Ejecutivo.pdf');
  }

  // 4. Postulantes Aprobados vs Rechazados
  private getPostulantesVersusData() {
    return this.postulantes.filter(p => p.estadoEvaluacion === 'APROBADO' || p.estadoEvaluacion === 'RECHAZADO');
  }
  private pdfPostulantesVersus() {
    const doc = new jsPDF();
    doc.text('Postulantes: Aprobados vs Rechazados', 14, 15);
    autoTable(doc, {
      startY: 20, head: [['Cédula', 'Estudiante', 'Asignatura', 'Estado']],
      body: this.getPostulantesVersusData().map(p => [p.cedula, p.nombreEstudiante, p.nombreAsignatura, p.estadoEvaluacion])
    });
    doc.save('Postulantes_Aprobados_Rechazados.pdf');
  }
  private excelPostulantesVersus() {
    const data = this.getPostulantesVersusData().map(p => ({ Cédula: p.cedula, Estudiante: p.nombreEstudiante, Asignatura: p.nombreAsignatura, Estado: p.estadoEvaluacion }));
    this.saveExcel(XLSX.utils.json_to_sheet(data), 'Postulantes_Aprobs_Rechs');
  }

  // 5. Desglose de Postulantes por Convocatoria
  private pdfPostulantesDesglose() {
    const doc = new jsPDF();
    doc.text('Desglose de Postulantes por Convocatoria (Asignatura)', 14, 15);

    const agrupado = this.agruparPostulantesPorAsignatura();
    let currentY = 25;
    for (const [asignatura, posts] of Object.entries(agrupado)) {
      doc.setFontSize(11);
      doc.text(`Asignatura: ${asignatura} (${(posts as any[]).length} postulantes)`, 14, currentY);
      autoTable(doc, {
        startY: currentY + 5,
        head: [['Cédula', 'Estudiante', 'Estado']],
        body: (posts as any[]).map(p => [p.cedula, p.nombreEstudiante, p.estadoEvaluacion]),
        margin: { bottom: 20 }
      });
      currentY = (doc as any).lastAutoTable.finalY + 15;
    }
    doc.save('Desglose_Por_Convocatoria.pdf');
  }
  private excelPostulantesDesglose() {
    const wsData: any[] = [];
    const agrupado = this.agruparPostulantesPorAsignatura();
    for (const [asignatura, posts] of Object.entries(agrupado)) {
      wsData.push({ Cédula: `--- ${asignatura} ---`, Estudiante: '', Estado: '' });
      (posts as any[]).forEach(p => wsData.push({ Cédula: p.cedula, Estudiante: p.nombreEstudiante, Estado: p.estadoEvaluacion }));
    }
    this.saveExcel(XLSX.utils.json_to_sheet(wsData), 'Desglose_Postulantes');
  }
  private agruparPostulantesPorAsignatura() {
    return this.postulantes.reduce((acc, obj) => {
      const key = obj.nombreAsignatura;
      if (!acc[key]) acc[key] = [];
      acc[key].push(obj); return acc;
    }, {} as Record<string, CoordinadorPostulanteReporteDTO[]>);
  }

  // 6. Convocatorias por Estado
  private pdfConvocatoriasEstado() {
    const doc = new jsPDF();
    doc.text('Convocatorias Agrupadas por Estado', 14, 15);
    const agrupado = this.convocatorias.reduce((acc, obj) => {
      const key = obj.estado; if (!acc[key]) acc[key] = []; acc[key].push(obj); return acc;
    }, {} as Record<string, CoordinadorConvocatoriaReporteDTO[]>);

    let currentY = 25;
    for (const [estado, convs] of Object.entries(agrupado)) {
      doc.text(`Estado: ${estado} (${convs.length} items)`, 14, currentY);
      autoTable(doc, {
        startY: currentY + 5, head: [['Asignatura', 'Cupos', 'Postulantes']],
        body: convs.map(c => [c.nombreAsignatura, c.cuposAprobados, c.numeroPostulantes])
      });
      currentY = (doc as any).lastAutoTable.finalY + 15;
    }
    doc.save('Convocatorias_Por_Estado.pdf');
  }
  private excelConvocatoriasEstado() {
    // similar logic for grouping
    const wsData: any[] = [];
    const agrupado = this.convocatorias.reduce((acc, obj) => {
      const key = obj.estado; if (!acc[key]) acc[key] = []; acc[key].push(obj); return acc;
    }, {} as Record<string, CoordinadorConvocatoriaReporteDTO[]>);

    for (const [estado, convs] of Object.entries(agrupado)) {
      wsData.push({ Asignatura: `[ESTADO: ${estado}]`, Cupos: '', Postulantes: '' });
      convs.forEach(c => wsData.push({ Asignatura: c.nombreAsignatura, Cupos: c.cuposAprobados, Postulantes: c.numeroPostulantes }));
    }
    this.saveExcel(XLSX.utils.json_to_sheet(wsData), 'Convocatorias_Por_Estado');
  }

  // Utilería
  private saveExcel(ws: XLSX.WorkSheet, filename: string) {
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Reporte");
    XLSX.writeFile(wb, `${filename}.xlsx`);
  }
}
