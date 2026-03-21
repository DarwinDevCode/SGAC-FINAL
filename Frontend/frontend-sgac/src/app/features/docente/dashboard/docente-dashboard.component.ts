import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { DocenteDashboardService } from '../../../core/services/docente-dashboard.service';
import { DocenteDashboardDTO, DocenteDashboardUltimaActividadDTO } from '../../../core/models/dashboard/docente-dashboard.model';

@Component({
  selector: 'app-docente-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, RouterModule],
  templateUrl: './docente-dashboard.component.html',
  styleUrl: './docente-dashboard.component.css'
})
export class DocenteDashboardComponent implements OnInit {
  private dashboardService = inject(DocenteDashboardService);
  private router = inject(Router);

  dashboard: DocenteDashboardDTO | null = null;
  loading = true;
  error = '';

  ngOnInit(): void {
    this.dashboardService.getResumen().subscribe({
      next: (d) => { this.dashboard = d; this.loading = false; },
      error: (err) => { this.error = err.message || 'Error al cargar el panel.'; this.loading = false; }
    });
  }

  irA(ruta: string): void {
    this.router.navigate([ruta]);
  }

  getEstadoClass(fecha: string): string {
    const dias = Math.floor((Date.now() - new Date(fecha).getTime()) / 86400000);
    if (dias <= 1) return 'act-recent';
    if (dias <= 5) return 'act-normal';
    return 'act-old';
  }

  getEstadoLabel(fecha: string): string {
    const dias = Math.floor((Date.now() - new Date(fecha).getTime()) / 86400000);
    if (dias === 0) return 'Hoy';
    if (dias === 1) return 'Ayer';
    return `Hace ${dias} días`;
  }

  formatFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-EC', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
