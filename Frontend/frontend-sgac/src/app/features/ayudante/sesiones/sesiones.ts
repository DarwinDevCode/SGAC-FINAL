import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { finalize } from 'rxjs/operators';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from '../../../core/services/auth-service';
import { SesionService } from '../../../core/services/sesion-service';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { SesionResponseDTO } from '../../../core/dto/sesion-response-dto';
import { EvidenciaResponseDTO } from '../../../core/dto/evidencia-response-dto';
import { RegistrarSesionRequest } from '../../../core/dto/registrar-sesion-request';
import { EvidenciaRequest } from '../../../core/dto/evidencia-request';
import { TipoEvidencia } from '../../../core/dto/tipo-evidencia';

type EvidenciaForm = {
  idTipoEvidencia: FormControl<number>;
  nombreArchivo: FormControl<string>;
};

type RegistrarSesionForm = {
  descripcionActividad: FormControl<string>;
  temaTratado: FormControl<string>;
  fecha: FormControl<string>;
  numeroAsistentes: FormControl<number>;
  horasDedicadas: FormControl<number>;
  evidencias: FormArray<FormGroup<EvidenciaForm>>;
};

@Component({
  selector: 'app-sesiones',
  standalone: true,
  imports: [CommonModule, NgClass, LucideAngularModule, ReactiveFormsModule],
  templateUrl: './sesiones.html',
  styleUrls: ['./sesiones.css'],
})
export class SesionesComponent implements OnInit {
  private authService = inject(AuthService);
  private sesionService = inject(SesionService);
  private catalogosService = inject(CatalogosService);
  private fb = inject(FormBuilder);

  sesiones: SesionResponseDTO[] = [];
  sesionSeleccionada: SesionResponseDTO | null = null;

  isLoading = true;
  isDetailLoading = false;
  errorMessage = '';

  // Modal registro
  isModalOpen = false;
  isSubmitting = false;
  formError = '';
  selectedFiles: File[] = [];

  readonly maxFiles = 5;

