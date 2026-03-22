import {
  Component, OnInit, OnDestroy, inject, signal, computed
} from '@angular/core';
import { CommonModule }  from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';

import { AsistenciaService } from '../../../core/services/ayudantia/asistencia-service';
import { Participante,
  FilaPreview,
  DetalleAsistencia,
  PreviewResponse } from '../../../core/models/ayudantia/Asistencia';

type VistaEstado = 'cargando' | 'vacio' | 'preview' | 'activo' | 'error';

interface ToastMsg {
  texto: string;
  tipo:  'ok' | 'err' | 'warn';
}

@Component({
  selector:    'app-asistencia-dinamica',
  standalone:  true,
  imports:     [CommonModule, LucideAngularModule],
  templateUrl: './asistencia-dinamica-component.html',
  styleUrl:    './asistencia-dinamica-component.css'
})
export class AsistenciaDinamicaComponent implements OnInit, OnDestroy {

  private route  = inject(ActivatedRoute);
  private router = inject(Router);
  private svc    = inject(AsistenciaService);
  private destroy$ = new Subject<void>();

  idAyudantia!: number;
  idRegistro!:  number;

  vistaEstado = signal<VistaEstado>('cargando');
  errorMsg    = signal<string>('');
  toast       = signal<ToastMsg | null>(null);
  private toastTimer?: ReturnType<typeof setTimeout>;
  archivoSeleccionado = signal<File | null>(null);
  dragOver            = signal(false);
  procesandoExcel     = signal(false);
  descargandoPlantilla = signal(false);
  previewData    = signal<PreviewResponse | null>(null);
  confirmando    = signal(false);

  previewValido = computed(() => {
    const p = this.previewData();
    return p !== null && p.exito && !p.tieneErrores;
  });

  filasConError = computed(() =>
    this.previewData()?.filas.filter(f => !f.valida).length ?? 0
  );

  participantes     = signal<Participante[]>([]);
  detalleAsistencia = signal<DetalleAsistencia[]>([]);
  guardandoAsist    = signal(false);
  asistenciaGuardada = signal(false);

  presentes = computed(() =>
    this.detalleAsistencia().filter(d => d.asistio).length
  );
  ausentes = computed(() =>
    this.detalleAsistencia().filter(d => !d.asistio).length
  );
  porcentajeAsistencia = computed(() => {
    const total = this.detalleAsistencia().length;
    return total > 0 ? Math.round((this.presentes() / total) * 100) : 0;
  });

