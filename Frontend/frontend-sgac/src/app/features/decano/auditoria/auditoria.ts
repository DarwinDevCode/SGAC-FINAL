import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { DecanoService } from '../../../core/services/decano-service';
import { AuthService } from '../../../core/services/auth-service';
import { ConvocatoriaReporteDTO, LogAuditoriaDTO, DecanoResponseDTO } from '../../../core/dto/decano';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.css',
})
export class AuditoriaComponent implements OnInit, OnDestroy {
  activeTab: 'reportes' | 'auditoria' = 'reportes';

  decanoService = inject(DecanoService);
  authService = inject(AuthService);
  private subs = new Subscription();

  decanoData: DecanoResponseDTO | null = null;
  reportes: ConvocatoriaReporteDTO[] = [];
  auditoriaLogs: LogAuditoriaDTO[] = [];

  loading = true;
  errorMensaje = '';

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cambiarPestana(tab: 'reportes' | 'auditoria'): void {
    this.activeTab = tab;
  }

  cargarDatos() {
    this.loading = true;
    const user = this.authService.getUser();

    if (!user) {
      this.errorMensaje = 'No hay sesión activa.';
      this.loading = false;
      return;
    }

    this.subs.add(
      this.decanoService.obtenerDecanoPorUsuario(user.idUsuario).subscribe({
        next: (decano) => {
          this.decanoData = decano;
          this.cargarListas(decano.idFacultad);
        },
        error: (err) => {
          this.errorMensaje = 'No se encontró tu registro como Decano.';
          this.loading = false;
        }
      })
    );
  }

  private cargarListas(idFacultad: number) {
    this.subs.add(
      this.decanoService.obtenerReporteConvocatorias(idFacultad).subscribe({
        next: (res) => {
          this.reportes = res;
          this.checkIfDone();
        },
        error: () => this.checkIfDone()
      })
    );

    this.subs.add(
      this.decanoService.obtenerReporteAuditoria(idFacultad).subscribe({
        next: (res) => {
          this.auditoriaLogs = res;
          this.checkIfDone();
        },
        error: () => this.checkIfDone()
      })
    );
  }

  // Una manera simple de quitar el loader cuando ambas terminen (o fallen)
  private loadCount = 0;
  private checkIfDone() {
    this.loadCount++;
    if (this.loadCount >= 2) {
      this.loading = false;
    }
  }

  // ============== EXPORTACIONES ==============

  exportarReportesPDF() {
    const doc = new jsPDF();
    doc.text('Reporte de Convocatorias - Facultad', 14, 15);

    const body = this.reportes.map(r => [
      r.idConvocatoria,
      r.nombreAsignatura,
      r.nombreCarrera,
      r.nombreCoordinador,
      r.fechaInicio,
      r.estado
    ]);

    autoTable(doc, {
      head: [['ID', 'Asignatura', 'Carrera', 'Coordinador', 'Fecha Inicio', 'Estado']],
      body: body,
      startY: 20,
      styles: { fontSize: 8 }
    });

    doc.save('Reporte_Convocatorias_SGAC.pdf');
  }

  exportarReportesExcel() {
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.reportes);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Reportes');
    XLSX.writeFile(wb, 'Reporte_Convocatorias_SGAC.xlsx');
  }

  exportarAuditoriaPDF() {
    const doc = new jsPDF();
    doc.text('Trazabilidad y Auditoría - Coordinadores', 14, 15);

    const body = this.auditoriaLogs.map(l => [
      l.idLog,
      l.nombreUsuario,
      l.accion,
      l.tablaAfectada,
      new Date(l.fechaHora).toLocaleString()
    ]);

    autoTable(doc, {
      head: [['ID', 'Usuario (Coordinador)', 'Acción Realizada', 'Tabla Afectada', 'Fecha y Hora']],
      body: body,
      startY: 20,
      styles: { fontSize: 8 }
    });

    doc.save('Reporte_Auditoria_SGAC.pdf');
  }

  exportarAuditoriaExcel() {
    const dataFiltrada = this.auditoriaLogs.map(l => ({
      ID: l.idLog,
      Usuario: l.nombreUsuario,
      Accion: l.accion,
      Modulo: l.tablaAfectada,
      Fecha: new Date(l.fechaHora).toLocaleString()
    }));
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(dataFiltrada);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Auditoria');
    XLSX.writeFile(wb, 'Reporte_Auditoria_SGAC.xlsx');
  }
}
