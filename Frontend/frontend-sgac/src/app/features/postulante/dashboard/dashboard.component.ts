import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PostulanteDashboardService } from '../../../core/services/postulante/postulante-dashboard.service';
import { PostulanteDashboard } from '../../../core/models/postulante/postulante-dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(PostulanteDashboardService);

  dashboard: PostulanteDashboard | null = null;
  loading = true;
  error = false;

  ngOnInit(): void {
    this.cargarDashboard();
  }

  cargarDashboard(): void {
    this.loading = true;
    this.error = false;

    this.dashboardService.getResumenDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar dashboard', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  getEstadoClass(estado: string | undefined): string {
    if (!estado) return 'badge badge-secondary';

    const valor = estado.toUpperCase();

    if (valor.includes('APROB')) return 'badge badge-success';
    if (valor.includes('RECHAZ')) return 'badge badge-danger';
    if (valor.includes('REVISION') || valor.includes('PENDIENTE')) return 'badge badge-warning';
    if (valor.includes('OBSERV')) return 'badge badge-warning';

    return 'badge badge-secondary';
  }

  /*
   * IMPORTANTE:
   * Esta función ahora usa el MISMO formato que la pantalla
   * "Mi Postulación", para que ambas muestren la misma fecha.
   */
  formatearFecha(fecha: string | null | undefined): string {
    if (!fecha) return '--';

    try {
      const date = new Date(fecha);
      return date.toLocaleDateString('es-EC', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    } catch {
      return fecha;
    }
  }
}