  ngOnInit(): void {
    const params = this.route.snapshot.params;
    this.idAyudantia = +params['idAyudantia'];
    this.idRegistro  = +params['idRegistro'];

    if (!this.idAyudantia || !this.idRegistro) {
      this.errorMsg.set('Parámetros de ruta inválidos.');
      this.vistaEstado.set('error');
      return;
    }

    this.cargarEstadoInicial();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.toastTimer) clearTimeout(this.toastTimer);
  }

  private cargarEstadoInicial(): void {
    this.vistaEstado.set('cargando');
    this.svc.consultarParticipantes(this.idAyudantia)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (lista) => {
          this.participantes.set(lista);
          if (lista.length === 0) {
            this.vistaEstado.set('vacio');
          } else {
            this.activarEstadoC();
          }
        },
        error: (err) => {
          this.errorMsg.set(err?.error?.message ?? 'Error al cargar participantes.');
          this.vistaEstado.set('error');
        }
      });
  }

  private activarEstadoC(): void {
    this.vistaEstado.set('cargando');
    this.svc.inicializarAsistencia(this.idRegistro)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.cargarDetalleAsistencia(),
        error: (err) => {
          this.errorMsg.set(err?.error?.message ?? 'Error al inicializar asistencia.');
          this.vistaEstado.set('error');
        }
      });
  }

  private cargarDetalleAsistencia(): void {
    this.svc.consultarAsistencia(this.idRegistro)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (detalle) => {
          this.detalleAsistencia.set(detalle);
          this.asistenciaGuardada.set(false);
          this.vistaEstado.set('activo');
        },
        error: () => this.vistaEstado.set('activo')
      });
  }

  onDragOver(ev: DragEvent): void {
    ev.preventDefault();
    this.dragOver.set(true);
  }

  onDragLeave(): void {
    this.dragOver.set(false);
  }

  onDrop(ev: DragEvent): void {
    ev.preventDefault();
    this.dragOver.set(false);
    const file = ev.dataTransfer?.files[0];
    if (file) this.procesarArchivo(file);
  }

  onFileInput(ev: Event): void {
    const file = (ev.target as HTMLInputElement).files?.[0];
    if (file) this.procesarArchivo(file);
    (ev.target as HTMLInputElement).value = '';
  }

  private procesarArchivo(file: File): void {
    this.archivoSeleccionado.set(file);
    this.procesandoExcel.set(true);

    this.svc.previewExcel(file)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.procesandoExcel.set(false))
      )
      .subscribe({
        next: (res) => {
          if (!res.exito && !res.filas?.length) {
            // Error de capa 1 o 2: no pasar a preview
            this.mostrarToast(res.mensaje, 'err');
            this.archivoSeleccionado.set(null);
            return;
          }
          this.previewData.set(res);
          this.vistaEstado.set('preview');
        },
        error: (err) => {
          const msg = err?.error?.mensaje ?? 'Error al procesar el archivo.';
          this.mostrarToast(msg, 'err');
          this.archivoSeleccionado.set(null);
        }
      });
  }

  descargarPlantilla(): void {
    this.descargandoPlantilla.set(true);
    this.svc.descargarPlantilla()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.descargandoPlantilla.set(false))
      )
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const a   = Object.assign(document.createElement('a'), {
            href: url, download: 'plantilla_participantes.xlsx'
          });
          a.click();
          URL.revokeObjectURL(url);
        },
        error: () => this.mostrarToast('No se pudo descargar la plantilla.', 'err')
      });
  }

  volverADropzone(): void {
    this.archivoSeleccionado.set(null);
    this.previewData.set(null);
    this.vistaEstado.set('vacio');
  }

  confirmarCargaMasiva(): void {
    const preview = this.previewData();
    if (!preview || !this.previewValido()) return;

    const participantes = preview.filas.map(f => ({
      nombreCompleto: f.nombreCompleto,
      curso:          f.curso,
      paralelo:       f.paralelo
    }));

    this.confirmando.set(true);
    this.svc.cargarParticipantesMasivo(this.idAyudantia, participantes)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.confirmando.set(false))
      )
      .subscribe({
        next: (res) => {
          if (res.exito) {
            this.mostrarToast(
              `${res.insertados} participante(s) cargados correctamente.`, 'ok');
            this.previewData.set(null);
            this.archivoSeleccionado.set(null);
            this.cargarEstadoInicial();
          } else {
            this.mostrarToast(res.mensaje, 'err');
          }
        },
        error: (err) => {
          this.mostrarToast(
            err?.error?.message ?? 'Error al cargar los participantes.', 'err');
        }
      });
  }

  toggleAsistencia(idParticipante: number, asistio: boolean): void {
    this.detalleAsistencia.update(lista =>
      lista.map(d =>
        d.idParticipante === idParticipante ? { ...d, asistio } : d
      )
    );
    this.asistenciaGuardada.set(false);
  }

  marcarTodos(asistio: boolean): void {
    this.detalleAsistencia.update(lista =>
      lista.map(d => ({ ...d, asistio }))
    );
    this.asistenciaGuardada.set(false);
  }

  guardarAsistencia(): void {
    const payload = this.detalleAsistencia().map(d => ({
      idParticipante: d.idParticipante,
      asistio:        d.asistio
    }));

    this.guardandoAsist.set(true);
    this.svc.guardarAsistencias(this.idRegistro, payload)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.guardandoAsist.set(false))
      )
      .subscribe({
        next: (res) => {
          if (res.exito) {
            this.asistenciaGuardada.set(true);
            this.mostrarToast(
              `Asistencia guardada. ${res.presentes} presente(s) de ${res.total}.`, 'ok');
          } else {
            this.mostrarToast('Error al guardar la asistencia.', 'err');
          }
        },
        error: (err) => {
          this.mostrarToast(
            err?.error?.message ?? 'Error al guardar la asistencia.', 'err');
        }
      });
  }

  trackById(_: number, item: DetalleAsistencia): number {
    return item.idParticipante;
  }

  trackByFila(_: number, item: FilaPreview): number {
    return item.fila;
  }

  formatBytes(bytes: number): string {
    if (bytes < 1024)       return `${bytes} B`;
    if (bytes < 1048576)    return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }

  private mostrarToast(texto: string, tipo: 'ok' | 'err' | 'warn'): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toast.set({ texto, tipo });
    this.toastTimer = setTimeout(() => this.toast.set(null), 4500);
  }

  volver(): void {
    this.router.navigate(['../..'], { relativeTo: this.route });
  }
}
