import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { DocenteDashboardDTO } from '../../../core/models/docente-dashboard.model';
import { DocenteDashboardService } from '../../../core/services/docente-dashboard.service';

@Component({
  selector: 'app-docente-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, DatePipe],
  templateUrl: './docente-dashboard.component.html',
  styleUrl: './docente-dashboard.component.css'
})
export class DocenteDashboardComponent implements OnInit {
  private readonly dashboardService = inject(DocenteDashboardService);
  private readonly router = inject(Router);

  resumen: DocenteDashboardDTO | null = null;
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.loading = true;
    this.error = null;

    this.dashboardService.getResumen().subscribe({
      next: (data) => {
        this.resumen = data;
        this.loading = false;
      },
      error: (e: Error) => {
        this.error = e.message || 'Error inesperado.';
        this.loading = false;
      }
    });
  }

  revisarActividad(idRegistro: number): void {
    // Ajusta destino a la pantalla de revisión real si ya existe.
    this.router.navigate(['/docente/validar-informes'], { queryParams: { idRegistro } });
  }

  nuevaConvocatoria(): void {
    this.router.navigate(['/docente/convocatorias/nueva']);
  }

  evaluarPostulantes(): void {
    this.router.navigate(['/docente/mis-ayudantes']);
  }

  reportes(): void {
    this.router.navigate(['/docente/reportes']);
  }
}

