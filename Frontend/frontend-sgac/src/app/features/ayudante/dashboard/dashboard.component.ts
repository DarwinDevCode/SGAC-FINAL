import {Component, OnInit, OnDestroy, inject} from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { RouterModule }                 from '@angular/router';
import { Subject, forkJoin }            from 'rxjs';
import { takeUntil, finalize }          from 'rxjs/operators';
import { LucideAngularModule }          from 'lucide-angular';

import { SesionService } from '../../../core/services/sesion-service';
import { ProgresoGeneral } from '../../../core/dto/progreso-general';
import { ControlSemanal } from '../../../core/dto/control-semanal';
import { SesionListado } from '../../../core/dto/sesion-listado';
import {AuthService} from '../../../core/services/auth-service';
import {HttpErrorResponse} from '@angular/common/http';


@Component({
  selector:    'app-dashboard',
  standalone:  true,
  imports:     [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrls:   ['./dashboard.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();
  authService = inject(AuthService);
  sesionService = inject(SesionService);

  ID_USUARIO_MOCK = this.authService.getUser()?.idUsuario ?? 0;

  cargando = false;
  progreso!:       ProgresoGeneral;
  controlSemanal!: ControlSemanal;
  ultimasSesiones: SesionListado[] = [];
  error: string | null = null;

  ngOnInit(): void {
    this.cargarDashboard();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarDashboard(): void {
    this.cargando = true;
    this.error    = null;

    forkJoin({
      progreso: this.sesionService.progresoGeneral(this.ID_USUARIO_MOCK),
      semanal:  this.sesionService.controlSemanal(this.ID_USUARIO_MOCK),
      sesiones: this.sesionService.listarSesiones(this.ID_USUARIO_MOCK)
    })
      .pipe(takeUntil(this.destroy$), finalize(() => this.cargando = false))
      .subscribe({
        next: ({ progreso, semanal, sesiones }) => {
          this.progreso        = progreso;
          this.controlSemanal  = semanal;
          this.ultimasSesiones = sesiones.slice(0, 5); // top 5
        },
        error: (err: HttpErrorResponse) => {
          console.log(err.error?.data?.message || err.error?.message || err.message || 'Error al cargar');
          this.error = 'No se pudo cargar la información del dashboard. Intente nuevamente.';
        }
      });
  }

  getPorcentajeGlobal(): number {
    return Math.min(this.progreso?.porcentajeAvance ?? 0, 100);
  }

  getPorcentajeSemanal(): number {
    if (!this.controlSemanal) return 0;
    return Math.min(
      (this.controlSemanal.horasRegistradas / this.controlSemanal.limiteSemanal) * 100,
      100
    );
  }

  sesionesConObservacion(): SesionListado[] {
    return this.ultimasSesiones.filter(s => s.tieneObservacion);
  }

  getClaseEstado(estado: string): string {
    const mapa: Record<string, string> = {
      APROBADO:  'aprobado',
      PENDIENTE: 'pendiente',
      OBSERVADO: 'observado',
      RECHAZADO: 'rechazado'
    };
    return mapa[estado] ?? 'pendiente';
  }

  getColorProgreso(): string {
    const p = this.getPorcentajeGlobal();
    if (p >= 75) return 'green';
    if (p >= 40) return 'amber';
    return 'blue';
  }

  getColorSemanal(): string {
    if (this.controlSemanal?.superaLimite) return 'red';
    const p = this.getPorcentajeSemanal();
    if (p >= 85) return 'amber';
    return 'green';
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-EC', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }
}
