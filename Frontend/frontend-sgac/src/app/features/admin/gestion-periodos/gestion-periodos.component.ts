import { Component, inject, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule, ReactiveFormsModule,
  FormGroup, FormControl, Validators,
} from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';

import { PeriodoAcademicoService } from '../../../core/services/periodo-academico-service';
import { PeriodoAcademicoDTO }     from '../../../core/dto/periodo-academico';
import { ConfiguracionService }    from '../../../core/services/configuracion/Configuracion';
import {
  PeriodoFaseResponse, FaseCronogramaRequest, AjusteCronogramaRequest,
} from '../../../core/models/configuracion/Configuracion';

interface ToastMessage {
  id: number; severity: 'success'|'error'|'warn'|'info'; summary: string; detail: string;
}
interface FaseEditable {
  idPeriodoFase: number; idTipoFase: number; nombreFase: string; orden: number;
  fechaInicio: string; fechaFin: string; tieneError: boolean; mensajeError: string;
}
export interface GanttColumn {
  label: string; subLabel: string; leftPct: number; widthPct: number;
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

  readonly FASE_COLORS = [
    '#3b82f6','#22c55e','#f59e0b','#a855f7','#ef4444',
    '#eab308','#06b6d4','#ec4899','#6366f1','#14b8a6',
  ];

  // ── TOASTS ─────────────────────────────────────────────────────────────────
  toasts: ToastMessage[] = [];
  private toastIdCtr = 0;

  // ── LISTADO ─────────────────────────────────────────────────────────────────
  periodos: PeriodoAcademicoDTO[] = [];

  // ── STEPPER ─────────────────────────────────────────────────────────────────
  dialogCreacionVisible = false;
  pasoActivo = 0;

  formularioPeriodo = new FormGroup({
    nombrePeriodo : new FormControl('', [Validators.required, Validators.minLength(3)]),
    fechaInicio   : new FormControl('', Validators.required),
    fechaFin      : new FormControl('', Validators.required),
  });
  abriendo = false;

  periodoNuevoId = null as number|null;
  periodoNuevoNombre = '';
  periodoRangoInicio = '';
  periodoRangoFin    = '';

  fasesEditables: FaseEditable[] = [];
  cargandoFases = false;
  guardandoCronograma = false;
  tieneErroresCronograma = true;

  // ── MODAL EDICIÓN ──────────────────────────────────────────────────────────
  modalEditarAbierto = false;
  formularioEdicion: Partial<PeriodoAcademicoDTO> = {};

  // ── MODAL IMPORTAR ─────────────────────────────────────────────────────────
  modalImportarAbierto = false;
  importarConfig = { idDestino: null as number|null, idFuente: null as number|null };

  // ── MODAL INICIAR ──────────────────────────────────────────────────────────
  periodoAIniciar: PeriodoAcademicoDTO | null = null;
  modalIniciarAbierto = false;
  iniciandoPeriodo = false;

  // ═══════════════════════════════════════════════════════════════════════════
  ngOnInit(): void { this.cargarPeriodos(); }

  // ── TOASTS ─────────────────────────────────────────────────────────────────
  addToast(s: ToastMessage['severity'], sum: string, det: string, life = 5000): void {
    const id = ++this.toastIdCtr;
    this.toasts.push({ id, severity: s, summary: sum, detail: det });
    setTimeout(() => this.removeToast(id), life);
  }
  removeToast(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }
  private toastOk (d: string) { this.addToast('success', 'Éxito', d, 4000); }
  private toastErr(d: string) { this.addToast('error',   'Error', d, 6000); }
  private toastSrv(d: string) { this.addToast('error', 'Error del servidor', d, 9000); }

  // ── LISTADO ─────────────────────────────────────────────────────────────────
  cargarPeriodos(): void {
    this.periodoService.listarTodos().subscribe({
      next  : r  => { this.periodos = r; },
      error : () => this.toastErr('No se pudieron cargar los periodos académicos'),
    });
  }

