import {
  Component, OnInit, OnDestroy, inject, signal, computed
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';

import { AsistenciaService } from '../../../core/services/asistencia-service';
import {
  ContextoAsistencia,
  Participante,
  FilaPreview,
  DetalleAsistencia,
  PreviewResponse,
} from '../../../core/models/Asistencia';

type VistaEstado = 'cargando' | 'vacio' | 'preview' | 'activo' | 'error';

interface ToastMsg {
  texto: string;
  tipo: 'ok' | 'err' | 'warn';
}

@Component({
  selector: 'app-asistencia-dinamica',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './asistencia-dinamica-component.html',
  styleUrl: './asistencia-dinamica-component.css',
})
export class AsistenciaDinamicaComponent implements OnInit, OnDestroy {

  private router   = inject(Router);
  private svc      = inject(AsistenciaService);
  private destroy$ = new Subject<void>();
  vistaEstado = signal<VistaEstado>('cargando');
  errorMsg    = signal<string>('');
  toast       = signal<ToastMsg | null>(null);
  private toastTimer?: ReturnType<typeof setTimeout>;
  archivoSeleccionado  = signal<File | null>(null);
  dragOver             = signal(false);
  procesandoExcel      = signal(false);
  descargandoPlantilla = signal(false);
  previewData          = signal<PreviewResponse | null>(null);
  confirmando          = signal(false);
  participantes      = signal<Participante[]>([]);
  detalleAsistencia  = signal<DetalleAsistencia[]>([]);
  guardandoAsist     = signal(false);
  asistenciaGuardada = signal(false);
  previewValido = computed(() => {
    const p = this.previewData();
    return p !== null && !p.tieneErrores && (p.nuevos ?? 0) > 0;
  });

  filasConError = computed(() =>
    this.previewData()?.filas.filter(f => !f.valida).length ?? 0
  );

  filasNuevas = computed(() =>
    this.previewData()?.filas.filter(f => f.valida && !f.yaExiste) ?? []
  );

  filasExistentes = computed(() =>
    this.previewData()?.filas.filter(f => f.yaExiste) ?? []
  );

  presentes = computed(() => this.detalleAsistencia().filter(d => d.asistio).length);
  ausentes  = computed(() => this.detalleAsistencia().filter(d => !d.asistio).length);

  porcentajeAsistencia = computed(() => {
    const total = this.detalleAsistencia().length;
    return total > 0 ? Math.round((this.presentes() / total) * 100) : 0;
  });

  ngOnInit(): void {
    this.inicializarContexto();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.toastTimer) clearTimeout(this.toastTimer);
  }

  private inicializarContexto(): void {
    this.vistaEstado.set('cargando');
    this.svc.obtenerContexto()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (_ctx: ContextoAsistencia) => this.cargarEstadoInicial(),
        error: (err) => {
          this.errorMsg.set(err?.error?.message ?? 'No se encontró una ayudantía activa.');
          this.vistaEstado.set('error');
        },
      });
  }

  private cargarEstadoInicial(): void {
    this.svc.consultarParticipantes()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res: any) => {
          const lista: Participante[] = Array.isArray(res) ? res : (res?.participantes ?? []);
          this.participantes.set(lista);
          lista.length === 0
            ? this.vistaEstado.set('vacio')
            : this.prepararLista();
        },
        error: () => {
          this.errorMsg.set('Error al cargar participantes.');
          this.vistaEstado.set('error');
        },
      });
  }

  private prepararLista(): void {
    this.svc.inicializarAsistencia()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.cargarDetalle(),
        error: () => {
          this.errorMsg.set('No se pudo inicializar la asistencia.');
          this.vistaEstado.set('error');
        },
      });
  }

  private cargarDetalle(): void {
    this.svc.consultarAsistencia()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (detalle) => {
          this.detalleAsistencia.set(Array.isArray(detalle) ? detalle : []);
          this.vistaEstado.set('activo');
        },
      });
  }

  onDragOver(ev: DragEvent): void { ev.preventDefault(); this.dragOver.set(true); }
  onDragLeave(): void              { this.dragOver.set(false); }

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
      .pipe(takeUntil(this.destroy$), finalize(() => this.procesandoExcel.set(false)))
      .subscribe({
        next: (res: any) => {
          if (res?.exito === false && !res.filas) {
            // Error duro (archivo dañado, headers incorrectos, etc.)
            this.mostrarToast(res.mensaje, 'err');
            this.archivoSeleccionado.set(null);
            return;
          }
          this.previewData.set(res);
          this.vistaEstado.set('preview');
        },
        error: () => this.mostrarToast('Error al procesar el archivo.', 'err'),
      });
  }

  volverADropzone(): void {
    this.archivoSeleccionado.set(null);
    this.previewData.set(null);
    this.vistaEstado.set('vacio');
  }

  descargarPlantilla(): void {
    this.descargandoPlantilla.set(true);
    this.svc.descargarPlantilla()
      .pipe(takeUntil(this.destroy$), finalize(() => this.descargandoPlantilla.set(false)))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const a   = Object.assign(document.createElement('a'), {
            href:     url,
            download: 'plantilla_participantes.xlsx',
          });
          a.click();
          URL.revokeObjectURL(url);
        },
      });
  }

  confirmarCargaMasiva(): void {
    if (!this.previewValido()) return;
    const body = this.filasNuevas().map(f => ({
      nombreCompleto: f.nombreCompleto,
      curso:          f.curso,
      paralelo:       f.paralelo,
    }));

    this.confirmando.set(true);
    this.svc.cargarParticipantesMasivo(body)
      .pipe(takeUntil(this.destroy$), finalize(() => this.confirmando.set(false)))
      .subscribe({
        next: (res: any) => {
          if (res?.exito !== false) {
            const msg = res?.mensaje ?? 'Padrón cargado correctamente.';
            this.mostrarToast(msg, 'ok');
            this.previewData.set(null);
            this.archivoSeleccionado.set(null);
            this.cargarEstadoInicial();
          } else {
            this.mostrarToast(res.mensaje, 'err');
          }
        },
        error: (err) =>
          this.mostrarToast(err?.error?.message ?? 'Error al cargar el padrón.', 'err'),
      });
  }

  toggleAsistencia(idParticipante: number, asistio: boolean): void {
    this.detalleAsistencia.update(lista =>
      lista.map(d => d.idParticipante === idParticipante ? { ...d, asistio } : d)
    );
    this.asistenciaGuardada.set(false);
  }

  marcarTodos(asistio: boolean): void {
    this.detalleAsistencia.update(lista => lista.map(d => ({ ...d, asistio })));
    this.asistenciaGuardada.set(false);
  }

  guardarAsistencia(): void {
    const payload = this.detalleAsistencia().map(d => ({
      idParticipante: d.idParticipante,
      asistio:        d.asistio,
    }));
    this.guardandoAsist.set(true);
    this.svc.guardarAsistencias(payload)
      .pipe(takeUntil(this.destroy$), finalize(() => this.guardandoAsist.set(false)))
      .subscribe({
        next: () => {
          this.asistenciaGuardada.set(true);
          this.mostrarToast('Asistencia guardada con éxito.', 'ok');
        },
        error: () => this.mostrarToast('Error al guardar la asistencia.', 'err'),
      });
  }

  volver(): void { this.router.navigate(['/ayudante/sesiones']); }

  verMatriz(): void { this.router.navigate(['/ayudante/asistencia/matriz']); }

  formatBytes(bytes: number): string {
    return bytes < 1_048_576
      ? `${(bytes / 1024).toFixed(1)} KB`
      : `${(bytes / 1_048_576).toFixed(1)} MB`;
  }

  private mostrarToast(texto: string, tipo: 'ok' | 'err' | 'warn'): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toast.set({ texto, tipo });
    this.toastTimer = setTimeout(() => this.toast.set(null), 4500);
  }

  trackById  = (_: number, item: DetalleAsistencia) => item.idParticipante;
  trackByFila = (_: number, item: FilaPreview)       => item.fila;
}
