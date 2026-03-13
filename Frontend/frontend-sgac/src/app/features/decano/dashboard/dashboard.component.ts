import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { DecanoService } from '../../../core/services/decano-service';
import { AuthService } from '../../../core/services/auth-service';
import { InformeMensualService } from '../../../core/services/informe-mensual.service';
import { DecanoResponseDTO } from '../../../core/dto/decano';

@Component({
  selector: 'app-decano-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  decanoService = inject(DecanoService);
  authService = inject(AuthService);
  informeService = inject(InformeMensualService);
  private subs = new Subscription();

  decanoData: DecanoResponseDTO | null = null;
  totalConvocatoriasActivas = 0;
  totalInformesPendientes = 0;

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
      this.errorMensaje = 'No se encontró información de la sesión activa.';
      this.loading = false;
      return;
    }

    // 1. Cargar datos del Decano 
    this.subs.add(
      this.decanoService.obtenerDecanoPorUsuario(user.idUsuario).subscribe({
        next: (decano) => {
          this.decanoData = decano;

          // 2. Cargar indicadores generales
          this.subs.add(
            this.decanoService.listarConvocatoriasActivas().subscribe({
              next: (convocatorias) => {
                this.totalConvocatoriasActivas = convocatorias.length;
              },
              error: () => { }
            })
          );

          this.subs.add(
            this.informeService.listarPendientesDecano().subscribe({
              next: (informes) => {
                this.totalInformesPendientes = informes.length;
                this.loading = false;
              },
              error: () => this.loading = false
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
}