  cambiarEstado(p: PeriodoAcademicoDTO, activando: boolean): void {
    const op = activando
      ? this.periodoService.activar(p.idPeriodoAcademico!)
      : this.periodoService.desactivar(p.idPeriodoAcademico!);
    op.subscribe({
      next  : () => { this.cargarPeriodos(); this.toastOk(activando ? 'Periodo activado' : 'Periodo desactivado'); },
      error : () => this.toastErr('No se pudo cambiar el estado del periodo'),
    });
  }

  // ── MODAL INICIAR ──────────────────────────────────────────────────────────
  abrirModalIniciar(p: PeriodoAcademicoDTO): void {
    this.periodoAIniciar    = p;
    this.modalIniciarAbierto = true;
  }

  cerrarModalIniciar(): void {
    this.periodoAIniciar    = null;
    this.modalIniciarAbierto = false;
  }

  confirmarInicio(): void {
    if (!this.periodoAIniciar?.idPeriodoAcademico) return;
    this.iniciandoPeriodo = true;
    this.configuracionSvc.iniciarPeriodo(this.periodoAIniciar.idPeriodoAcademico).subscribe({
      next: res => {
        this.iniciandoPeriodo = false;
        if (res.exito) {
          this.addToast('success', '¡Periodo iniciado!',
            `"${this.periodoAIniciar!.nombrePeriodo}" está ahora EN PROCESO.`, 5000);
          this.cerrarModalIniciar();
          this.cargarPeriodos();
        } else {
          this.toastSrv(res.mensaje ?? 'No se pudo iniciar el periodo');
        }
      },
      error: err => {
        this.iniciandoPeriodo = false;
        this.toastErr(err.message ?? 'Error de conexión al intentar iniciar el periodo');
      },
    });
  }

  // ── MODAL EDICIÓN ──────────────────────────────────────────────────────────
  abrirModalEditar(p: PeriodoAcademicoDTO): void {
    if (p.estado === 'INACTIVO') return;
    this.formularioEdicion  = { ...p };
    this.modalEditarAbierto = true;
  }
  cerrarModalEditar(): void { this.modalEditarAbierto = false; }
  guardarEdicion(): void {
    if (!this.formularioEdicion.idPeriodoAcademico) return;
    this.periodoService.actualizar(this.formularioEdicion.idPeriodoAcademico, this.formularioEdicion as any)
      .subscribe({
        next  : () => { this.toastOk('Periodo actualizado'); this.cargarPeriodos(); this.cerrarModalEditar(); },
        error : () => this.toastErr('No se pudo actualizar el periodo'),
      });
  }

  // ── PASO 1 ─────────────────────────────────────────────────────────────────
  abrirDialogNuevoPeriodo(): void {
    this.formularioPeriodo.reset();
    this.fasesEditables = []; this.periodoNuevoId = null; this.periodoNuevoNombre = '';
    this.pasoActivo = 0; this.dialogCreacionVisible = true;
  }

  abrirDialogCronograma(p: PeriodoAcademicoDTO): void {
    this.periodoNuevoId = p.idPeriodoAcademico ?? null;
    this.periodoNuevoNombre = p.nombrePeriodo;
    this.periodoRangoInicio = p.fechaInicio as string;
    this.periodoRangoFin    = p.fechaFin    as string;
    this.pasoActivo = 1; this.dialogCreacionVisible = true;
    if (this.periodoNuevoId) this.cargarFases(this.periodoNuevoId);
  }

  cerrarDialogCreacion(): void { this.dialogCreacionVisible = false; this.cargarPeriodos(); }

  confirmarApertura(): void {
    if (this.formularioPeriodo.invalid) { this.formularioPeriodo.markAllAsTouched(); return; }
    const { nombrePeriodo, fechaInicio, fechaFin } = this.formularioPeriodo.value;
    if (fechaInicio! >= fechaFin!) {
      this.addToast('warn', 'Fechas inválidas', 'La fecha de inicio debe ser anterior a la fecha de fin');
      return;
    }
    this.abriendo = true;
    this.configuracionSvc.abrirPeriodo({ nombrePeriodo: nombrePeriodo!, fechaInicio: fechaInicio!, fechaFin: fechaFin! })
      .subscribe({
        next: res => {
          this.abriendo = false;
          if (res.exito && res.id) {
            this.periodoNuevoId = res.id; this.periodoNuevoNombre = nombrePeriodo!;
            this.periodoRangoInicio = fechaInicio!; this.periodoRangoFin = fechaFin!;
            this.toastOk('Periodo creado. Configure el cronograma de fases.');
            this.pasoActivo = 1; this.cargarFases(res.id);
          } else { this.toastErr(res.mensaje ?? 'Error desconocido al abrir el periodo'); }
        },
        error: err => { this.abriendo = false; this.toastErr(err.message ?? 'Error de conexión'); },
      });
  }

