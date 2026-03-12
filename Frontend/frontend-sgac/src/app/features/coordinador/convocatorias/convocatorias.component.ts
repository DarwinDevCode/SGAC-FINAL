// src/app/features/coordinador/convocatorias/convocatorias.component.ts
import {
  Component, inject, OnInit, OnDestroy
} from '@angular/core';
import {
  CommonModule
} from '@angular/common';
import {
  FormsModule, ReactiveFormsModule,
  FormBuilder, FormGroup, Validators
} from '@angular/forms';
import { RouterModule }         from '@angular/router';
import { LucideAngularModule }  from 'lucide-angular';
import {
  Subscription, forkJoin, of, catchError
} from 'rxjs';
import { switchMap }            from 'rxjs/operators';

import { ConvocatoriaService }    from '../../../core/services/convocatoria-service';
import { CoordinadorService }     from '../../../core/services/coordinador-service';
import { PeriodoAcademicoService} from '../../../core/services/periodo-academico-service';
import { AuthService }            from '../../../core/services/auth-service';

import {
  ConvocatoriaDTO,
  ConvocatoriaCrearRequest,
  ConvocatoriaActualizarRequest,
  VerificarFaseResponse,
  VerificarPostulantesResponse,
} from '../../../core/models/convocatoria/convocatoria';
import { PeriodoAcademicoDTO }    from '../../../core/dto/periodo-academico';
import { DocenteComboDTO, AsignaturaComboDTO } from '../../../core/models/convocatoria.model';

@Component({
  selector:    'app-coordinador-convocatorias-crud',
  standalone:  true,
  imports:     [CommonModule, FormsModule, ReactiveFormsModule,
    RouterModule, LucideAngularModule],
  templateUrl: './convocatorias.html',
  styleUrl:    './convocatorias.css',
})
export class CoordinadorConvocatoriasComponent implements OnInit, OnDestroy {

  // ── Servicios ────────────────────────────────────────────────────────────
  private readonly convSrv   = inject(ConvocatoriaService);
  private readonly coordSrv  = inject(CoordinadorService);
  private readonly periodoSrv= inject(PeriodoAcademicoService);
  private readonly authSrv   = inject(AuthService);
  private readonly fb        = inject(FormBuilder);
  private readonly subs      = new Subscription();

  // ── Datos ────────────────────────────────────────────────────────────────
  convocatorias:   ConvocatoriaDTO[]    = [];
  periodoActivo:   PeriodoAcademicoDTO | null = null;
  docentes:        DocenteComboDTO[]    = [];
  asignaturas:     AsignaturaComboDTO[] = [];
  periodosMap      = new Map<number, string>();

  // ── UI General ───────────────────────────────────────────────────────────
  loading        = true;
  textoBusqueda  = '';
  toastMensaje   = '';
  toastTipo: 'success' | 'error' = 'success';
  private toastTimer: any;

  // ── Fase / contexto ──────────────────────────────────────────────────────
  /** Resultado de verificar-fase; se consulta al abrir el formulario */
  faseInfo:        VerificarFaseResponse | null = null;
  verificandoFase  = false;

  // ── Modal Crear / Editar ─────────────────────────────────────────────────
  mostrarModal = false;
  modoEdicion  = false;
  loadingForm  = false;
  editId:      number | null = null;

  /** PARCIAL: solo cupos/estado. COMPLETA: cambia docente/asignatura */
  tipoEdicion: 'PARCIAL' | 'COMPLETA' = 'PARCIAL';
  checkPostResult: VerificarPostulantesResponse | null = null;

  form: FormGroup = this.fb.group({
    idDocente:        [null, Validators.required],
    idAsignatura:     [{ value: null, disabled: true }, Validators.required],
    cuposDisponibles: [1,   [Validators.required, Validators.min(1)]],
    estado:           ['ABIERTA', Validators.required],
  });

  readonly estadoOpciones = ['ABIERTA', 'CERRADA', 'EN_EVALUACION', 'RESUELTA'];

  // ── Modal Desactivar ─────────────────────────────────────────────────────
  mostrarModalDesactivar = false;
  convDesactivar: ConvocatoriaDTO | null = null;
  desactivando   = false;

