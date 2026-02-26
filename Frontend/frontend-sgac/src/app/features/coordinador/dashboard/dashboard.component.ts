import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { CoordinadorResponseDTO } from '../../../core/dto/coordinador';

@Component({
  selector: 'app-coordinador-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  coordinadorService = inject(CoordinadorService);
  authService = inject(AuthService);
  private subs = new Subscription();

  coordinadorData: CoordinadorResponseDTO | null = null;
  totalConvocatoriasCarrera = 0;

  loading = true;
  errorMensaje = '';

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
        next: (coord) => {
          this.coordinadorData = coord;

          this.subs.add(
            this.coordinadorService.listarConvocatoriasPorCarrera(coord.idCarrera).subscribe({
              next: (convocatorias) => {
                // Filter by career ID if the common list is returned
                // We'll filter by name or ID if available. 
                // Looking at ConvocatoriaDTO, it doesn't have idCarrera but it might have it in name.
                // For now we assume if idCarrera is used, we might need to filter manually.
                this.totalConvocatoriasCarrera = convocatorias.length;
                this.loading = false;
              },
              error: () => this.loading = false
            })
          );
        },
        error: (err) => {
          console.error(err);
          this.errorMensaje = 'No tienes permisos de coordinador.';
          this.loading = false;
        }
      })
    );
  }
}