  // ── PASO 2 ─────────────────────────────────────────────────────────────────
  // ── PASO 2 ─────────────────────────────────────────────────────────────────
  cargarFases(id: number): void {
    this.cargandoFases = true;
    this.configuracionSvc.obtenerCronograma(id).subscribe({
      next: res => {
        this.cargandoFases = false;
        if (res.exito && res.datos) {
          this.fasesEditables = res.datos.map((f: PeriodoFaseResponse) => ({
            idPeriodoFase: f.idPeriodoFase, idTipoFase: f.idTipoFase,
            nombreFase: f.nombreFase, orden: f.orden,
            fechaInicio: f.fechaInicio, fechaFin: f.fechaFin,
            tieneError: false, mensajeError: '',
          }));

          // CORRECCIÓN: Detectamos si es el cronograma por defecto
          // Es por defecto si TODAS las fases están vacías o si TODAS duran exactamente 1 día (fechaInicio === fechaFin)
          const esCronogramaPorDefecto = this.fasesEditables.every(f =>
            !f.fechaInicio || !f.fechaFin || f.fechaInicio === f.fechaFin
          );

          if (esCronogramaPorDefecto && this.fasesEditables.length > 0) {
            this.distribuirFasesUniformemente();
          }

          this.validarCronograma();
        } else { this.toastErr(res.mensaje ?? 'No se pudieron cargar las fases'); }
      },
      error: () => { this.cargandoFases = false; this.toastErr('Error al obtener el cronograma'); },
    });
  }

  // ── NUEVO MATEMÁTICO: DISTRIBUCIÓN UNIFORME DE FASES ───────────────────────
  distribuirFasesUniformemente(): void {
    const totalFases = this.fasesEditables.length;
    const pInicio = this.toDate(this.periodoRangoInicio);
    const pFin = this.toDate(this.periodoRangoFin);

    // Días totales del periodo (+1 para incluir inicio y fin cerrados)
    const diasTotalesPeriodo = Math.round((pFin.getTime() - pInicio.getTime()) / 86400000) + 1;

    // Repartimos usando enteros
    const diasPorFase = Math.max(1, Math.floor(diasTotalesPeriodo / totalFases));

    let fechaActual = new Date(pInicio.getTime());

    for (let i = 0; i < totalFases; i++) {
      const fase = this.fasesEditables[i];

      // Si nos quedamos sin días (muy raro, solo si el periodo dura muy poco), truncamos al final
      if (fechaActual > pFin) {
        fechaActual = new Date(pFin.getTime());
      }

      fase.fechaInicio = this.toString(fechaActual);

      if (i === totalFases - 1) {
        // La última fase absorbe todos los días sobrantes y ancla su fin al fin del periodo
        fase.fechaFin = this.periodoRangoFin;
      } else {
        // Fin de la fase = inicio + dias que le tocan - 1 día
        let finFase = this.addDays(fechaActual, diasPorFase - 1);
        if (finFase > pFin) finFase = new Date(pFin.getTime()); // Seguro matemático

        fase.fechaFin = this.toString(finFase);

        // La siguiente fase empieza al día siguiente
        fechaActual = this.addDays(finFase, 1);
      }
    }
  }