  // ── Computed ─────────────────────────────────────────────────────────────
  get convocatoriasFiltradas(): ConvocatoriaDTO[] {
    const t = this.textoBusqueda.toLowerCase().trim();
    if (!t) return this.convocatorias;
    return this.convocatorias.filter(c =>
      (c.nombreAsignatura || '').toLowerCase().includes(t) ||
      (c.nombreDocente    || '').toLowerCase().includes(t)
    );
  }

  getPeriodoNombre(id?: number): string {
    if (!id) return '—';
    return this.periodosMap.get(id) || String(id);
  }

  // ── Ciclo de vida ────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.cargarDatos();
    this.cargarDocentes();
  }

  ngOnDestroy(): void { this.subs.unsubscribe(); }

  // ── Carga inicial ────────────────────────────────────────────────────────
  cargarDatos(): void {
    this.loading = true;
    const user = this.authSrv.getUser();
    if (!user) { this.loading = false; return; }

    this.subs.add(
      forkJoin({
        coordinador:  this.coordSrv.obtenerCoordinadorPorUsuario(user.idUsuario),
        periodoActivo: this.periodoSrv.obtenerActivo()
          .pipe(catchError(() => of(null))),
      }).pipe(
        switchMap(({ coordinador, periodoActivo }) => {
          this.periodoActivo = periodoActivo as PeriodoAcademicoDTO | null;
          if (periodoActivo?.idPeriodoAcademico) {
            this.periodosMap.set(
              periodoActivo.idPeriodoAcademico,
              periodoActivo.nombrePeriodo
            );
          }
          return this.convSrv.getAll().pipe(catchError(() => of([])));
        })
      ).subscribe({
        next: (convs) => {
          this.convocatorias = convs as ConvocatoriaDTO[];
          this.loading = false;
        },
        error: () => {
          this.showToast('Error al cargar los datos.', 'error');
          this.loading = false;
        },
      })
    );
  }

  cargarDocentes(): void {
    this.subs.add(
      this.convSrv.getDocentesCarrera()
        .pipe(catchError(() => of([])))
        .subscribe(d => this.docentes = d as DocenteComboDTO[])
    );
  }

  // ── Cambio de docente ────────────────────────────────────────────────────
  onDocenteChange(event: Event): void {
    const id = +(event.target as HTMLSelectElement).value || null;
    this.form.patchValue({ idDocente: id, idAsignatura: null });
    this.asignaturas = [];

    if (!id) { this.form.get('idAsignatura')?.disable(); return; }
    this.form.get('idAsignatura')?.enable();

    this.subs.add(
      this.convSrv.getAsignaturasPorDocente(id)
        .pipe(catchError(() => of([])))
        .subscribe(a => this.asignaturas = a as AsignaturaComboDTO[])
    );
  }

  // ── Modal Crear ──────────────────────────────────────────────────────────
  abrirModalCrear(): void {
    this.modoEdicion  = false;
    this.editId       = null;
    this.tipoEdicion  = 'COMPLETA';   // crear siempre requiere todos los campos
    this.checkPostResult = null;
    this.asignaturas  = [];
    this.form.reset({ cuposDisponibles: 1, estado: 'ABIERTA',
      idDocente: null, idAsignatura: null });
    this.form.get('idAsignatura')?.disable();
    this.mostrarModal = true;
    this.consultarFase();
  }

  // ── Modal Editar ─────────────────────────────────────────────────────────
  abrirModalEditar(conv: ConvocatoriaDTO): void {
    this.modoEdicion  = true;
    this.editId       = conv.idConvocatoria ?? null;
    this.checkPostResult = null;
    this.mostrarModal = true;

    this.form.patchValue({
      idDocente:        conv.idDocente        ?? null,
      idAsignatura:     conv.idAsignatura     ?? null,
      cuposDisponibles: conv.cuposDisponibles ?? 1,
      estado:           conv.estado           ?? 'ABIERTA',
    });

    if (conv.idDocente) {
      this.form.get('idAsignatura')?.enable();
      this.subs.add(
        this.convSrv.getAsignaturasPorDocente(conv.idDocente)
          .pipe(catchError(() => of([])))
          .subscribe(a => this.asignaturas = a as AsignaturaComboDTO[])
      );
    } else {
      this.form.get('idAsignatura')?.disable();
      this.asignaturas = [];
    }

    // 1. Verificar postulantes para definir tipoEdicion
    if (this.editId) {
      this.subs.add(
        this.convSrv.checkPostulantes(this.editId)
          .pipe(catchError(() => of(null)))
          .subscribe(res => {
            this.checkPostResult = res as VerificarPostulantesResponse | null;
            this.tipoEdicion     = res?.tienePostulantes ? 'PARCIAL' : 'COMPLETA';
            if (this.tipoEdicion === 'PARCIAL') {
              // Bloquear campos que no se pueden cambiar con postulantes
              this.form.get('idDocente')?.disable();
              this.form.get('idAsignatura')?.disable();
            } else {
              this.form.get('idDocente')?.enable();
            }
          })
      );
    }

    // 2. Consultar fase igualmente (para mostrar el banner informativo)
    this.consultarFase();
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.faseInfo     = null;
    this.form.get('idDocente')?.enable();
  }

  // ── Verificar fase del cronograma ────────────────────────────────────────
  private consultarFase(): void {
    this.verificandoFase = true;
    this.subs.add(
      this.convSrv.verificarFase()
        .pipe(catchError(() => of(null)))
        .subscribe(res => {
          this.faseInfo        = res as VerificarFaseResponse | null;
          this.verificandoFase = false;
        })
    );
  }

  // ── Guardar (crear o actualizar) ─────────────────────────────────────────
  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loadingForm = true;
    const raw = this.form.getRawValue();

    if (!this.modoEdicion) {
      // ── CREAR ────────────────────────────────────────────────────────────
      const payload: ConvocatoriaCrearRequest = {
        idAsignatura:     raw.idAsignatura,
        idDocente:        raw.idDocente,
        cuposDisponibles: raw.cuposDisponibles,
        estado:           raw.estado,
      };
      this.subs.add(
        this.convSrv.guardar(payload).subscribe({
          next:  (res) => { this.onGuardadoExitoso(res.mensaje); },
          error: (err) => { this.onGuardadoError(err); },
        })
      );
    } else {
      // ── ACTUALIZAR ───────────────────────────────────────────────────────
      const payload: ConvocatoriaActualizarRequest = {
        idConvocatoria:   this.editId!,
        tipoEdicion:      this.tipoEdicion,
        cuposDisponibles: raw.cuposDisponibles,
        estado:           raw.estado,
        ...(this.tipoEdicion === 'COMPLETA' && {
          idDocente:    raw.idDocente,
          idAsignatura: raw.idAsignatura,
        }),
      };
      this.subs.add(
        this.convSrv.actualizar(payload).subscribe({
          next:  (res) => { this.onGuardadoExitoso(res.mensaje); },
          error: (err) => { this.onGuardadoError(err); },
        })
      );
    }
  }

  private onGuardadoExitoso(msg: string): void {
    this.showToast(msg, 'success');
    this.cerrarModal();
    this.loadingForm = false;
    this.cargarDatos();
  }

  private onGuardadoError(err: Error): void {
    this.showToast(err.message, 'error');
    this.loadingForm = false;
  }

  // ── Desactivar ───────────────────────────────────────────────────────────
  abrirModalDesactivar(conv: ConvocatoriaDTO): void {
    this.convDesactivar = conv;
    this.mostrarModalDesactivar = true;
  }

  cerrarModalDesactivar(): void {
    this.mostrarModalDesactivar = false;
    this.convDesactivar = null;
  }

  confirmarDesactivar(): void {
    if (!this.convDesactivar?.idConvocatoria) return;
    this.desactivando = true;
    this.subs.add(
      this.convSrv.desactivar(this.convDesactivar.idConvocatoria).subscribe({
        next:  (res) => {
          this.showToast(res.mensaje, 'success');
          this.cerrarModalDesactivar();
          this.desactivando = false;
          this.cargarDatos();
        },
        error: (err) => {
          this.showToast(err.message, 'error');
          this.desactivando = false;
          this.cerrarModalDesactivar();
        },
      })
    );
  }

  // ── Toast interno ────────────────────────────────────────────────────────
  showToast(msg: string, tipo: 'success' | 'error'): void {
    clearTimeout(this.toastTimer);
    this.toastMensaje = msg;
    this.toastTipo    = tipo;
    this.toastTimer   = setTimeout(() => this.toastMensaje = '', tipo === 'error' ? 9000 : 4000);
  }
}
