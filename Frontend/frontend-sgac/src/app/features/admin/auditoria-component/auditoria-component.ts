import { Component, OnInit, signal } from '@angular/core';
import { AuditoriaService } from '../../../core/services/reports_audit/auditoria-service';
import { AuditoriaResponseDTO, Page } from '../../../core/models/reportes_y_auditoria/reports_audit';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
// IMPORTAMOS CHART.JS
import { Chart } from 'chart.js/auto';

// Interfaces temporales (puedes moverlas a tu archivo reports_audit.ts si prefieres)
export interface AuditoriaKpiDTO {
  totalRegistros: number;
  actividadHoy: number;
  totalInserts: number;
  totalUpdates: number;
  totalDeletes: number;
}

export interface EvolucionAuditoria {
  fecha: string;
  inserts: number;
  updates: number;
  deletes: number;
}

@Component({
  selector: 'app-auditoria',
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './auditoria-component.html',
  styleUrls: ['../../ayudante/matriz-asistencia-component/matriz-asistencia-component.css', './auditoria-component.css']
})
export class AuditoriaComponent implements OnInit {

  cargando = signal<boolean>(false);
  errorMsg = signal<string | null>(null);

  registros = signal<AuditoriaResponseDTO[]>([]);
  totalElementos = signal<number>(0);

  paginaActual = signal<number>(0);
  pageSize = signal<number>(10);

  // Filtros completos
  filtroAccion = signal<string>('');
  filtroTabla = signal<string>('');
  filtroUsuario = signal<number | null>(null);
  filtroFechaInicio = signal<string>('');
  filtroFechaFin = signal<string>('');

  registroSeleccionado = signal<AuditoriaResponseDTO | null>(null);

  // NUEVO: Signals para el Dashboard (KPIs y Gráfica)
  kpis = signal<AuditoriaKpiDTO | null>(null);
  graficaInstancia: any;

  constructor(private auditoriaService: AuditoriaService) {}

  ngOnInit(): void {
    this.cargarDatos();
    this.cargarDashboard(); // Cargamos los datos visuales al iniciar
  }

  // --- NUEVA LÓGICA DEL DASHBOARD ---
  cargarDashboard(): void {
    // 1. Obtener KPIs (Asegúrate de tener este método en tu auditoria-service.ts)
    this.auditoriaService.obtenerKpis().subscribe({
      next: (data) => this.kpis.set(data),
      error: (err) => console.error('Error cargando KPIs', err)
    });

    // 2. Obtener datos de evolución y renderizar la gráfica
    this.auditoriaService.obtenerEvolucion().subscribe({
      next: (data) => this.renderizarGrafica(data),
      error: (err) => console.error('Error cargando evolución', err)
    });
  }

  renderizarGrafica(datos: EvolucionAuditoria[]): void {
    const canvas = document.getElementById('auditoriaChart') as HTMLCanvasElement;
    if (!canvas) return;

    // Si la gráfica ya existe, la destruimos para evitar superposición
    if (this.graficaInstancia) {
      this.graficaInstancia.destroy();
    }

    const labels = datos.map(d => d.fecha);
    const inserts = datos.map(d => d.inserts);
    const updates = datos.map(d => d.updates);
    const deletes = datos.map(d => d.deletes);

    this.graficaInstancia = new Chart(canvas, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'INSERT (Creaciones)',
            data: inserts,
            borderColor: '#10b981', // green-soft
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            borderWidth: 2,
            tension: 0.3,
            fill: true
          },
          {
            label: 'UPDATE (Modificaciones)',
            data: updates,
            borderColor: '#f59e0b', // amber-soft
            backgroundColor: 'rgba(245, 158, 11, 0.1)',
            borderWidth: 2,
            tension: 0.3,
            fill: true
          },
          {
            label: 'DELETE (Eliminaciones)',
            data: deletes,
            borderColor: '#ef4444', // red-soft
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            borderWidth: 2,
            tension: 0.3,
            fill: true
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { position: 'top', labels: { usePointStyle: true, boxWidth: 8 } }
        },
        scales: {
          y: { beginAtZero: true, ticks: { precision: 0 } }
        }
      }
    });
  }
  // --- FIN LÓGICA DEL DASHBOARD ---

  // --- TU LÓGICA INTACTA ---
  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);

    const fInicio = this.filtroFechaInicio() ? `${this.filtroFechaInicio()}T00:00:00` : undefined;
    const fFin = this.filtroFechaFin() ? `${this.filtroFechaFin()}T23:59:59` : undefined;

    this.auditoriaService.listarAuditorias(
      this.paginaActual(),
      this.pageSize(),
      this.filtroTabla() || undefined,
      this.filtroAccion() || undefined,
      this.filtroUsuario() || undefined,
      fInicio,
      fFin
    ).subscribe({
      next: (page) => {
        this.registros.set(page.content);
        this.totalElementos.set(page.totalElements);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set('No se pudo cargar el registro de auditoría.');
        this.cargando.set(false);
      }
    });
  }

  aplicarFiltros(): void {
    this.paginaActual.set(0);
    this.cargarDatos();
  }

  limpiarFiltros(): void {
    this.filtroAccion.set('');
    this.filtroTabla.set('');
    this.filtroUsuario.set(null);
    this.filtroFechaInicio.set('');
    this.filtroFechaFin.set('');
    this.aplicarFiltros();
  }

  cambiarPagina(nuevaPagina: number): void {
    this.paginaActual.set(nuevaPagina);
    this.cargarDatos();
  }

  verDetalles(registro: AuditoriaResponseDTO): void {
    this.registroSeleccionado.set(registro);
  }

  cerrarModal(): void {
    this.registroSeleccionado.set(null);
  }

  formatearJson(obj: any): string {
    if (!obj || Object.keys(obj).length === 0) return 'Sin datos registrados.';
    return JSON.stringify(obj, null, 2);
  }
}
