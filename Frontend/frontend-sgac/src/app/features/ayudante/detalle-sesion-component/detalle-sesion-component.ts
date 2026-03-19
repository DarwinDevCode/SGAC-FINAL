import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize, debounceTime, distinctUntilChanged } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { AyudantiaService } from '../../../core/services/ayudantia/ayudantia-service';
import {
  AsistenciaSesionActualResponseDTO, EstudianteAsistenciaDTO, BorradorSesionResponseDTO, EvidenciaResponseDTO,
} from '../../../core/models/ayudantia/sesiones';
import { MarcadoAsistenciaRequestDTO, FinalizarSesionRequestDTO } from '../../../core/models/ayudantia/asistencia';

type PestanaActiva = 'asistencia' | 'borrador' | 'evidencias';

@Component({
  selector: 'app-detalle-sesion',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './detalle-sesion-component.html',
  styleUrl: './detalle-sesion-component.css',
})
export class DetalleSesionComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ayudantiaService = inject(AyudantiaService);
  private destroy$ = new Subject<void>();
  private progreso$ = new Subject<string>();

  idRegistro: number = 0;
  cargando = signal(true);
  errorMsg = signal<string | null>(null);
  pestanaActiva = signal<PestanaActiva>('asistencia');

  sesionInfo = signal<any>(null);
  estudiantes = signal<EstudianteAsistenciaDTO[]>([]);
  puedeEditar = signal(true);

  asistenciaEnvio = signal(false);
  asistenciaError = signal<string | null>(null);

  borradorCargando = signal(false);
  borradorError = signal<string | null>(null);
  descripcion = signal('');
  evidencias = signal<EvidenciaResponseDTO[]>([]);
  guardarBorradorEnvio = signal(false);

  subiendeArchivo = signal(false);
  subirArchivoError = signal<string | null>(null);
  finalizando = signal(false);
  finalizarError = signal<string | null>(null);

  totalAsistencias = computed(() => this.estudiantes().length);
  asistentesPresentes = computed(() => this.estudiantes().filter(e => e.asistio).length);
  porcentajeAsistencia = computed(() => {
    const total = this.totalAsistencias();
    return total > 0 ? Math.round((this.asistentesPresentes() / total) * 100) : 0;
  });

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.idRegistro = parseInt(id, 10);
        this.cargarSesion();
      }
    });

    // UX: Se dispara el guardado 1s después de que deje de escribir
    this.progreso$
      .pipe(debounceTime(1000), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((desc) => {
        if (this.puedeEditar()) this.guardarProgreso(desc);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarSesion(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);
    this.ayudantiaService
      // @ts-ignore
      .obtenerSesionActual(this.idRegistro)
      .pipe(takeUntil(this.destroy$), finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (res) => {
          if (res.valido && res.datos) {
            const data = res.datos as AsistenciaSesionActualResponseDTO;
            this.sesionInfo.set(data.sesion);
            this.estudiantes.set(data.estudiantes);
            this.puedeEditar.set(data.sesion.puedeEditar);
            this.cargarBorrador();
          } else {
            this.errorMsg.set(res.mensaje ?? 'No se pudo cargar la sesión.');
          }
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(err?.error?.message ?? 'Error al cargar la sesión.');
        },
      });
  }

  cargarBorrador(): void {
    this.borradorCargando.set(true);
    this.borradorError.set(null);
    this.ayudantiaService.obtenerBorrador(this.idRegistro)
      .pipe(takeUntil(this.destroy$), finalize(() => this.borradorCargando.set(false)))
      .subscribe({
        next: (res) => {
          if (res.valido && res.datos) {
            const data = res.datos as BorradorSesionResponseDTO;
            this.descripcion.set(data.detalle.descripcionActual || '');
            this.evidencias.set(data.evidencias || []);
          } else {
            this.borradorError.set(res.mensaje);
          }
        },
        error: (err: HttpErrorResponse) => {
          this.borradorError.set(err?.error?.message ?? 'No se pudo cargar el borrador.');
        },
      });
  }

  marcarAsistencia(estudiante: EstudianteAsistenciaDTO, asistio: boolean): void {
    if (!this.puedeEditar()) return;
    this.asistenciaError.set(null);
    this.asistenciaEnvio.set(true);
    const request: MarcadoAsistenciaRequestDTO = { idDetalle: estudiante.idDetalle, asistio };

    // Actualización optimista
    this.actualizarAsistenciaLocal(estudiante.idDetalle, asistio);

    this.ayudantiaService.marcarAsistencia(request)
      .pipe(takeUntil(this.destroy$), finalize(() => this.asistenciaEnvio.set(false)))
      .subscribe({
        next: (res) => {
          if (!res.valido) {
            this.asistenciaError.set(res.mensaje);
            this.actualizarAsistenciaLocal(estudiante.idDetalle, !asistio);
          }
        },
        error: (err: HttpErrorResponse) => {
          this.asistenciaError.set(err?.error?.message ?? 'Error al marcar asistencia.');
          this.actualizarAsistenciaLocal(estudiante.idDetalle, !asistio);
        },
      });
  }

  private actualizarAsistenciaLocal(idDetalle: number, asistio: boolean): void {
    const idx = this.estudiantes().findIndex(e => e.idDetalle === idDetalle);
    if (idx >= 0) {
      const updated = [...this.estudiantes()];
      updated[idx] = { ...updated[idx], asistio };
      this.estudiantes.set(updated);
    }
  }

  alCambiarDescripcion(evento: Event): void {
    const valor = (evento.target as HTMLTextAreaElement).value;
    this.descripcion.set(valor);
    this.progreso$.next(valor);
  }

  guardarProgreso(descripcion: string): void {
    this.guardarBorradorEnvio.set(true);
    this.ayudantiaService.guardarProgreso(this.idRegistro, descripcion)
      .pipe(takeUntil(this.destroy$), finalize(() => this.guardarBorradorEnvio.set(false)))
      .subscribe({
        next: (res) => { if (!res.valido) console.warn('No se pudo guardar:', res.mensaje); },
        error: (err: HttpErrorResponse) => console.warn('Error al guardar progreso:', err?.error?.message),
      });
  }

  subirArchivo(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;
    this.subirArchivoError.set(null);
    this.subiendeArchivo.set(true);

    this.ayudantiaService.cargarEvidencia(this.idRegistro, 1, archivo)
      .pipe(takeUntil(this.destroy$), finalize(() => this.subiendeArchivo.set(false)))
      .subscribe({
        next: (res) => {
          if (res.valido) {
            this.cargarBorrador();
            input.value = '';
          } else {
            this.subirArchivoError.set(res.mensaje);
          }
        },
        error: (err: HttpErrorResponse) => {
          this.subirArchivoError.set(err?.error?.message ?? 'Error al subir el archivo.');
        },
      });
  }

  eliminarEvidencia(idEvidencia: number): void {
    if (!confirm('¿Deseas eliminar esta evidencia?')) return;
    this.ayudantiaService.eliminarEvidencia(idEvidencia).pipe(takeUntil(this.destroy$)).subscribe({
      next: (res) => {
        if (res.valido) this.evidencias.set(this.evidencias().filter(e => e.idEvidencia !== idEvidencia));
      },
      error: (err: HttpErrorResponse) => console.error('Error al eliminar evidencia:', err),
    });
  }

  finalizarSesion(): void {
    if (!confirm('¿Estás seguro de que deseas finalizar esta sesión? No podrás editarla después.')) return;
    this.finalizarError.set(null);
    this.finalizando.set(true);
    const request: FinalizarSesionRequestDTO = { idRegistro: this.idRegistro, descripcion: this.descripcion() };

    this.ayudantiaService.finalizarSesion(request)
      .pipe(takeUntil(this.destroy$), finalize(() => this.finalizando.set(false)))
      .subscribe({
        next: (res) => {
          if (res.valido) this.router.navigate(['/ayudante/sesiones']);
          else this.finalizarError.set(res.mensaje);
        },
        error: (err: HttpErrorResponse) => this.finalizarError.set(err?.error?.message ?? 'Error al finalizar.'),
      });
  }

  volver(): void { this.router.navigate(['/ayudante/sesiones']); }
  cambiarPestana(pestaña: PestanaActiva): void { this.pestanaActiva.set(pestaña); }
  formatearFecha(iso: string): string {
    return iso ? new Date(iso + 'T00:00:00').toLocaleDateString('es-EC', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }) : '—';
  }
  formatearHora(iso: string | null): string { return iso ? iso.substring(0, 5) : '—'; }
  obtenerColorBarra(pct: number): string { return pct >= 75 ? 'progress-fill-verde' : pct >= 50 ? 'progress-fill-amarillo' : 'progress-fill-rojo'; }
  obtenerTamanoArchivo(bytes: number): string {
    if (bytes === 0) return '0 B';
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round((bytes / Math.pow(1024, i)) * 10) / 10 + ' ' + ['B', 'KB', 'MB'][i];
  }
  trackByEstudiante = (_: number, e: EstudianteAsistenciaDTO) => e.idDetalle;
  trackByEvidencia = (_: number, e: EvidenciaResponseDTO) => e.idEvidencia;
}
