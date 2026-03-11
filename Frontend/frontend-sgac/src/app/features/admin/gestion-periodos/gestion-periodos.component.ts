import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormGroup,
  FormControl,
  Validators,
} from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';

import { PeriodoAcademicoService } from '../../../core/services/periodo-academico-service';
import { PeriodoAcademicoDTO }     from '../../../core/dto/periodo-academico';
import { ConfiguracionService }    from '../../../core/services/configuracion/Configuracion';
import {
  PeriodoFaseResponse,
  FaseCronogramaRequest,
  AjusteCronogramaRequest,
} from '../../../core/models/configuracion/Configuracion';

// ─────────────────────────────────────────────────────────────────────────────
// Toast interno — reemplaza p-toast de PrimeNG sin ninguna dependencia externa.
// Cada mensaje lleva un `id` numérico para poder eliminarlo individualmente
// mediante setTimeout (auto-dismiss) o por acción del usuario (×).
// ─────────────────────────────────────────────────────────────────────────────
interface ToastMessage {
  id       : number;
  severity : 'success' | 'error' | 'warn' | 'info';
  summary  : string;
  detail   : string;
}

// ─────────────────────────────────────────────────────────────────────────────
// FaseEditable extiende los datos de PeriodoFaseResponse con campos de
// validación visual. Solo existe en memoria del componente; no viaja al backend.
// ─────────────────────────────────────────────────────────────────────────────
interface FaseEditable {
  idPeriodoFase : number;
  idTipoFase    : number;
  nombreFase    : string;
  orden         : number;
  fechaInicio   : string; // YYYY-MM-DD
  fechaFin      : string; // YYYY-MM-DD
  tieneError    : boolean;
  mensajeError  : string;
}