  validarCronograma(): void {
    let err = false;
    this.fasesEditables.forEach(f => { f.tieneError = false; f.mensajeError = ''; });
    for (let i = 0; i < this.fasesEditables.length; i++) {
      const f = this.fasesEditables[i];
      if (!f.fechaInicio || !f.fechaFin) {
        f.tieneError = true; f.mensajeError = 'Las fechas son obligatorias'; err = true; continue;
      }
      if (f.fechaFin < f.fechaInicio) {
        f.tieneError = true; f.mensajeError = 'La fecha de fin no puede ser anterior a la de inicio'; err = true; continue;
      }
      if (this.periodoRangoInicio && f.fechaInicio < this.periodoRangoInicio) {
        f.tieneError = true; f.mensajeError = `Fuera del rango del periodo (inicio: ${this.fmt(this.periodoRangoInicio)})`; err = true; continue;
      }
      if (this.periodoRangoFin && f.fechaFin > this.periodoRangoFin) {
        f.tieneError = true; f.mensajeError = `Fuera del rango del periodo (fin: ${this.fmt(this.periodoRangoFin)})`; err = true; continue;
      }
      if (i < this.fasesEditables.length - 1) {
        const sig = this.fasesEditables[i + 1];
        if (sig.fechaInicio && f.fechaFin >= sig.fechaInicio) {
          f.tieneError = true;
          f.mensajeError = `Solapamiento con "${sig.nombreFase}" (inicia el ${this.fmt(sig.fechaInicio)})`;
          err = true;
        }
      }
    }
    this.tieneErroresCronograma = err;
  }

  guardarCronograma(): void {
    this.validarCronograma();
    if (this.tieneErroresCronograma || !this.periodoNuevoId) return;
    const payload: AjusteCronogramaRequest = {
      idPeriodo : this.periodoNuevoId,
      fases: this.fasesEditables.map(f => ({
        idTipoFase: f.idTipoFase, fechaInicio: f.fechaInicio, fechaFin: f.fechaFin,
      } as FaseCronogramaRequest)),
    };
    this.guardandoCronograma = true;
    this.configuracionSvc.guardarCronograma(payload).subscribe({
      next: res => {
        this.guardandoCronograma = false;
        if (res.exito) {
          this.addToast('success', '¡Cronograma guardado!',
            `"${this.periodoNuevoNombre}" está en CONFIGURADO. Ya puedes iniciarlo desde la tabla.`, 6000);
          this.cerrarDialogCreacion();
        } else { this.toastSrv(res.mensaje ?? 'El cronograma no pasó las validaciones del sistema'); }
      },
      error: err => { this.guardandoCronograma = false; this.toastErr(err.message ?? 'Error al guardar'); },
    });
  }

  // ── IMPORTAR ───────────────────────────────────────────────────────────────
  abrirModalImportar(id: number): void { this.importarConfig = { idDestino: id, idFuente: null }; this.modalImportarAbierto = true; }
  cerrarModalImportar(): void { this.modalImportarAbierto = false; }
  confirmarImportacion(): void {
    const { idDestino, idFuente } = this.importarConfig;
    if (!idDestino || !idFuente) return;
    this.periodoService.importarRequisitos(idDestino, idFuente).subscribe({
      next  : (r: any) => { this.toastOk(r.mensaje ?? 'Requisitos importados'); this.cerrarModalImportar(); },
      error : (e: any) => this.toastErr(e.error?.error ?? 'Error al importar'),
    });
  }

  // ── HELPERS ────────────────────────────────────────────────────────────────
  getEstadoClass(estado: string): string {
    return ({ PLANIFICACION:'planificacion', CONFIGURADO:'configurado', 'EN PROCESO':'proceso',
      INACTIVO:'inactivo', PLANIFICADO:'planificado' } as Record<string,string>)[estado] ?? 'inactivo';
  }
  puedeConfigurarCronograma(p: PeriodoAcademicoDTO): boolean {
    return p.estado === 'PLANIFICACION' || p.estado === 'CONFIGURADO';
  }
  puedeIniciar(p: PeriodoAcademicoDTO): boolean {
    return p.estado === 'CONFIGURADO';
  }
  get nombrePeriodoInvalido(): boolean {
    const c = this.formularioPeriodo.get('nombrePeriodo');
    return !!(c?.invalid && c?.touched);
  }
  fmt(f: string): string {
    if (!f || f.length < 10) return f;
    const [y,m,d] = f.split('-'); return `${d}/${m}/${y}`;
  }

