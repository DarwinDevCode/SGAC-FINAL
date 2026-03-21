import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { DecanoService } from '../../../core/services/decano-service';
import { AuthService } from '../../../core/services/auth-service';
import { DecanoResponseDTO, DecanoEstadisticasDTO } from '../../../core/dto/decano';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType, ChartOptions } from 'chart.js';

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
  private subs = new Subscription();

  decanoData: DecanoResponseDTO | null = null;
  estadisticas: DecanoEstadisticasDTO | null = null;

  loading = true;
  errorMensaje = '';

  // Configuración Chart.js - Actividad por Coordinador (Bar)
  public barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
  };
  public barChartType = 'bar' as const;
  public barChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  // Configuración Chart.js - Estado de Convocatorias (Doughnut)
  public doughnutChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } }
  };
  public doughnutChartType = 'doughnut' as const;
  public doughnutChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };

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
                this.estadisticas = stats;

                // PROTECCIÓN 1: Asegurarnos de que stats exista antes de configurar
                if (stats) {
                  this.configurarGraficos(stats);
                }

                this.loading = false;
              },
              error: () => {
                this.errorMensaje = 'No se pudieron cargar las estadísticas de la facultad.';
                this.loading = false;
              }
            })
          );
        },
        error: (err) => {
          console.error('Error al cargar datos del Decano:', err);
          this.errorMensaje = 'Tu usuario no está registrado como un Decano activo en el sistema.';
          this.loading = false;
        }
      })
    );
  }

  private configurarGraficos(stats: DecanoEstadisticasDTO) {
    // PROTECCIÓN 2: Si el backend envía null, usamos un arreglo vacío '|| []'
    const actividad = stats.actividadPorCoordinador || [];

    // 1. Gráfico de Barras: Actividad por Coordinador
    const labelsCoordinadores = actividad.map(c => c.nombreCoordinador);
    const dataCoordinadores = actividad.map(c => c.totalConvocatorias);

    this.barChartData = {
      labels: labelsCoordinadores,
      datasets: [
        {
          data: dataCoordinadores,
          label: 'Convocatorias',
          backgroundColor: '#3b82f6', // blue-500
          borderRadius: 4,
          hoverBackgroundColor: '#2563eb' // blue-600
        }
      ]
    };

    // PROTECCIÓN 3: Si los números vienen null, usamos '0' por defecto
    const activas = stats.convocatoriasActivas || 0;
    const inactivas = stats.convocatoriasInactivas || 0;

    // 2. Gráfico Doughnut: Estado de Convocatorias
    this.doughnutChartData = {
      labels: ['Activas', 'Inactivas'],
      datasets: [
        {
          data: [activas, inactivas],
          backgroundColor: ['#10b981', '#ef4444'], // green-500, red-500
          hoverBackgroundColor: ['#059669', '#dc2626'] // green-600, red-600
        }
      ]
    };
  }
}