@Component({
  selector:    'app-gestion-periodos',
  standalone:  true,
  // Sin PrimeNG: solo Angular estándar + Lucide para iconos
  imports:     [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './gestion-periodos.component.html',
  styleUrl:    './gestion-periodos.component.css',
})
export class GestionPeriodosComponent implements OnInit {

  // ── Servicios ──────────────────────────────────────────────────────────────
  private periodoService   = inject(PeriodoAcademicoService);
  private configuracionSvc = inject(ConfiguracionService);

  // ─────────────────────────────────────────────────────────────────────────
  // ESTADO: Sistema de toasts propio
  // ─────────────────────────────────────────────────────────────────────────
  toasts             : ToastMessage[] = [];
  private toastIdCtr = 0;

  // ─────────────────────────────────────────────────────────────────────────
  // ESTADO: Listado principal de periodos (funcionalidad original)
  // ─────────────────────────────────────────────────────────────────────────
  periodos: PeriodoAcademicoDTO[] = [];

  // ─────────────────────────────────────────────────────────────────────────
  // ESTADO: Dialog/stepper de dos pasos
  //   pasoActivo 0 → Apertura del Periodo
  //   pasoActivo 1 → Configurar Cronograma
  // ─────────────────────────────────────────────────────────────────────────
  dialogCreacionVisible = false;
  pasoActivo            = 0;

  // Step 1 — formulario reactivo para abrir el periodo
  formularioPeriodo = new FormGroup({
    nombrePeriodo : new FormControl('', [Validators.required, Validators.minLength(3)]),
    fechaInicio   : new FormControl('', Validators.required),
    fechaFin      : new FormControl('', Validators.required),
  });
  abriendo = false;

  // Contexto del periodo recién creado / seleccionado — usado en Step 2
  periodoNuevoId     : number | null = null;
  periodoNuevoNombre = '';
  periodoRangoInicio = ''; // YYYY-MM-DD — límite inferior para validar fases
  periodoRangoFin    = ''; // YYYY-MM-DD — límite superior

  // Step 2 — fases editables en memoria
  fasesEditables         : FaseEditable[] = [];
  cargandoFases          = false;
  guardandoCronograma    = false;
  tieneErroresCronograma = true; // true hasta que el usuario configure fechas válidas

  // ─────────────────────────────────────────────────────────────────────────
  // ESTADO: Modal de edición de periodo existente (funcionalidad original)
  // ─────────────────────────────────────────────────────────────────────────
  modalEditarAbierto  = false;
  formularioEdicion   : Partial<PeriodoAcademicoDTO> = {};

  // ─────────────────────────────────────────────────────────────────────────
  // ESTADO: Modal de importar requisitos (funcionalidad original)
  // ─────────────────────────────────────────────────────────────────────────
  modalImportarAbierto = false;
  importarConfig = {
    idDestino : null as number | null,
    idFuente  : null as number | null,
  };

  // ═══════════════════════════════════════════════════════════════════════════
  // CICLO DE VIDA
  // ═══════════════════════════════════════════════════════════════════════════
  ngOnInit(): void {
    this.cargarPeriodos();
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // TOASTS — sistema propio, sin dependencia de PrimeNG
  // ═══════════════════════════════════════════════════════════════════════════

  addToast(severity: ToastMessage['severity'], summary: string, detail: string, life = 5000): void {
    const id = ++this.toastIdCtr;
    this.toasts.push({ id, severity, summary, detail });
    setTimeout(() => this.removeToast(id), life);
  }

  removeToast(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  private toastSuccess(detail: string): void { this.addToast('success', 'Éxito', detail, 4000); }
  private toastError  (detail: string): void { this.addToast('error',   'Error', detail, 6000); }
  private toastServer (detail: string): void {
    // Los mensajes de los triggers de PostgreSQL ([SOLAPAMIENTO], [SECUENCIA]…)
    // se muestran con más tiempo porque suelen ser descriptivos y largos.
    this.addToast('error', 'Error de validación del servidor', detail, 9000);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // LISTADO (sin cambios respecto al componente original)
  // ═══════════════════════════════════════════════════════════════════════════
  cargarPeriodos(): void {
    this.periodoService.listarTodos().subscribe({
      next  : (res) => { this.periodos = res; },
      error : ()    => this.toastError('No se pudieron cargar los periodos académicos'),
    });
  }

  cambiarEstado(p: PeriodoAcademicoDTO, activando: boolean): void {
    const op = activando
      ? this.periodoService.activar(p.idPeriodoAcademico!)
      : this.periodoService.desactivar(p.idPeriodoAcademico!);
    op.subscribe({
      next  : () => { this.cargarPeriodos(); this.toastSuccess(activando ? 'Periodo activado' : 'Periodo desactivado'); },
      error : () => this.toastError('No se pudo cambiar el estado del periodo'),
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // MODAL EDICIÓN (funcionalidad original conservada)
  // ═══════════════════════════════════════════════════════════════════════════
  abrirModalEditar(p: PeriodoAcademicoDTO): void {
    if (p.estado === 'INACTIVO') return;
    this.formularioEdicion  = { ...p };
    this.modalEditarAbierto = true;
  }

  cerrarModalEditar(): void { this.modalEditarAbierto = false; }

  guardarEdicion(): void {
    if (!this.formularioEdicion.idPeriodoAcademico) return;
    this.periodoService.actualizar(
      this.formularioEdicion.idPeriodoAcademico,
      this.formularioEdicion as any
    ).subscribe({
      next  : () => { this.toastSuccess('Periodo actualizado exitosamente'); this.cargarPeriodos(); this.cerrarModalEditar(); },
      error : () => this.toastError('No se pudo actualizar el periodo'),
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // FLUJO NUEVO — PASO 1: Apertura de Periodo
  // ═══════════════════════════════════════════════════════════════════════════

  /** Abre el stepper desde cero para crear un NUEVO periodo. */
  abrirDialogNuevoPeriodo(): void {
    this.formularioPeriodo.reset();
    this.fasesEditables        = [];
    this.periodoNuevoId        = null;
    this.periodoNuevoNombre    = '';
    this.pasoActivo            = 0;
    this.dialogCreacionVisible = true;
  }

  /**
   * Abre el stepper en Step 2 para (re-)configurar el cronograma
   * de un periodo ya existente. Resuelve el caso en que el usuario cerró
   * el dialog después de crear el periodo pero antes de configurar las fechas.
   */
  abrirDialogCronograma(p: PeriodoAcademicoDTO): void {
    this.periodoNuevoId        = p.idPeriodoAcademico ?? null;
    this.periodoNuevoNombre    = p.nombrePeriodo;
    this.periodoRangoInicio    = p.fechaInicio as string;
    this.periodoRangoFin       = p.fechaFin    as string;
    this.pasoActivo            = 1;
    this.dialogCreacionVisible = true;
    if (this.periodoNuevoId) this.cargarFases(this.periodoNuevoId);
  }

  cerrarDialogCreacion(): void {
    this.dialogCreacionVisible = false;
    // Refrescamos siempre porque puede existir un periodo PLANIFICACION
    // recién creado que debe aparecer en la tabla aunque el cronograma
    // no esté configurado aún.
    this.cargarPeriodos();
  }

  /** Valida el formulario reactivo y llama a academico.fn_abrir_periodo_academico. */
  confirmarApertura(): void {
    if (this.formularioPeriodo.invalid) {
      this.formularioPeriodo.markAllAsTouched();
      return;
    }
    const { nombrePeriodo, fechaInicio, fechaFin } = this.formularioPeriodo.value;

    if (fechaInicio! >= fechaFin!) {
      this.addToast('warn', 'Fechas inválidas', 'La fecha de inicio debe ser anterior a la fecha de fin');
      return;
    }

    this.abriendo = true;
    this.configuracionSvc.abrirPeriodo({
      nombrePeriodo : nombrePeriodo!,
      fechaInicio   : fechaInicio!,
      fechaFin      : fechaFin!,
    }).subscribe({
      next: (res) => {
        this.abriendo = false;
        if (res.exito && res.id) {
          this.periodoNuevoId     = res.id;
          this.periodoNuevoNombre = nombrePeriodo!;
          this.periodoRangoInicio = fechaInicio!;
          this.periodoRangoFin    = fechaFin!;
          this.toastSuccess('Periodo creado. Configure el cronograma de fases.');
          this.pasoActivo = 1;
          this.cargarFases(res.id);
        } else {
          // Ejemplos de mensaje del backend: "Ya existe un periodo activo que finaliza el..."
          this.toastError(res.mensaje ?? 'Error desconocido al abrir el periodo');
        }
      },
      error: (err) => {
        this.abriendo = false;
        this.toastError(err.message ?? 'Error de conexión con el servidor');
      },
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // FLUJO NUEVO — PASO 2: Configuración de Cronograma
  // ═══════════════════════════════════════════════════════════════════════════

  cargarFases(idPeriodo: number): void {
    this.cargandoFases = true;
    this.configuracionSvc.obtenerCronograma(idPeriodo).subscribe({
      next: (res) => {
        this.cargandoFases = false;
        if (res.exito && res.datos) {
          this.fasesEditables = res.datos.map((f: PeriodoFaseResponse) => ({
            idPeriodoFase : f.idPeriodoFase,
            idTipoFase    : f.idTipoFase,
            nombreFase    : f.nombreFase,
            orden         : f.orden,
            fechaInicio   : f.fechaInicio,
            fechaFin      : f.fechaFin,
            tieneError    : false,
            mensajeError  : '',
          }));
          // Validamos de inmediato para marcar las fechas placeholder
          // como inválidas y guiar al usuario a que las ajuste.
          this.validarCronograma();
        } else {
          this.toastError(res.mensaje ?? 'No se pudieron cargar las fases del periodo');
        }
      },
      error: () => {
        this.cargandoFases = false;
        this.toastError('Error al obtener el cronograma del periodo');
      },
    });
  }

  /**
   * Validación en tiempo real del cronograma. Se llama en cada cambio de
   * fecha. Las reglas se evalúan en orden y el `continue` garantiza que
   * cada fila muestre solo UN mensaje de error (el más prioritario).
   *
   * Reglas en orden de prioridad:
   *   0. Ambas fechas deben estar presentes
   *   1. fechaFin >= fechaInicio (coherencia interna)
   *   2. fechaInicio >= inicio del periodo
   *   3. fechaFin   <= fin del periodo
   *   4. fechaFin de fase N < fechaInicio de fase N+1 (sin solapamiento)
   */
  validarCronograma(): void {
    let hayErrores = false;
    this.fasesEditables.forEach(f => { f.tieneError = false; f.mensajeError = ''; });

    for (let i = 0; i < this.fasesEditables.length; i++) {
      const fase = this.fasesEditables[i];

      if (!fase.fechaInicio || !fase.fechaFin) {
        fase.tieneError = true; fase.mensajeError = 'Las fechas son obligatorias';
        hayErrores = true; continue;
      }
      if (fase.fechaFin < fase.fechaInicio) {
        fase.tieneError = true; fase.mensajeError = 'La fecha de fin no puede ser anterior a la de inicio';
        hayErrores = true; continue;
      }
      if (this.periodoRangoInicio && fase.fechaInicio < this.periodoRangoInicio) {
        fase.tieneError = true;
        fase.mensajeError = `Fuera del rango del periodo (inicio: ${this.formatFecha(this.periodoRangoInicio)})`;
        hayErrores = true; continue;
      }
      if (this.periodoRangoFin && fase.fechaFin > this.periodoRangoFin) {
        fase.tieneError = true;
        fase.mensajeError = `Fuera del rango del periodo (fin: ${this.formatFecha(this.periodoRangoFin)})`;
        hayErrores = true; continue;
      }
      if (i < this.fasesEditables.length - 1) {
        const sig = this.fasesEditables[i + 1];
        if (sig.fechaInicio && fase.fechaFin >= sig.fechaInicio) {
          fase.tieneError = true;
          fase.mensajeError = `Se solapa con "${sig.nombreFase}" (inicia el ${this.formatFecha(sig.fechaInicio)})`;
          hayErrores = true;
        }
      }
    }
    this.tieneErroresCronograma = hayErrores;
  }

  /** Construye el payload y llama a planificacion.fn_ajustar_cronograma_lote. */
  guardarCronograma(): void {
    this.validarCronograma();
    if (this.tieneErroresCronograma || !this.periodoNuevoId) return;

    const payload: AjusteCronogramaRequest = {
      idPeriodo : this.periodoNuevoId,
      fases     : this.fasesEditables.map(f => ({
        idTipoFase  : f.idTipoFase,
        fechaInicio : f.fechaInicio,
        fechaFin    : f.fechaFin,
      } as FaseCronogramaRequest)),
    };

    this.guardandoCronograma = true;
    this.configuracionSvc.guardarCronograma(payload).subscribe({
      next: (res) => {
        this.guardandoCronograma = false;
        if (res.exito) {
          this.addToast('success', '¡Cronograma configurado!',
            `El periodo "${this.periodoNuevoNombre}" está ahora en estado CONFIGURADO.`, 5000);
          this.cerrarDialogCreacion();
        } else {
          // Mensaje exacto del trigger PostgreSQL: [SOLAPAMIENTO]..., [SECUENCIA]...
          this.toastServer(res.mensaje ?? 'El cronograma no pasó las validaciones del sistema');
        }
      },
      error: (err) => {
        this.guardandoCronograma = false;
        this.toastError(err.message ?? 'Error al guardar el cronograma');
      },
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // MODAL IMPORTAR REQUISITOS (funcionalidad original conservada)
  // ═══════════════════════════════════════════════════════════════════════════
  abrirModalImportar(idDestino: number): void {
    this.importarConfig = { idDestino, idFuente: null };
    this.modalImportarAbierto = true;
  }
  cerrarModalImportar(): void { this.modalImportarAbierto = false; }
  confirmarImportacion(): void {
    const { idDestino, idFuente } = this.importarConfig;
    if (!idDestino || !idFuente) return;
    this.periodoService.importarRequisitos(idDestino, idFuente).subscribe({
      next  : (res: any) => { this.toastSuccess(res.mensaje ?? 'Requisitos importados correctamente'); this.cerrarModalImportar(); },
      error : (e)        => this.toastError(e.error?.error ?? 'Error al importar requisitos'),
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // HELPERS
  // ═══════════════════════════════════════════════════════════════════════════

  getEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      'PLANIFICACION' : 'planificacion',
      'CONFIGURADO'   : 'configurado',
      'EN PROCESO'    : 'proceso',
      'INACTIVO'      : 'inactivo',
      'PLANIFICADO'   : 'planificado',
    };
    return map[estado] ?? 'inactivo';
  }

  puedeConfigurarCronograma(p: PeriodoAcademicoDTO): boolean {
    return p.estado === 'PLANIFICACION' || p.estado === 'CONFIGURADO';
  }

  get nombrePeriodoInvalido(): boolean {
    const ctrl = this.formularioPeriodo.get('nombrePeriodo');
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  private formatFecha(fecha: string): string {
    if (!fecha || fecha.length < 10) return fecha;
    const [y, m, d] = fecha.split('-');
    return `${d}/${m}/${y}`;
  }
}