  // ── HELPERS DEL GANTT ─────────────────────────────────────────────────────
  getFaseColor(i: number): string { return this.FASE_COLORS[i % this.FASE_COLORS.length]; }

  getDuracionDias(f: FaseEditable): number {
    if (!f.fechaInicio || !f.fechaFin) return 0;
    const a = new Date(f.fechaInicio + 'T00:00:00'), b = new Date(f.fechaFin + 'T00:00:00');
    return Math.max(0, Math.ceil((b.getTime() - a.getTime()) / 86400000) + 1);
  }

  getFaseBarStyle(f: FaseEditable): { left: string; width: string } | null {
    if (!this.periodoRangoInicio || !this.periodoRangoFin || !f.fechaInicio || !f.fechaFin) return null;
    const pI = new Date(this.periodoRangoInicio + 'T00:00:00').getTime();
    const pF = new Date(this.periodoRangoFin    + 'T00:00:00').getTime();
    const fI = new Date(f.fechaInicio           + 'T00:00:00').getTime();
    const fF = new Date(f.fechaFin              + 'T00:00:00').getTime();
    const total = pF - pI;
    const left  = Math.max(0, ((fI - pI) / total) * 100);
    const width = Math.max(0.5, ((fF - fI + 86400000) / total) * 100);
    return { left: `${left}%`, width: `${Math.min(width, 100 - left)}%` };
  }

  get ganttColumns(): GanttColumn[] {
    if (!this.periodoRangoInicio || !this.periodoRangoFin) return [];
    const inicio  = new Date(this.periodoRangoInicio + 'T00:00:00');
    const fin     = new Date(this.periodoRangoFin    + 'T00:00:00');
    const totalMs = fin.getTime() - inicio.getTime();
    const totalDias = totalMs / 86400000;
    const cols: GanttColumn[] = [];
    const MESES = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];

