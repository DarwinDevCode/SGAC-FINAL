import {
  Component, OnInit, OnDestroy, inject, signal, computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { SesionService } from '../../../core/services/sesion-service';
import { SesionResponseDTO } from '../../../core/models/Sesiones';
import { PlanificarSesionDialogComponent } from '../planificar-sesion-dialog-component/planificar-sesion-dialog-component';

@Component({
  selector: 'app-listado-sesiones',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule],
  templateUrl: './listado-sesiones-component.html',
  styleUrl: './listado-sesiones-component.css',
})
export class ListadoSesionesComponent implements OnInit, OnDestroy {

  private router = inject(Router);
  private sesionService = inject(SesionService);
  private dialog = inject(MatDialog);
  private destroy$ = new Subject<void>();

  // Estado Principal
  cargando = signal(true);
  errorMsg = signal<string | null>(null);
  sesiones = signal<SesionResponseDTO[]>([]);
  textoBusqueda = signal('');

  // ESTADO PARA EL OFFCANVAS (Panel Lateral)
  offcanvasAbierto = signal(false);
  sesionSeleccionadaId = signal<number | null>(null);
  cargandoDetalle = signal(false);
  detalleSesionRO = signal<SesionResponseDTO | null>(null); // RO = Read Only

  // Computed
  sesioneFiltradas = computed(() => {
    const texto = this.textoBusqueda().toLowerCase().trim();
    if (!texto) return this.sesiones();
    return this.sesiones().filter(s =>
      s.temaTratado?.toLowerCase().includes(texto) ||
      s.lugar?.toLowerCase().includes(texto)
    );
  });

  totalSesiones = computed(() => this.sesiones().length);
  sesionesActivas = computed(() =>
    this.sesiones().filter(s => s.codigoEstado === 'PLANIFICADO' || s.codigoEstado === 'RECHAZADO').length
  );
  resumenHoras = computed(() => {
    return this.sesiones().reduce((sum, s) => sum + (s.horasDedicadas || 0), 0);
  });

  ngOnInit(): void {
    this.cargarSesiones();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarSesiones(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);

    this.sesionService
      .listarMisSesiones()
      .pipe(takeUntil(this.destroy$), finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (data) => {
          const ordenadas = (data || []).sort((a, b) =>
            new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
          );
          this.sesiones.set(ordenadas);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(err?.error?.message ?? 'No se pudo cargar las sesiones.');
        },
      });
  }

  abrirPlanificar(): void {
    const dialogRef = this.dialog.open(PlanificarSesionDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      panelClass: 'sesion-dialog-container',
      disableClose: false,
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) this.cargarSesiones();
    });
  }

  gestionarClicSesion(sesion: SesionResponseDTO): void {
    if (this.puedoEditar(sesion)) {
      this.router.navigate(['/ayudante/sesiones/detalle', sesion.idRegistroActividad]);
    } else {
      this.sesionSeleccionadaId.set(sesion.idRegistroActividad);
      this.offcanvasAbierto.set(true);
      this.cargarDetalleSoloLectura(sesion.idRegistroActividad);
    }
  }

  cargarDetalleSoloLectura(idRegistro: number): void {
    this.cargandoDetalle.set(true);
    this.detalleSesionRO.set(null);
    // Nota: El 0 es representativo si el backend toma el ID del token JWT
    this.sesionService.obtenerDetalleMiSesion(0, idRegistro)
      .pipe(takeUntil(this.destroy$), finalize(() => this.cargandoDetalle.set(false)))
      .subscribe({
        next: (data) => this.detalleSesionRO.set(data),
        error: (err: HttpErrorResponse) => console.error('Error cargando detalle', err)
      });
  }

  cerrarOffcanvas(): void {
    this.offcanvasAbierto.set(false);
    this.sesionSeleccionadaId.set(null);
  }

  formatearFecha(iso: string): string {
    if (!iso) return '—';
    const fecha = new Date(iso + 'T00:00:00');
    return fecha.toLocaleDateString('es-EC', {
      weekday: 'short', day: '2-digit', month: 'short', year: 'numeric'
    });
  }

  formatearHora(iso: string | null): string {
    return iso ? iso.substring(0, 5) : '—';
  }

  obtenerEstadoColor(codigo: string): string {
    switch (codigo) {
      case 'PLANIFICADO': return 'status-planificado';
      case 'PENDIENTE':   return 'status-pendiente';
      case 'APROBADO':    return 'status-aprobado';
      case 'RECHAZADO':   return 'status-rechazado';
      default:            return 'status-default';
    }
  }

  obtenerEstadoLabel(codigo: string): string {
    switch (codigo) {
      case 'PLANIFICADO': return 'Planificada';
      case 'PENDIENTE':   return 'En Revisión';
      case 'APROBADO':    return 'Aprobada';
      case 'RECHAZADO':   return 'Corrección Solicitada';
      default:            return 'Desconocido';
    }
  }

  puedoEditar(sesion: SesionResponseDTO): boolean {
    return sesion.codigoEstado === 'PLANIFICADO' || sesion.codigoEstado === 'RECHAZADO';
  }

  trackBySesion = (_: number, s: SesionResponseDTO) => s.idRegistroActividad;
}
