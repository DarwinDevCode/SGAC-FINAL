import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { CoordinadorResponseDTO } from '../../../core/dto/coordinador';

@Component({
  selector: 'app-coordinador-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  coordinadorService = inject(CoordinadorService);
  authService = inject(AuthService);
  http = inject(HttpClient);
  private subs = new Subscription();

  coordinadorData: CoordinadorResponseDTO | null = null;

  // KPIs reales del SP
  kpis: any = null;

  loading = true;
  errorMensaje = '';

  // P10 — Panel de notificaciones masivas
  msgMasivo = '';
  tipoMasivo = 'CONVOCATORIA';
  enviadoMasivo = false;
  enviandoMasivo = false;
  respuestaMasiva = '';

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
      this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario).pipe(
        switchMap((coord: any) => {
          this.coordinadorData = coord;
          return this.http.get<any>(`http://localhost:8080/api/metricas/coordinador?idCarrera=${coord.idCarrera}`);
        })
      ).subscribe({
        next: (kpis) => {
          this.kpis = kpis;
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.errorMensaje = 'Error al cargar KPIs del dashboard.';
          this.loading = false;
        }
      })
    );
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