    if (totalDias <= 90) {
      let cur = new Date(inicio); let semNum = 1;
      while (cur <= fin) {
        const sS = cur.getTime();
        const sE = Math.min(new Date(cur.getTime() + 6 * 86400000).getTime(), fin.getTime());
        cols.push({ label: `Sem ${semNum}`,
          subLabel: `${String(cur.getDate()).padStart(2,'0')}/${String(cur.getMonth()+1).padStart(2,'0')}`,
          leftPct: ((sS - inicio.getTime()) / totalMs) * 100,
          widthPct: ((sE - sS + 86400000) / totalMs) * 100 });
        cur = new Date(cur.getTime() + 7 * 86400000); semNum++;
      }
    } else if (totalDias <= 180) {
      let cur = new Date(inicio.getFullYear(), inicio.getMonth(), 1);
      while (cur <= fin) {
        for (const sd of [1, 16]) {
          const sS = new Date(cur.getFullYear(), cur.getMonth(), sd);
          if (sS > fin) break;
          const sE = sd === 1
            ? new Date(cur.getFullYear(), cur.getMonth(), 15)
            : new Date(cur.getFullYear(), cur.getMonth() + 1, 0);
          const sMs = Math.max(sS.getTime(), inicio.getTime());
          const eMs = Math.min(sE.getTime(), fin.getTime());
          if (eMs >= sMs) cols.push({ label: MESES[sS.getMonth()],
            subLabel: `${String(sS.getDate()).padStart(2,'0')}/${String(sS.getMonth()+1).padStart(2,'0')}`,
            leftPct: ((sMs - inicio.getTime()) / totalMs) * 100,
            widthPct: ((eMs - sMs + 86400000) / totalMs) * 100 });
        }
        cur = new Date(cur.getFullYear(), cur.getMonth() + 1, 1);
      }
    } else {
      let cur = new Date(inicio.getFullYear(), inicio.getMonth(), 1);
      while (cur <= fin) {
        const mS = Math.max(cur.getTime(), inicio.getTime());
        const nM = new Date(cur.getFullYear(), cur.getMonth() + 1, 1);
        const mE = Math.min(nM.getTime() - 1, fin.getTime());
        if (mE >= mS) cols.push({ label: MESES[cur.getMonth()],
          subLabel: `${String(new Date(mS).getDate()).padStart(2,'0')}/${String(cur.getMonth()+1).padStart(2,'0')}`,
          leftPct: ((mS - inicio.getTime()) / totalMs) * 100,
          widthPct: ((mE - mS + 86400000) / totalMs) * 100 });
        cur = nM;
      }
    }
    return cols;
  }

  // ── 🖱️ LÓGICA DE DRAG & DROP DEL GANTT ──────────────────────────────────────
  draggingAction: 'DRAG' | 'RESIZE_L' | 'RESIZE_R' | null = null;
  draggedFase: FaseEditable | null = null;
  dragStartX: number = 0;
  dragStartFechaInicio: Date | null = null;
  dragStartFechaFin: Date | null = null;
  pixelsPerDay: number = 0;

  iniciarDrag(event: MouseEvent, fase: FaseEditable, accion: 'DRAG' | 'RESIZE_L' | 'RESIZE_R', container: HTMLElement): void {
    if (!fase.fechaInicio || !fase.fechaFin || !this.periodoRangoInicio || !this.periodoRangoFin) return;

    event.preventDefault();
    event.stopPropagation();

    this.draggingAction = accion;
    this.draggedFase = fase;
    this.dragStartX = event.clientX;
    this.dragStartFechaInicio = this.toDate(fase.fechaInicio);
    this.dragStartFechaFin = this.toDate(fase.fechaFin);

    const pI = this.toDate(this.periodoRangoInicio);
    const pF = this.toDate(this.periodoRangoFin);
    const totalDays = Math.max(1, (pF.getTime() - pI.getTime()) / 86400000);
    this.pixelsPerDay = container.getBoundingClientRect().width / totalDays;
  }

  @HostListener('document:mousemove', ['$event'])
  onMouseMove(event: MouseEvent): void {
    if (!this.draggingAction || !this.draggedFase || !this.dragStartFechaInicio || !this.dragStartFechaFin) return;

    const deltaX = event.clientX - this.dragStartX;
    const deltaDays = Math.round(deltaX / this.pixelsPerDay);

    let newInicioDate = new Date(this.dragStartFechaInicio.getTime());
    let newFinDate = new Date(this.dragStartFechaFin.getTime());
    const limiteMin = this.toDate(this.periodoRangoInicio);
    const limiteMax = this.toDate(this.periodoRangoFin);

    if (this.draggingAction === 'DRAG') {
      newInicioDate = this.addDays(newInicioDate, deltaDays);
      newFinDate = this.addDays(newFinDate, deltaDays);

      const duracion = Math.round((this.dragStartFechaFin.getTime() - this.dragStartFechaInicio.getTime()) / 86400000);

      if (newInicioDate < limiteMin) {
        newInicioDate = new Date(limiteMin.getTime());
        newFinDate = this.addDays(newInicioDate, duracion);
      }

      if (newFinDate > limiteMax) {
        newFinDate = new Date(limiteMax.getTime());
        newInicioDate = this.addDays(newFinDate, -duracion);
      }

    } else if (this.draggingAction === 'RESIZE_L') {
      newInicioDate = this.addDays(newInicioDate, deltaDays);
      if (newInicioDate < limiteMin) newInicioDate = limiteMin;
      if (newInicioDate > newFinDate) newInicioDate = newFinDate;
    } else if (this.draggingAction === 'RESIZE_R') {
      newFinDate = this.addDays(newFinDate, deltaDays);
      if (newFinDate > limiteMax) newFinDate = limiteMax;
      if (newFinDate < newInicioDate) newFinDate = newInicioDate;
    }

    this.draggedFase.fechaInicio = this.toString(newInicioDate);
    this.draggedFase.fechaFin = this.toString(newFinDate);

    this.validarCronograma();
  }

  @HostListener('document:mouseup')
  onMouseUp(): void {
    this.draggingAction = null;
    this.draggedFase = null;
  }

  private toDate(str: string): Date { return new Date(str + 'T00:00:00'); }
  private addDays(d: Date, days: number): Date {
    const res = new Date(d.getTime());
    res.setDate(res.getDate() + days);
    return res;
  }
  private toString(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
