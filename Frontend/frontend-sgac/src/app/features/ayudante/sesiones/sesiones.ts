import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { finalize, switchMap } from 'rxjs/operators';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { of } from 'rxjs';

import { AuthService } from '../../../core/services/auth-service';
import { SesionService } from '../../../core/services/sesion-service';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { AyudanteService } from '../../../core/services/ayudante-service';
import { PostulanteService } from '../../../core/services/postulaciones/postulante-service';

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

const EXTENSIONES_SOPORTADAS: Record<string, number> = {
  '.jpg': 21,
  '.pdf': 20,
  '.docx': 22
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
  private ayudanteService = inject(AyudanteService);
  private postulanteService = inject(PostulanteService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

  sesiones: SesionResponseDTO[] = [];
  sesionSeleccionada: SesionResponseDTO | null = null;
  idAyudantiaReal: number | null = null;

  isLoading = true;
  isDetailLoading = false;
  errorMessage = '';

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
    this.idAyudante = user.idUsuario;

    this.catalogosService.getTiposEvidencia().subscribe({
      next: (tipos) => {
        this.tiposEvidencia = tipos ?? [];
        this.tiposEvidenciaByExt = new Map(
          this.tiposEvidencia.map((t) => [String(t.extensionPermitida || '').toLowerCase(), t])
        );
      }
    });

    this.obtenerContextoAyudantia();
    this.cargarSesiones();
  }

  private obtenerContextoAyudantia() {
    if (!this.idAyudante) return;
    this.ayudanteService.obtenerAyudantePorUsuario(this.idAyudante).pipe(
      switchMap(est => this.postulanteService.misPostulaciones(est.idUsuario)),
      switchMap(posts => {
        const activa = posts.find(p => p.estadoPostulacion === 'SELECCIONADO' || p.estadoPostulacion === 'ACEPTADO' || p.estadoPostulacion === 'APROBADO');
        return activa ? this.ayudanteService.obtenerAyudantiaPorPostulacion(activa.idPostulacion) : of(null);
      })
    ).subscribe(ayud => {
      this.idAyudantiaReal = ayud?.idAyudantia ?? null;
    });
  }

  irAAsistencia(s: SesionResponseDTO) {
    if (!this.idAyudantiaReal) {
      alert('No se pudo determinar el ID de la ayudantía activa.');
      return;
    }
    this.router.navigate(['/ayudante/sesiones', this.idAyudantiaReal, 'asistencia', s.idRegistroActividad]);
  }

  cargarSesiones(): void {
    if (this.idAyudante == null) return;
    this.isLoading = true;
    this.errorMessage = '';
    this.sesionService.listarMisSesiones(this.idAyudante)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (data) => this.sesiones = data ?? [],
        error: () => this.errorMessage = 'No se pudieron cargar tus sesiones.'
      });
  }

  submitRegistro(): void {
    if (this.idAyudante == null || !this.canSubmit) return;
    this.formError = '';
    this.isSubmitting = true;

    // SOLUCIÓN AL ERROR TS2322: Aseguramos que idTipoEvidencia nunca sea undefined
    const evidencias: EvidenciaRequest[] = this.selectedFiles.map((file, i) => {
      const evCtrl = this.evidenciasFA.at(i);
      return {
        // Usamos el operador de aserción no nula o un fallback
        idTipoEvidencia: evCtrl.controls.idTipoEvidencia.value ?? 1,
        nombreArchivo: evCtrl.controls.nombreArchivo.value ?? file.name,
      };
    });

    const request: RegistrarSesionRequest = {
      idAyudantia: this.idAyudantiaReal || 0,
      descripcionActividad: this.form.controls.descripcionActividad.value,
      temaTratado: this.form.controls.temaTratado.value,
      fecha: this.form.controls.fecha.value,
      numeroAsistentes: this.form.controls.numeroAsistentes.value,
      horasDedicadas: this.form.controls.horasDedicadas.value,
      evidencias,
    };

    this.sesionService.registrarSesion(this.idAyudante, request, this.selectedFiles)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: (res) => {
          if (res?.exito) {
            this.cerrarModalRegistro();
            this.cargarSesiones();
          } else {
            this.formError = res?.mensaje || 'Error al registrar.';
          }
        },
        error: () => this.formError = 'Error de conexión.'
      });
  }

  // --- Métodos de UI y Lógica Auxiliar ---
  get evidenciasFA() { return this.form.controls.evidencias; }
  get canSubmit() { return this.form.valid && this.selectedFiles.length > 0 && !this.isSubmitting; }

  verDetalle(idRegistroActividad: number): void {
    if (this.idAyudante == null) return;
    if (this.sesionSeleccionada?.idRegistroActividad === idRegistroActividad) {
      this.sesionSeleccionada = null;
      return;
    }
    this.isDetailLoading = true;
    const base = this.sesiones.find((s) => s.idRegistroActividad === idRegistroActividad) ?? null;
    this.sesionService.obtenerDetalleMiSesion(this.idAyudante, idRegistroActividad)
      .pipe(finalize(() => (this.isDetailLoading = false)))
      .subscribe({
        next: (detalle) => {
          this.sesionSeleccionada = { ...(base as any), evidencias: detalle?.evidencias ?? [] };
        }
      });
  }

  onFilesSelected(fileList: FileList | null): void {
    if (!fileList) return;

    this.formError = '';
    const incoming = Array.from(fileList);

    incoming.forEach((file) => {
      // 1. Extraer extensión
      const ext = '.' + file.name.split('.').pop()?.toLowerCase();

      // 2. Buscar el ID (Corregido a camelCase: extensionPermitida e idTipoEvidencia)
      let idTipo = this.tiposEvidencia.find(t =>
        (t.extensionPermitida || '').toLowerCase() === ext
      )?.id;

      // 3. Fallback a la constante si el catálogo no ha cargado
      if (!idTipo) {
        idTipo = EXTENSIONES_SOPORTADAS[ext];
      }

      // 4. Validación de soporte
      if (!idTipo) {
        this.formError = `El archivo "${file.name}" no es permitido. Solo se aceptan: ${Object.keys(EXTENSIONES_SOPORTADAS).join(', ')}`;
        return;
      }

      // 5. Agregar al formulario y a la lista de archivos
      if (this.selectedFiles.length < this.maxFiles) {
        this.selectedFiles.push(file);

        this.evidenciasFA.push(
          this.fb.group<EvidenciaForm>({
            idTipoEvidencia: this.fb.nonNullable.control(idTipo, [Validators.required]),
            nombreArchivo: this.fb.nonNullable.control(file.name, [Validators.required]),
          }) as FormGroup<EvidenciaForm>
        );
      } else {
        this.formError = `Máximo ${this.maxFiles} archivos permitidos.`;
      }
    });
  }

  /*
  onFilesSelected(fileList: FileList | null): void {
    if (!fileList) return;
    Array.from(fileList).forEach((file) => {
      this.selectedFiles.push(file);
      this.evidenciasFA.push(this.fb.group({
        idTipoEvidencia: [1, Validators.required],
        nombreArchivo: [file.name, Validators.required]
      }) as any);
    });
  }

   */

  removeFile(index: number) { this.selectedFiles.splice(index, 1); this.evidenciasFA.removeAt(index); }
  abrirModalRegistro() { this.isModalOpen = true; this.form.controls.fecha.setValue(new Date().toISOString().split('T')[0]); }
  cerrarModalRegistro() { this.isModalOpen = false; this.form.reset(); this.selectedFiles = []; this.evidenciasFA.clear(); }

  // Helpers de Estado
  private normalizarEstado = (v: any) => String(v ?? '').toUpperCase();
  getEstadoSesion = (s: any) => this.normalizarEstado(s.nombreEstado ?? s.estadoRevision);
  getEstadoEvidencia = (ev: any) => this.normalizarEstado(ev.nombreEstadoEvidencia ?? ev.estadoEvidencia);
  statusClass(estado: string) {
    if (estado === 'PENDIENTE') return 'status-pendiente';
    if (estado === 'APROBADO') return 'status-aprobado';
    if (estado === 'OBSERVADO') return 'status-observado';
    return 'status-default';
  }

  esImagen = (ev: any) => (ev.mimeType || '').startsWith('image/');
  iconoEvidencia = (ev: any) => (ev.mimeType?.includes('pdf') ? 'file-text' : 'image');
  abrirEvidencia = (ev: any) => window.open(ev.rutaArchivo, '_blank');
  cerrarDetalle = () => this.sesionSeleccionada = null;
  tooltipObservado = () => 'Plazo de 24h para corregir';
  puedeEditarSesion = (s: any) => this.getEstadoSesion(s) === 'OBSERVADO';
  puedeEditarEvidencia = (ev: any) => this.getEstadoEvidencia(ev) === 'OBSERVADO';
  trackById = (_: number, s: SesionResponseDTO) => s.idRegistroActividad;
  editarSesion = (s: any) => console.log('Editar', s.idRegistroActividad);
  editarEvidencia = (ev: any) => console.log('Editar ev', ev.idEvidenciaRegistroActividad);
}
