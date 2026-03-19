import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { CoordinadorResponseDTO, CoordinadorEstadisticasDTO } from '../../../core/dto/coordinador';

@Component({
  selector: 'app-coordinador-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LucideAngularModule, BaseChartDirective],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  coordinadorService = inject(CoordinadorService);
  authService = inject(AuthService);
  http = inject(HttpClient);
  private subs = new Subscription();

  coordinadorData: CoordinadorResponseDTO | null = null;
  estadisticas: CoordinadorEstadisticasDTO | null = null;

  loading = true;
  errorMensaje = '';

  msgMasivo = '';
  tipoMasivo = 'CONVOCATORIA';
  enviadoMasivo = false;
  enviandoMasivo = false;
  respuestaMasiva = '';

  // ── Gráfico 1: Barras horizontales — Top convocatorias por postulantes ──
  public barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      tooltip: { callbacks: { label: (ctx) => ` ${ctx.parsed.x} postulantes` } }
    },
    scales: { x: { beginAtZero: true, ticks: { stepSize: 1 } } }
  };
  public barChartType = 'bar' as const;
  public barChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  // ── Gráfico 2: Doughnut — Estado de Convocatorias ──
  public doughnutConvocatoriasOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { font: { size: 11 } } }
    },
    cutout: '70%'
  };
  public doughnutConvocatoriasType = 'doughnut' as const;
  public doughnutConvocatoriasData: ChartData<'doughnut'> = { labels: [], datasets: [] };

  // ── Gráfico 3: Doughnut — Estados de Postulantes ──
  public doughnutPostulantesOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { font: { size: 11 } } }
    },
    cutout: '70%'
  };
  public doughnutPostulantesType = 'doughnut' as const;
  public doughnutPostulantesData: ChartData<'doughnut'> = { labels: [], datasets: [] };

  ngOnInit(): void {
    this.cargarDatosDashboard();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cargarDatosDashboard() {
    this.loading = true;
    const user = this.authService.getUser();

    if (!user) {
      this.errorMensaje = 'Sesión no encontrada.';
      this.loading = false;
      return;
    }

    this.subs.add(
      this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario).subscribe({
        next: (coord: CoordinadorResponseDTO) => {
          this.coordinadorData = coord;

          this.subs.add(
            this.coordinadorService.obtenerEstadisticasPropias(user.idUsuario).subscribe({
              next: (stats: CoordinadorEstadisticasDTO) => {
                this.estadisticas = {
                  ...stats,
                  totalConvocatoriasPropias: stats.totalConvocatoriasPropias ?? 0,
                  convocatoriasActivas: stats.convocatoriasActivas ?? 0,
                  convocatoriasInactivas: stats.convocatoriasInactivas ?? 0,
                  totalPostulantesRecibidos: stats.totalPostulantesRecibidos ?? 0,
                  postulantesAprobados: stats.postulantesAprobados ?? 0,
                  postulantesRechazados: stats.postulantesRechazados ?? 0,
                  postulantesEnEvaluacion: stats.postulantesEnEvaluacion ?? 0,
                  postulantesPendientes: stats.postulantesPendientes ?? 0,
                  postulantesPorConvocatoria: stats.postulantesPorConvocatoria ?? []
                };

                this.configurarGraficos(this.estadisticas);
                this.loading = false;
              },
              error: (err) => {
                console.error('Error al cargar estadísticas:', err);
                this.errorMensaje = 'No se pudieron cargar las métricas.';
                this.loading = false;
              }
            })
          );
        },
        error: (err) => {
          console.error('Error al cargar Coordinador:', err);
          this.errorMensaje = 'Usuario no registrado como Coordinador activo.';
          this.loading = false;
        }
      })
    );
  }

  private configurarGraficos(stats: CoordinadorEstadisticasDTO) {
    const lista = stats.postulantesPorConvocatoria || [];

    // Gráfico 1: Horizontal bar — Top convocatorias
    this.barChartData = {
      labels: lista.map(p => {
        const title = p.tituloConvocatoria || 'Sin Título';
        return title.length > 25 ? title.substring(0, 25) + '…' : title;
      }),
      datasets: [{
        data: lista.map(p => p.cantidadPostulantes || 0),
        label: 'Postulantes',
        backgroundColor: [
          '#6366f1', '#3b82f6', '#06b6d4', '#10b981', '#f59e0b'
        ],
        borderRadius: 6,
        hoverBackgroundColor: '#4f46e5'
      }]
    };

    // Gráfico 2: Doughnut — Convocatorias activas vs inactivas
    this.doughnutConvocatoriasData = {
      labels: ['Activas', 'Cerradas'],
      datasets: [{
        data: [stats.convocatoriasActivas, stats.convocatoriasInactivas],
        backgroundColor: ['#10b981', '#94a3b8'],
        hoverBackgroundColor: ['#059669', '#64748b'],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    };

    // Gráfico 3: Doughnut — Estado postulantes
    this.doughnutPostulantesData = {
      labels: ['Seleccionados', 'No Selec.', 'En Evaluación', 'Pendientes'],
      datasets: [{
        data: [
          stats.postulantesAprobados,
          stats.postulantesRechazados,
          stats.postulantesEnEvaluacion,
          stats.postulantesPendientes
        ],
        backgroundColor: ['#10b981', '#ef4444', '#f59e0b', '#6366f1'],
        hoverBackgroundColor: ['#059669', '#dc2626', '#d97706', '#4f46e5'],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    };
  }

  /** Tasa de selección = SELECCIONADOS / total */
  get tasaAprobacion(): number {
    if (!this.estadisticas || this.estadisticas.totalPostulantesRecibidos === 0) return 0;
    return Math.round((this.estadisticas.postulantesAprobados / this.estadisticas.totalPostulantesRecibidos) * 100);
  }

  enviarNotifMasiva() {
    if (!this.msgMasivo.trim()) return;
    this.enviandoMasivo = true;
    this.respuestaMasiva = '';
    this.subs.add(
      this.http.post<any>('http://localhost:8080/api/metricas/notificaciones/masiva', {
        mensaje: this.msgMasivo,
        tipo: this.tipoMasivo,
        tipoNotificacion: 'MASIVA_TODOS',
        idRol: null,
        idConvocatoria: null,
      }).subscribe({
        next: (res) => {
          this.respuestaMasiva = res.mensaje;
          this.enviandoMasivo = false;
          this.enviadoMasivo = true;
          this.msgMasivo = '';
          setTimeout(() => { this.enviadoMasivo = false; this.respuestaMasiva = ''; }, 5000);
        },
        error: () => {
          this.respuestaMasiva = 'Error al enviar la notificación.';
          this.enviandoMasivo = false;
        }
      })
    );
  }
}
