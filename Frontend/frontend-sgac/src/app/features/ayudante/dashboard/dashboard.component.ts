import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { AyudanteService } from '../../../core/services/ayudante-service';
import { AuthService } from '../../../core/services/auth-service';
import { AyudanteCatedraResponseDTO, AyudantiaResponseDTO } from '../../../core/dto/ayudante';

@Component({
  selector: 'app-ayudante-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  ayudanteService = inject(AyudanteService);
  authService = inject(AuthService);
  private subs = new Subscription();

  ayudanteData: AyudanteCatedraResponseDTO | null = null;
  ayudantiaActiva: AyudantiaResponseDTO | null = null;

  loading = true;
  errorMensaje = '';

  ngOnInit(): void {
    this.cargarDatosAyudante();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cargarDatosAyudante() {
    this.loading = true;
    const user = this.authService.getUser();

    if (!user) {
      this.errorMensaje = 'Sesión no encontrada.';
      this.loading = false;
      return;
    }

    this.subs.add(
      this.ayudanteService.obtenerAyudantePorUsuario(user.idUsuario).subscribe({
        next: (ayudante) => {
          this.ayudanteData = ayudante;
          // Note: In a real scenario, we might need an idPostulacion or active ayudantia ID
          // For now, we'll try to fetch by a generic logic or assume the dashboard shows global hours.
          // Since the AyudanteController has buscarPorPostulacion, we might need to find the user's postulation first.
          // For simplicity in this step, if we don't have idPostulacion, we show profile hours.
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.errorMensaje = 'No se encontró perfil de ayudante para este usuario.';
          this.loading = false;
        }
      })
    );
  }

  get porcentajeHoras(): number {
    if (!this.ayudanteData || !this.ayudanteData.horasAyudante) return 0;
    // Assuming 160 hours total for a standard assistantship (mock value)
    const totalRequerido = 160;
    const porcentaje = (Number(this.ayudanteData.horasAyudante) / totalRequerido) * 100;
    return Math.min(Math.round(porcentaje), 100);
  }
}
