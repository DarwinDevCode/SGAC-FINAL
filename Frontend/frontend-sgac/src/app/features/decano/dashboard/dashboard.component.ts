import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { DecanoService } from '../../../core/services/decano-service';
import { AuthService } from '../../../core/services/auth-service';
import { DecanoResponseDTO, DecanoEstadisticasDTO } from '../../../core/dto/decano';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-decano-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, BaseChartDirective],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  decanoService = inject(DecanoService);
  authService = inject(AuthService);
  router = inject(Router);
  private subs = new Subscription();

  decanoData: DecanoResponseDTO | null = null;
  estadisticas: DecanoEstadisticasDTO | null = null;

  loading = true;
  errorMensaje = '';

  irA(path: string) {
    this.router.navigate([path]);
  }

  // ── Gráfico 1: Barras horizontales — Postulantes por Coordinador ──
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
    plugins: { legend: { position: 'bottom', labels: { font: { size: 11 } } } },
    cutout: '70%'
  };
  public doughnutConvocatoriasType = 'doughnut' as const;
  public doughnutConvocatoriasData: ChartData<'doughnut'> = { labels: [], datasets: [] };

  // ── Gráfico 3: Doughnut — Estado de Postulantes ──
  public doughnutPostulantesOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom', labels: { font: { size: 11 } } } },
    cutout: '70%'
  };
  public doughnutPostulantesType = 'doughnut' as const;
  public doughnutPostulantesData: ChartData<'doughnut'> = { labels: [], datasets: [] };

  ngOnInit(): void { this.cargarDatosDashboard(); }
  ngOnDestroy(): void { this.subs.unsubscribe(); }

  cargarDatosDashboard() {
    this.loading = true;
    const user = this.authService.getUser();

    if (!user) {
      this.errorMensaje = 'No se encontró información de la sesión activa.';
      this.loading = false;
      return;
    }

    this.subs.add(
      this.decanoService.obtenerDecanoPorUsuario(user.idUsuario).subscribe({
        next: (decano) => {
          this.decanoData = decano;
          this.subs.add(
            this.decanoService.obtenerEstadisticasPorFacultad(decano.idFacultad).subscribe({
              next: (stats) => {
                this.estadisticas = {
                  ...stats,
                  totalConvocatorias: stats.totalConvocatorias ?? 0,
                  convocatoriasActivas: stats.convocatoriasActivas ?? 0,
                  convocatoriasInactivas: stats.convocatoriasInactivas ?? 0,
                  totalPostulantes: stats.totalPostulantes ?? 0,
                  postulantesSeleccionados: stats.postulantesSeleccionados ?? 0,
                  postulantesNoSeleccionados: stats.postulantesNoSeleccionados ?? 0,
                  postulantesEnEvaluacion: stats.postulantesEnEvaluacion ?? 0,
                  postulantesPendientes: stats.postulantesPendientes ?? 0,
                  actividadPorCoordinador: stats.actividadPorCoordinador ?? []
                };
                this.configurarGraficos(this.estadisticas);
                this.loading = false;
              },
              error: () => {
                this.errorMensaje = 'No se pudieron cargar las estadísticas de la facultad.';
                this.loading = false;
              }
            })
          );
        },
        error: () => {
          this.errorMensaje = 'Tu usuario no está registrado como un Decano activo en el sistema.';
          this.loading = false;
        }
      })
    );
  }

  private configurarGraficos(stats: DecanoEstadisticasDTO) {
    const actividad = stats.actividadPorCoordinador || [];

    // Gráfico 1: Horizontal bar — postulantes por coordinador
    this.barChartData = {
      labels: actividad.map(c => {
        const n = c.nombreCoordinador || 'Sin Coordinador';
        return n.length > 22 ? n.substring(0, 22) + '…' : n;
      }),
      datasets: [{
        data: actividad.map(c => c.totalConvocatorias),
        label: 'Postulantes',
        backgroundColor: ['#6366f1', '#3b82f6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444'],
        borderRadius: 6,
        hoverBackgroundColor: '#4f46e5'
      }]
    };

    // Gráfico 2: Doughnut — Convocatorias
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
          stats.postulantesSeleccionados,
          stats.postulantesNoSeleccionados,
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

  get tasaSeleccion(): number {
    if (!this.estadisticas || this.estadisticas.totalPostulantes === 0) return 0;
    return Math.round((this.estadisticas.postulantesSeleccionados / this.estadisticas.totalPostulantes) * 100);
  }
}
