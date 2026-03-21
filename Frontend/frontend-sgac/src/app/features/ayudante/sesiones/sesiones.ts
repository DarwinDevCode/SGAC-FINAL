import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { finalize } from 'rxjs/operators';
import {
  FormBuilder, FormControl, FormGroup, FormsModule,
  ReactiveFormsModule, Validators,
} from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService }       from '../../../core/services/auth-service';
import { SesionService }     from '../../../core/services/sesion-service';
import { AsistenciaService } from '../../../core/services/asistencia-service';
import { CatalogosService }  from '../../../core/services/catalogos-service';

import {
  SesionResponseDTO,
  EvidenciaResponseDTO,
  PlanificarSesionRequest,
  CompletarSesionRequest,
  EvaluarSesionRequest,
  AsistenciaItem,
  EvidenciaMetadata,
} from '../../../core/models/Sesiones';
import { TipoEvidencia } from '../../../core/dto/tipo-evidencia';

type EvidenciaForm = {
  idTipoEvidencia:         FormControl<number>;
  nombreArchivoReferencia: FormControl<string>;
};

type PlanificarForm = {
  fecha:       FormControl<string>;
  horaInicio:  FormControl<string>;
  horaFin:     FormControl<string>;
  lugar:       FormControl<string>;
  temaTratado: FormControl<string>;
};

type CompletarForm = {
  descripcionActividad: FormControl<string>;
};

const EXTENSIONES: Record<string, number> = {
  '.pdf':  20,
  '.jpg':  21,
  '.docx': 22,
  '.png':  23,
  '.xlsx': 24,
  '.pptx': 25
};

@Component({
  selector: 'app-sesiones',
  standalone: true,
  imports: [CommonModule, NgClass, LucideAngularModule, ReactiveFormsModule, FormsModule],
  templateUrl: './sesiones.html',
  styleUrls:   ['./sesiones.css'],
})
export class SesionesComponent implements OnInit {

  private auth      = inject(AuthService);
  private svc       = inject(SesionService);
  private asistSvc  = inject(AsistenciaService);
  private catalogos = inject(CatalogosService);
  private fb        = inject(FormBuilder);
  private router    = inject(Router);

  sesiones:           SesionResponseDTO[] = [];
  sesionSeleccionada: SesionResponseDTO | null = null;
  idAyudante:         number | null = null;
  tiposEvidencia:     TipoEvidencia[] = [];
  hoy = new Date().toISOString().split('T')[0]; // "YYYY-MM-DD"

  isLoading       = true;
  isDetailLoading = false;
  errorMessage    = '';

  filtroEstado = '';
  filtroDesde  = '';
  filtroHasta  = '';

  isModalPlanificarOpen = false;
  isPlanning  = false;
  planError   = '';

