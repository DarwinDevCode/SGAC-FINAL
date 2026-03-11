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

interface ToastMessage {
  id       : number;
  severity : 'success' | 'error' | 'warn' | 'info';
  summary  : string;
  detail   : string;
}

interface FaseEditable {
  idPeriodoFase : number;
  idTipoFase    : number;
  nombreFase    : string;
  orden         : number;
  fechaInicio   : string;
  fechaFin      : string;
  tieneError    : boolean;
  mensajeError  : string;
}

@Component({
  selector:    'app-gestion-periodos',
  standalone:  true,
  imports:     [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './gestion-periodos.component.html',
  styleUrl:    './gestion-periodos.component.css',
})
export class GestionPeriodosComponent implements OnInit {

  private periodoService   = inject(PeriodoAcademicoService);
  private configuracionSvc = inject(ConfiguracionService);

  toasts             : ToastMessage[] = [];
  private toastIdCtr = 0;

  periodos: PeriodoAcademicoDTO[] = [];

  dialogCreacionVisible = false;
  pasoActivo            = 0;

  formularioPeriodo = new FormGroup({
    nombrePeriodo : new FormControl('', [Validators.required, Validators.minLength(3)]),
    fechaInicio   : new FormControl('', Validators.required),
    fechaFin      : new FormControl('', Validators.required),
  });
  abriendo = false;

  periodoNuevoId     : number | null = null;
  periodoNuevoNombre = '';
  periodoRangoInicio = '';
  periodoRangoFin    = '';

  fasesEditables         : FaseEditable[] = [];
  cargandoFases          = false;
  guardandoCronograma    = false;
  tieneErroresCronograma = true;

  modalEditarAbierto  = false;
  formularioEdicion   : Partial<PeriodoAcademicoDTO> = {};

  modalImportarAbierto = false;
  importarConfig = {
    idDestino : null as number | null,
    idFuente  : null as number | null,
  };

  // ═══════════════════════════════════════════════════════════════════════════
  ngOnInit(): void { this.cargarPeriodos(); }

  // ═══════════════════════════════════════════════════════════════════════════
  // TOASTS
  // ═══════════════════════════════════════════════════════════════════════════
  addToast(severity: ToastMessage['severity'], summary: string, detail: string, life = 5000): void {
    const id = ++this.toastIdCtr;
    this.toasts.push({ id, severity, summary, detail });
    setTimeout(() => this.removeToast(id), life);
  }
  removeToast(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }
  private toastSuccess(detail: string): void { this.addToast('success', 'Éxito', detail, 4000); }
  private toastError  (detail: string): void { this.addToast('error',   'Error', detail, 6000); }
  private toastServer (detail: string): void {
    this.addToast('error', 'Error de validación del servidor', detail, 9000);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // LISTADO
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
  // MODAL EDICIÓN
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
  // FLUJO NUEVO — PASO 1
  // ═══════════════════════════════════════════════════════════════════════════
  abrirDialogNuevoPeriodo(): void {
    this.formularioPeriodo.reset();
    this.fasesEditables        = [];
    this.periodoNuevoId        = null;
    this.periodoNuevoNombre    = '';
    this.pasoActivo            = 0;
    this.dialogCreacionVisible = true;
  }

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
    this.cargarPeriodos();
  }

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
  // FLUJO NUEVO — PASO 2
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
  // MODAL IMPORTAR
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
  // HELPERS — originales
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

  getDuracionDias(fase: FaseEditable): number {
    if (!fase.fechaInicio || !fase.fechaFin) return 0;
    const inicio = new Date(fase.fechaInicio + 'T00:00:00');
    const fin    = new Date(fase.fechaFin    + 'T00:00:00');
    return Math.max(0, Math.ceil((fin.getTime() - inicio.getTime()) / 86400000) + 1);
  }

  /** Posición y ancho de la barra en porcentaje dentro de la columna timeline */
  getFaseStyle(fase: FaseEditable): Record<string, string> {
    if (!this.periodoRangoInicio || !this.periodoRangoFin || !fase.fechaInicio || !fase.fechaFin) {
      return { display: 'none' };
    }
    const inicioP = new Date(this.periodoRangoInicio + 'T00:00:00').getTime();
    const finP    = new Date(this.periodoRangoFin    + 'T00:00:00').getTime();
    const inicioF = new Date(fase.fechaInicio        + 'T00:00:00').getTime();
    const finF    = new Date(fase.fechaFin           + 'T00:00:00').getTime();
    const total   = finP - inicioP;
    const left    = ((inicioF - inicioP) / total) * 100;
    const width   = ((finF - inicioF + 86400000) / total) * 100;
    return { left: `${left}%`, width: `${width}%` };
  }

  getFechaLabel(porcentaje: number): string {
    if (!this.periodoRangoInicio || !this.periodoRangoFin) return '';
    const inicio = new Date(this.periodoRangoInicio + 'T00:00:00').getTime();
    const fin    = new Date(this.periodoRangoFin    + 'T00:00:00').getTime();
    const date   = new Date(inicio + (fin - inicio) * (porcentaje / 100));
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' });
  }

  // ─────────────────────────────────────────────────────────────────────────
  // NUEVOS: helpers para el Gantt de dos columnas
  // ─────────────────────────────────────────────────────────────────────────

  /** Color por índice (0-based) — punto lateral y barra del Gantt */
  getFaseColor(index: number): string {
    const colores = [
      '#3b82f6', '#22c55e', '#f59e0b', '#a855f7', '#ef4444',
      '#eab308', '#06b6d4', '#ec4899', '#6366f1', '#14b8a6',
    ];
    return colores[index % colores.length];
  }

  /** Segmentos de meses para la cabecera del Gantt */
  get mesesGantt(): { label: string; leftPct: number; widthPct: number }[] {
    if (!this.periodoRangoInicio || !this.periodoRangoFin) return [];
    const NOMBRES = ['ENE','FEB','MAR','ABR','MAY','JUN','JUL','AGO','SEP','OCT','NOV','DIC'];
    const inicio  = new Date(this.periodoRangoInicio + 'T00:00:00');
    const fin     = new Date(this.periodoRangoFin    + 'T00:00:00');
    const totalMs = fin.getTime() - inicio.getTime();
    const result: { label: string; leftPct: number; widthPct: number }[] = [];
    const cur = new Date(inicio.getFullYear(), inicio.getMonth(), 1);
    while (cur <= fin) {
      const mesStart  = Math.max(cur.getTime(), inicio.getTime());
      const nextMonth = new Date(cur.getFullYear(), cur.getMonth() + 1, 1);
      const mesEnd    = Math.min(nextMonth.getTime() - 1, fin.getTime());
      result.push({
        label    : NOMBRES[cur.getMonth()],
        leftPct  : ((mesStart - inicio.getTime()) / totalMs) * 100,
        widthPct : ((mesEnd   - mesStart)         / totalMs) * 100,
      });
      cur.setMonth(cur.getMonth() + 1);
    }
    return result;
  }
}