  form: FormGroup<RegistrarSesionForm> = this.fb.group({
    descripcionActividad: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)]),
    temaTratado: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(255)]),
    fecha: this.fb.nonNullable.control('', [Validators.required]),
    numeroAsistentes: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
    horasDedicadas: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0.1)]),
    evidencias: this.fb.array<FormGroup<EvidenciaForm>>([]),
  }) as FormGroup<RegistrarSesionForm>;

  private idAyudante: number | null = null;

  tiposEvidencia: TipoEvidencia[] = [];
  private tiposEvidenciaByExt = new Map<string, TipoEvidencia>();

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (!user) {
      this.isLoading = false;
      this.errorMessage = 'No se pudo identificar al usuario actual.';
      return;
    }

    // Asunción del proyecto: idAyudante coincide con idUsuario autenticado.
    this.idAyudante = user.idUsuario;

    // Catálogo de tipos de evidencia (para validar extensiones permitidas)
    this.catalogosService.getTiposEvidencia().subscribe({
      next: (tipos) => {
        this.tiposEvidencia = tipos ?? [];
        this.tiposEvidenciaByExt = new Map(
          this.tiposEvidencia.map((t) => [String(t.extensionPermitida || '').toLowerCase(), t])
        );
      },
      error: () => {
        // Si falla el catálogo, igual dejamos registrar pero sin validación por extensión.
        this.tiposEvidencia = [];
        this.tiposEvidenciaByExt = new Map();
      },
    });

    this.cargarSesiones();
  }

  private extensionDeArchivo(file: File): string {
    const name = file.name || '';
    const idx = name.lastIndexOf('.');
    if (idx < 0) return '';
    return name.substring(idx + 1).toLowerCase();
  }

  private tipoEvidenciaParaArchivo(file: File): TipoEvidencia | null {
    const ext = this.extensionDeArchivo(file);
    if (!ext) return null;
    return this.tiposEvidenciaByExt.get(ext) ?? null;
  }

  get evidenciasFA(): FormArray<FormGroup<EvidenciaForm>> {
    return this.form.controls.evidencias;
  }

  get canSubmit(): boolean {
    return this.form.valid && this.selectedFiles.length > 0 && !this.isSubmitting;
  }

  abrirModalRegistro(): void {
    this.isModalOpen = true;
    this.formError = '';

    // Pre-carga: fecha de hoy (yyyy-MM-dd)
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    this.form.controls.fecha.setValue(`${yyyy}-${mm}-${dd}`);
  }

  cerrarModalRegistro(): void {
    if (this.isSubmitting) return;
    this.isModalOpen = false;
    this.formError = '';
    this.form.reset();
    // Mantener estructura tipada: reponer defaults
    this.form.controls.numeroAsistentes.setValue(0);
    this.form.controls.horasDedicadas.setValue(0);
    this.selectedFiles = [];
    this.evidenciasFA.clear();
  }

  cargarSesiones(): void {
    if (this.idAyudante == null) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.sesionService
      .listarMisSesiones(this.idAyudante)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (data) => {
          this.sesiones = data ?? [];
          if (
            this.sesionSeleccionada &&
            !this.sesiones.some(
              (s) => s.idRegistroActividad === this.sesionSeleccionada?.idRegistroActividad
            )
          ) {
            this.sesionSeleccionada = null;
          }
        },
        error: () => {
          this.errorMessage = 'No se pudieron cargar tus sesiones. Intenta nuevamente.';
          this.sesiones = [];
        },
      });
  }

  // --- Estados / badges ---
  private normalizarEstado(valor: string | undefined | null): string {
    return String(valor ?? '')
      .trim()
      .toUpperCase();
  }

  getEstadoSesion(s: SesionResponseDTO): string {
    // Backend nuevo: nombreEstado. Compat: estadoRevision.
    return this.normalizarEstado(s.nombreEstado ?? s.estadoRevision);
  }

  getEstadoEvidencia(ev: EvidenciaResponseDTO): string {
    // Backend nuevo: nombreEstadoEvidencia. Compat: estadoEvidencia.
    return this.normalizarEstado(ev.nombreEstadoEvidencia ?? ev.estadoEvidencia);
  }

  statusClass(estado: string | undefined | null): string {
    const e = this.normalizarEstado(estado);
    if (e === 'PENDIENTE') return 'status-pendiente';
    if (e === 'APROBADO') return 'status-aprobado';
    if (e === 'OBSERVADO') return 'status-observado';
    if (e === 'RECHAZADO') return 'status-rechazado';
    return 'status-default';
  }

  /**
   * Regla: puede editarse si está OBSERVADO y se está dentro de las 24h desde fechaObservacion.
   */
  dentroPlazo24Horas(fechaObservacionIso?: string | null): boolean {
    if (!fechaObservacionIso) return false;

    // fechaObservacion viene como LocalDate (yyyy-MM-dd). Interpretamos inicio de día local.
    const d = new Date(fechaObservacionIso);
    if (Number.isNaN(d.getTime())) return false;

    const ahora = Date.now();
    const diffMs = ahora - d.getTime();
    const horas = diffMs / (1000 * 60 * 60);

    return horas >= 0 && horas <= 24;
  }

  puedeEditarSesion(s: SesionResponseDTO): boolean {
    return this.getEstadoSesion(s) === 'OBSERVADO' && this.dentroPlazo24Horas(s.fechaObservacion);
  }

  puedeEditarEvidencia(ev: EvidenciaResponseDTO): boolean {
    return this.getEstadoEvidencia(ev) === 'OBSERVADO' && this.dentroPlazo24Horas(ev.fechaObservacion);
  }

  tooltipObservado(): string {
    return 'Tienes 24 horas para corregir este registro desde la fecha de observación';
  }

  verDetalle(idRegistroActividad: number): void {
    if (this.idAyudante == null) return;

    if (this.sesionSeleccionada?.idRegistroActividad === idRegistroActividad) {
      this.sesionSeleccionada = null;
      return;
    }

    this.isDetailLoading = true;
    this.errorMessage = '';

    // Mantener datos del listado (cabecera) y solo inyectar evidencias desde el endpoint.
    const base = this.sesiones.find((s) => s.idRegistroActividad === idRegistroActividad) ?? null;

    this.sesionService
      .obtenerDetalleMiSesion(this.idAyudante, idRegistroActividad)
      .pipe(finalize(() => (this.isDetailLoading = false)))
      .subscribe({
        next: (detalle) => {
          const evidencias = detalle?.evidencias ?? [];
          this.sesionSeleccionada = {
            ...(base ?? { idRegistroActividad }),
            evidencias,
          };
        },
        error: () => {
          this.errorMessage = 'No se pudo cargar el detalle de la sesión seleccionada.';
        },
      });
  }

  // Acciones de edición (placeholder UI)
  editarSesion(s: SesionResponseDTO): void {
    // Aquí puedes abrir un modal de edición o navegar a otra pantalla.
    // Por ahora, solo mantenemos el hook para la UI.
    console.log('Editar sesión', s.idRegistroActividad);
  }

  editarEvidencia(ev: EvidenciaResponseDTO): void {
    console.log('Editar evidencia', ev.idEvidenciaRegistroActividad);
  }

  onFilesSelected(fileList: FileList | null): void {
    if (!fileList) return;

    this.formError = '';

    const incoming = Array.from(fileList);
    const available = this.maxFiles - this.selectedFiles.length;

    if (available <= 0) {
      this.formError = `Solo puedes adjuntar hasta ${this.maxFiles} archivos.`;
      return;
    }

    const toAdd = incoming.slice(0, available);
    if (incoming.length > available) {
      this.formError = `Solo se agregaron ${available} archivo(s). Máximo permitido: ${this.maxFiles}.`;
    }

    // Validación cliente: si ya cargamos catálogo, filtramos solo extensiones permitidas
    const invalidFiles: string[] = [];

    // Validación cliente: si ya cargamos catálogo, filtramos solo extensiones permitidas
    toAdd.forEach((file) => {
      const tipo = this.tiposEvidencia.length > 0 ? this.tipoEvidenciaParaArchivo(file) : null;

      if (this.tiposEvidencia.length > 0 && !tipo) {
        invalidFiles.push(file.name);
        return;
      }

      this.selectedFiles.push(file);

      this.evidenciasFA.push(
        this.fb.group<EvidenciaForm>({
          idTipoEvidencia: this.fb.nonNullable.control(tipo?.id ?? 1, [Validators.required, Validators.min(1)]),
          nombreArchivo: this.fb.nonNullable.control(file.name, [Validators.required, Validators.maxLength(150)]),
        }) as FormGroup<EvidenciaForm>
      );
    });

    if (invalidFiles.length > 0) {
      this.formError = `Archivo(s) no permitido(s): ${invalidFiles.join(', ')}.`;
    }
  }

  removeFile(index: number): void {
    if (index < 0 || index >= this.selectedFiles.length) return;

    // Mantener sincronía: borrar en ambos arreglos por índice
    this.selectedFiles.splice(index, 1);
    this.evidenciasFA.removeAt(index);
  }

  submitRegistro(): void {
    if (this.idAyudante == null) return;

    this.formError = '';

    // Validación cliente: no llamar si no hay archivos
    if (this.selectedFiles.length === 0) {
      this.formError = 'Adjunta al menos 1 evidencia.';
      return;
    }

    if (this.selectedFiles.length > this.maxFiles) {
      this.formError = `Máximo permitido: ${this.maxFiles} archivos.`;
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.formError = 'Revisa los campos requeridos.';
      return;
    }

    // 1) Evidencias (sin ruta/mime/tamaño) - sincronizadas por índice con selectedFiles
    const evidencias: EvidenciaRequest[] = this.selectedFiles.map((file, i) => {
      const evCtrl = this.evidenciasFA.at(i);
      const idTipo = evCtrl?.controls.idTipoEvidencia.value ?? 1;
      const nombre = evCtrl?.controls.nombreArchivo.value ?? file.name;

      return {
        idTipoEvidencia: idTipo,
        nombreArchivo: nombre,
      };
    });

    if (evidencias.length === 0) {
      this.formError = 'Debe existir al menos 1 evidencia.';
      return;
    }

    // 2) Request completo (backend exige idAyudantia + evidencias)
    const request: RegistrarSesionRequest = {
      // Placeholder: el backend debe recalcularlo desde idUsuario. Evita @NotNull.
      idAyudantia: 0,
      descripcionActividad: this.form.controls.descripcionActividad.value,
      temaTratado: this.form.controls.temaTratado.value,
      fecha: this.form.controls.fecha.value,
      numeroAsistentes: this.form.controls.numeroAsistentes.value,
      horasDedicadas: this.form.controls.horasDedicadas.value,
      evidencias,
    };

    // Chequeo final de coherencia índice files <-> evidencias
    if (request.evidencias.length !== this.selectedFiles.length) {
      this.formError = 'Error interno: evidencias y archivos no coinciden.';
      return;
    }

    this.isSubmitting = true;

    this.sesionService
      .registrarSesion(this.idAyudante, request, this.selectedFiles)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: (res) => {
          if (!res?.exito) {
            this.formError = res?.mensaje || 'No se pudo registrar la sesión.';
            return;
          }

          this.cerrarModalRegistro();
          this.cargarSesiones();
        },
        error: (err) => {
          this.formError = err?.error?.message || err?.message || 'No se pudo registrar la sesión. Intenta nuevamente.';
          // Emitimos alerta en pantalla (AlertService fallback básico)
          if (this.formError.includes('20 horas') || this.formError.toLowerCase().includes('evidencia')) {
            alert(this.formError);
          }
        },
      });
  }

  private normalizarUrlEvidencia(url: string): string {
    const u = (url || '').trim();
    if (!u) return u;

    // Cloudinary: los PDFs suelen servirse correctamente como raw/upload.
    // Si vienen como image/upload (por resource_type auto), el visor puede fallar.
    const isCloudinary = u.includes('res.cloudinary.com/');
    const isPdf = u.toLowerCase().includes('.pdf');

    if (isCloudinary && isPdf) {
      return u.replace('/image/upload/', '/raw/upload/');
    }

    return u;
  }

  abrirEvidencia(ev: EvidenciaResponseDTO): void {
    const urlOriginal = (ev?.rutaArchivo || '').trim();
    if (!urlOriginal) {
      this.errorMessage = 'La evidencia no tiene una URL válida.';
      return;
    }

    const url = this.normalizarUrlEvidencia(urlOriginal);

    // Caso 1: si el recurso está bajo nuestro backend, el interceptor puede adjuntar Authorization
    // y además podemos abrirlo en nueva pestaña como URL normal.
    // Si es un recurso externo (ej Cloudinary) y está protegido, abrir directo fallará con 401.
    try {
      const target = new URL(url, window.location.origin);
      const isSameOrigin = target.origin === window.location.origin;
      const isBackend = target.origin.includes('localhost:8080') || target.pathname.includes('/api/');

      if (isSameOrigin || isBackend) {
        window.open(target.toString(), '_blank', 'noopener');
        return;
      }

      // Caso 2: URL externa (Cloudinary u otro CDN).
      window.open(target.toString(), '_blank', 'noopener');
    } catch {
      window.open(url, '_blank', 'noopener');
    }
  }

  trackById = (_: number, s: SesionResponseDTO): number => s.idRegistroActividad;

  esImagen(ev: EvidenciaResponseDTO): boolean {
    return (ev.mimeType || '').toLowerCase().startsWith('image/');
  }

  iconoEvidencia(ev: EvidenciaResponseDTO): string {
    const mime = (ev.mimeType || '').toLowerCase();
    if (mime.includes('pdf')) return 'file-text';
    if (mime.includes('word')) return 'file-text';
    if (mime.includes('excel') || mime.includes('spreadsheet')) return 'file-spreadsheet';
    if (mime.includes('zip') || mime.includes('rar')) return 'file-archive';
    if (this.esImagen(ev)) return 'image';
    return 'file';
  }

  cerrarDetalle(): void {
    this.sesionSeleccionada = null;
  }
}