  formPlanificar: FormGroup<PlanificarForm> = this.fb.group({
    fecha:       this.fb.nonNullable.control('', [Validators.required]),
    horaInicio:  this.fb.nonNullable.control('', [Validators.required]),
    horaFin:     this.fb.nonNullable.control('', [Validators.required]),
    lugar:       this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(255)]),
    temaTratado: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(255)]),
  }) as FormGroup<PlanificarForm>;

  isModalCompletarOpen = false;
  isCompleting         = false;
  completarError       = '';
  sesionACompletar:    SesionResponseDTO | null = null;

  asistenciasCompletar: { idParticipanteAyudantia: number; nombreCompleto: string; asistio: boolean }[] = [];
  cargandoAsistencias  = false;
  archivosCompletar:   File[] = [];

  formCompletar: FormGroup<CompletarForm> = this.fb.group({
    descripcionActividad: this.fb.nonNullable.control('', [
      Validators.required,
    ]),
  }) as FormGroup<CompletarForm>;

  evidenciasCompletarFA = this.fb.array<FormGroup<EvidenciaForm>>([]);

  isModalEvaluarOpen = false;
  isEvaluating       = false;
  evaluarError       = '';
  sesionAEvaluar:    SesionResponseDTO | null = null;

  formEvaluar = this.fb.group({
    codigoEstado:  this.fb.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^(APROBADO|RECHAZADO)$/),
    ]),
    observaciones: this.fb.nonNullable.control(''),
  });

  toast = signal<{ texto: string; tipo: 'ok' | 'err' | 'warn' } | null>(null);
  private toastTimer?: ReturnType<typeof setTimeout>;

  rolActual = '';

  ngOnInit(): void {
    const user = this.auth.getUser();
    if (!user) { this.errorMessage = 'Sesión no válida.'; this.isLoading = false; return; }
    this.idAyudante = user.idUsuario;
    this.rolActual  = user.rolActual ?? '';

    this.catalogos.getTiposEvidencia().subscribe({
      next: (tipos: TipoEvidencia[]) => this.tiposEvidencia = tipos ?? [],
    });

    this.cargarSesiones();
  }

  cargarSesiones(): void {
    if (this.idAyudante == null) return;
    this.isLoading    = true;
    this.errorMessage = '';

    this.svc.listarMisSesiones(
      this.idAyudante,
      this.filtroDesde  || undefined,
      this.filtroHasta  || undefined,
      this.filtroEstado || undefined
    ).pipe(finalize(() => this.isLoading = false)).subscribe({
      next:  (data: SesionResponseDTO[]) => this.sesiones = data ?? [],
      error: ()                          => this.errorMessage = 'No se pudieron cargar las sesiones.',
    });
  }

  aplicarFiltros(): void { this.cargarSesiones(); }
  limpiarFiltros(): void {
    this.filtroEstado = ''; this.filtroDesde = ''; this.filtroHasta = '';
    this.cargarSesiones();
  }

  verDetalle(idRegistro: number): void {
    if (this.idAyudante == null) return;
    if (this.sesionSeleccionada?.idRegistroActividad === idRegistro) {
      this.sesionSeleccionada = null; return;
    }
    this.isDetailLoading = true;
    const base = this.sesiones.find(s => s.idRegistroActividad === idRegistro) ?? null;

    this.svc.obtenerDetalleMiSesion(this.idAyudante, idRegistro)
      .pipe(finalize(() => this.isDetailLoading = false))
      .subscribe({
        next: (detalle: SesionResponseDTO) => this.sesionSeleccionada = {
          ...(base as SesionResponseDTO),
          evidencias: detalle?.evidencias ?? [],
        },
      });
  }

  cerrarDetalle(): void { this.sesionSeleccionada = null; }

  // ─── Modal 1: Planificar ────────────────────────────────────────────────

  abrirModalPlanificar(): void {
    this.planError = '';
    this.formPlanificar.reset({ fecha: '', horaInicio: '', horaFin: '', lugar: '', temaTratado: '' });
    this.isModalPlanificarOpen = true;
  }

  cerrarModalPlanificar(): void { this.isModalPlanificarOpen = false; }

  submitPlanificar(): void {
    if (this.formPlanificar.invalid || !this.idAyudante || this.isPlanning) return;
    this.planError  = '';
    this.isPlanning = true;

    const v = this.formPlanificar.getRawValue();

    if (v.horaFin <= v.horaInicio) {
      this.planError  = 'La hora de fin debe ser posterior a la de inicio.';
      this.isPlanning = false;
      return;
    }

    const request: PlanificarSesionRequest = {
      idAyudantia: this.idAyudante,
      fecha:       v.fecha,
      horaInicio:  v.horaInicio + ':00',
      horaFin:     v.horaFin    + ':00',
      lugar:       v.lugar,
      temaTratado: v.temaTratado,
    };

    // CORRECCIÓN TS2339: planificarSesion ahora existe en el servicio actualizado.
    this.svc.planificarSesion(request)
      .pipe(finalize(() => this.isPlanning = false))
      .subscribe({
        // CORRECCIÓN TS7006: tipo explícito en el parámetro del callback
        next:  (res: { exito: boolean; mensaje: string; idRegistroCreado: number }) => {
          this.mostrarToast(res.mensaje ?? 'Sesión planificada. El docente fue notificado.', 'ok');
          this.cerrarModalPlanificar();
          this.cargarSesiones();
        },
        error: (err: { error?: { message?: string } }) => {
          this.planError = err?.error?.message ?? 'Error al planificar la sesión.';
        },
      });
  }

  // ─── Modal 2: Completar ─────────────────────────────────────────────────

  abrirModalCompletar(sesion: SesionResponseDTO): void {
    this.sesionACompletar   = sesion;
    this.completarError     = '';
    this.archivosCompletar  = [];
    this.evidenciasCompletarFA.clear();
    this.asistenciasCompletar = [];
    this.formCompletar.reset({ descripcionActividad: '' });
    this.isModalCompletarOpen = true;

    this.cargandoAsistencias = true;
    this.asistSvc.consultarAsistencia()
      .pipe(finalize(() => this.cargandoAsistencias = false))
      .subscribe({
        next: (lista: { idParticipante: number; nombreCompleto: string; asistio: boolean }[]) => {
          this.asistenciasCompletar = (lista ?? []).map(d => ({
            idParticipanteAyudantia: d.idParticipante,
            nombreCompleto:          d.nombreCompleto,
            asistio:                 d.asistio ?? false,
          }));
        },
        error: () => { },
      });
  }

  cerrarModalCompletar(): void { this.isModalCompletarOpen = false; this.sesionACompletar = null; }

  toggleAsistenciaCompletar(idParticipante: number, valor: boolean): void {
    this.asistenciasCompletar = this.asistenciasCompletar
      .map(a => a.idParticipanteAyudantia === idParticipante ? { ...a, asistio: valor } : a);
  }

  onArchivosCompletar(fileList: FileList | null): void {
    if (!fileList) return;
    this.completarError = '';
    Array.from(fileList).forEach(file => {
      const ext = '.' + (file.name.split('.').pop() ?? '').toLowerCase();
      const idTipo = this.tiposEvidencia
          .find(t => (t.extensionPermitida ?? '').toLowerCase() === ext)?.id
        ?? EXTENSIONES[ext];

      if (!idTipo) {
        this.completarError = `El archivo "${file.name}" no está permitido.`;
        return;
      }
      this.archivosCompletar.push(file);
      this.evidenciasCompletarFA.push(this.fb.group<EvidenciaForm>({
        idTipoEvidencia:         this.fb.nonNullable.control(idTipo,    [Validators.required]),
        nombreArchivoReferencia: this.fb.nonNullable.control(file.name, [Validators.required]),
      }) as FormGroup<EvidenciaForm>);
    });
  }

  quitarArchivoCompletar(idx: number): void {
    this.archivosCompletar.splice(idx, 1);
    this.evidenciasCompletarFA.removeAt(idx);
  }

  get puedeSubmitCompletar(): boolean {
    return this.formCompletar.valid && this.archivosCompletar.length > 0 && !this.isCompleting;
  }

  submitCompletar(): void {
    if (!this.puedeSubmitCompletar || !this.sesionACompletar) return;
    this.completarError = '';
    this.isCompleting   = true;

    const asistencias: AsistenciaItem[] = this.asistenciasCompletar.map(a => ({
      idParticipanteAyudantia: a.idParticipanteAyudantia,
      asistio:                 a.asistio,
    }));

    const metadatos: EvidenciaMetadata[] = this.evidenciasCompletarFA.controls.map(fg => ({
      idTipoEvidencia:         fg.value.idTipoEvidencia ?? 1,
      nombreArchivoReferencia: fg.value.nombreArchivoReferencia ?? '',
    }));

    const request: CompletarSesionRequest = {
      descripcionActividad: this.formCompletar.getRawValue().descripcionActividad,
      asistencias,
      metadatosEvidencias:  metadatos,
    };

    // CORRECCIÓN TS2339: completarSesion ahora existe en el servicio actualizado.
    this.svc.completarSesion(this.sesionACompletar.idRegistroActividad, request, this.archivosCompletar)
      .pipe(finalize(() => this.isCompleting = false))
      .subscribe({
        next:  (res: { exito: boolean; mensaje: string }) => {
          this.mostrarToast(res.mensaje ?? 'Sesión enviada a revisión.', 'ok');
          this.cerrarModalCompletar();
          this.cargarSesiones();
        },
        error: (err: { error?: { message?: string } }) => {
          this.completarError = err?.error?.message ?? 'Error al completar la sesión.';
        },
      });
  }

  // ─── Modal 3: Evaluar (docente) ─────────────────────────────────────────

  abrirModalEvaluar(sesion: SesionResponseDTO): void {
    this.sesionAEvaluar = sesion;
    this.evaluarError   = '';
    this.formEvaluar.reset({ codigoEstado: '', observaciones: '' });
    this.isModalEvaluarOpen = true;
  }

  cerrarModalEvaluar(): void { this.isModalEvaluarOpen = false; this.sesionAEvaluar = null; }

  submitEvaluar(): void {
    if (this.formEvaluar.invalid || !this.sesionAEvaluar || this.isEvaluating) return;

    const v = this.formEvaluar.getRawValue();
    if (v.codigoEstado === 'RECHAZADO' && !v.observaciones.trim()) {
      this.evaluarError = 'Debe indicar el motivo del rechazo.'; return;
    }

    this.evaluarError   = '';
    this.isEvaluating   = true;
    const request: EvaluarSesionRequest = {
      codigoEstado:  v.codigoEstado,
      observaciones: v.observaciones || null,
    };

    // CORRECCIÓN TS2339: evaluarSesion ahora existe en el servicio actualizado.
    this.svc.evaluarSesion(this.sesionAEvaluar.idRegistroActividad, request)
      .pipe(finalize(() => this.isEvaluating = false))
      .subscribe({
        next:  (res: { exito: boolean; mensaje: string }) => {
          this.mostrarToast(res.mensaje ?? 'Evaluación registrada.', 'ok');
          this.cerrarModalEvaluar();
          this.cerrarDetalle();
          this.cargarSesiones();
        },
        error: (err: { error?: { message?: string } }) => {
          this.evaluarError = err?.error?.message ?? 'Error al evaluar la sesión.';
        },
      });
  }

  puedeCompletar(s: SesionResponseDTO): boolean {
    const estado = this.codigoEstado(s);
    return (estado === 'PLANIFICADO' || estado === 'RECHAZADO') && s.fecha <= this.hoy;
  }

  puedeEvaluar(s: SesionResponseDTO): boolean {
    return this.codigoEstado(s) === 'PENDIENTE'
      && ['DOCENTE', 'COORDINADOR', 'ADMINISTRADOR'].includes(this.rolActual.toUpperCase());
  }

  puedeEditarSesion(s: SesionResponseDTO): boolean {
    return this.codigoEstado(s) === 'PLANIFICADO' && s.fecha > this.hoy;
  }

  puedeEditarEvidencia(ev: EvidenciaResponseDTO): boolean {
    return ev.codigoEstadoEvidencia === 'OBSERVADO';
  }

  codigoEstado = (s: SesionResponseDTO): string =>
    (s.codigoEstado ?? s.nombreEstado ?? '').toUpperCase();

  statusClass(s: SesionResponseDTO): string {
    switch (this.codigoEstado(s)) {
      case 'PLANIFICADO': return 'status-planificado';
      case 'PENDIENTE':   return 'status-pendiente';
      case 'APROBADO':    return 'status-aprobado';
      case 'RECHAZADO':   return 'status-rechazado';
      default:            return 'status-default';
    }
  }

  statusClassEv(ev: EvidenciaResponseDTO): string {
    switch ((ev.codigoEstadoEvidencia ?? '').toUpperCase()) {
      case 'APROBADO':  return 'status-aprobado';
      case 'OBSERVADO': return 'status-observado';
      case 'RECHAZADO': return 'status-rechazado';
      default:          return 'status-default';
    }
  }

  esImagen    = (ev: EvidenciaResponseDTO): boolean => (ev.mimeType ?? '').startsWith('image/');
  iconoEv     = (ev: EvidenciaResponseDTO): string  => ev.mimeType?.includes('pdf') ? 'file-text' : 'image';
  tooltipObs  = (): string                          => 'Plazo de 24 h para corregir';
  horaCorta   = (t: string | null): string          => t ? t.substring(0, 5) : '—';
  trackById   = (_: number, s: SesionResponseDTO)   => s.idRegistroActividad;

  // CORRECCIÓN TS2322: window.open retorna Window | null, no void.
  // Envolver en una función que no retorna nada resuelve el error.
  abrirEv(ev: EvidenciaResponseDTO): void {
    window.open(ev.rutaArchivo, '_blank');
  }

  // CORRECCIÓN TS2322: router.navigate retorna Promise<boolean>, no void.
  // Declararla como función normal (no arrow con : void) resuelve el conflicto.
  irAAsistencia(): void {
    void this.router.navigate(['/ayudante/actividades/asistencia']);
  }

  private mostrarToast(texto: string, tipo: 'ok' | 'err' | 'warn'): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toast.set({ texto, tipo });
    this.toastTimer = setTimeout(() => this.toast.set(null), 4500);
  }
}
