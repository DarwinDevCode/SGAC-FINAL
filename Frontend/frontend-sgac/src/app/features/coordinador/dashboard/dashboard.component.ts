import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType, ChartOptions } from 'chart.js';
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

  // P10 — Panel de notificaciones masivas
  msgMasivo = '';
  tipoMasivo = 'CONVOCATORIA';
  enviadoMasivo = false;
  enviandoMasivo = false;
  respuestaMasiva = '';

  // Configuración Chart.js - Estado de Convocatorias (Doughnut)
  public doughnutChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } }
  };
  public doughnutChartType = 'doughnut' as const;
  public doughnutChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };

  // Configuración Chart.js - Postulantes por Convocatoria (Bar)
  public barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
  };
  public barChartType = 'bar' as const;
  public barChartData: ChartData<'bar'> = { labels: [], datasets: [] };

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
                this.estadisticas = stats;
                this.configurarGraficos(stats);
                this.loading = false;
              },
              error: (err) => {
                console.error('Error al cargar estadísticas del Coordinador:', err);
                this.errorMensaje = 'No se pudieron cargar las métricas de tu dashboard.';
                this.loading = false;
              }
            })
          );
        },
        error: (err) => {
          console.error('Error al cargar datos del Coordinador:', err);
          this.errorMensaje = 'Tu usuario no está registrado como un Coordinador activo.';
          this.loading = false;
        }
      })
    );
  }

  private configurarGraficos(stats: CoordinadorEstadisticasDTO) {
    // 1. Gráfico de Barras: Top Convocatorias por número de postulantes
    const labelsBarras = stats.postulantesPorConvocatoria.map(p => p.tituloConvocatoria);
    const dataBarras = stats.postulantesPorConvocatoria.map(p => p.cantidadPostulantes);

    this.barChartData = {
      labels: labelsBarras,
      datasets: [
        {
          data: dataBarras,
          label: 'Postulantes',
          backgroundColor: '#3b82f6', // blue-500
          borderRadius: 4,
          hoverBackgroundColor: '#2563eb' // blue-600
        }
      ]
    };

    // 2. Gráfico Doughnut: Estado de Convocatorias Propias
    this.doughnutChartData = {
      labels: ['Activas', 'Inactivas'],
      datasets: [
        {
          data: [stats.convocatoriasActivas, stats.convocatoriasInactivas],
          backgroundColor: ['#10b981', '#ef4444'], // green-500, red-500
          hoverBackgroundColor: ['#059669', '#dc2626'] // green-600, red-600
        }
      ]
    };
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
