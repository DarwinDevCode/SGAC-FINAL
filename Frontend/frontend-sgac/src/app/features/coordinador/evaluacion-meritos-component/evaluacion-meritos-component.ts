// src/app/features/coordinador/evaluacion-meritos-component/evaluacion-meritos-component.ts
import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }       from '@angular/common';
import { FormsModule }        from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';

import { EvaluacionMeritosService } from '../../../core/services/evaluaciones/evaluacion-meritos-service';
import {
  EvaluacionMeritosDTO,
  EvaluacionMeritosResponse,
  calcularNotaAsignatura,
  calcularNotaSemestres,
  calcularTotal,
  puntajesPorNota,
} from '../../../core/models/evaluaciones/Evaluacionmeritos';

@Component({
  selector:    'app-evaluacion-meritos',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './evaluacion-meritos-component.html',
  styleUrls:   ['./evaluacion-meritos-component.css'],
})
export class EvaluacionMeritosComponent implements OnInit, OnDestroy {

  private svc    = inject(EvaluacionMeritosService);
  private route  = inject(ActivatedRoute);
  private router = inject(Router);

  // ── Estado ────────────────────────────────────────────────────
  loading    = true;
  guardando  = false;
  error      = '';
  toastMsg   = '';
  toastTipo  = 'ok';
  private toastTimer: any;

  // ── Datos del postulante ──────────────────────────────────────
  idPostulacion  = 0;
  faseActiva     = false;
  datos: EvaluacionMeritosResponse | null = null;
  evaluacionActual: EvaluacionMeritosDTO | null = null;

  // ── Campos del formulario ─────────────────────────────────────
  notaAprobacionAsignatura: number | null = null;  // nota cruda 0–10
  semestresNotas: number[] = [];                    // array dinámico
  nuevaSemestreNota: number | null = null;          // campo temporal de entrada
  notaExperiencia: number | null = null;            // 0–4
  notaEventos: number | null = null;                // 0–2

  // ── Modal confirmación ────────────────────────────────────────
  mostrarModalFinalizar = false;
  mostrarModalReabrir   = false;

  // ═══════════════════════════════════════════════════════════════
  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('idPostulacion'));
      if (!id) { this.router.navigate(['/coordinador/evaluacion-meritos']); return; }
      this.idPostulacion = id;
      this.cargar();
    });
  }

  ngOnDestroy(): void { clearTimeout(this.toastTimer); }

  volver(): void {
    this.router.navigate(['/coordinador/evaluacion-meritos']);
  }

  // ── Carga inicial ─────────────────────────────────────────────

  cargar(): void {
    this.loading = true;
    this.svc.obtener(this.idPostulacion).subscribe({
      next: res => {
        this.loading    = false;
        this.datos      = res;
        this.faseActiva = res.faseActiva ?? false;

        if (!res.exito) { this.error = res.mensaje ?? 'Error al cargar.'; return; }

        this.evaluacionActual = res.evaluacion ?? null;
        this.cargarFormDesdeEvaluacion();
      },
      error: (err: Error) => { this.loading = false; this.error = err.message; },
    });
  }

  /** Precarga el formulario con los datos de una evaluación existente */
  private cargarFormDesdeEvaluacion(): void {
    if (!this.evaluacionActual) return;

    // Campos manuales — siempre disponibles en BD
    this.notaExperiencia = this.evaluacionActual.notaExperiencia;
    this.notaEventos     = this.evaluacionActual.notaEventos;

    // Nota asignatura: la BD guarda pts convertidos (8.00), no la nota raw.
    // Usamos mapeo inverso (mínimo del rango) para pre-rellenar el input.
    // Cuando FINALIZADA los getters usan evaluacionActual directamente,
    // así que el valor del input es solo visual.
    this.notaAprobacionAsignatura = this.reverseMapNotaAsignatura(
      this.evaluacionActual.notaAsignatura
    );

    // semestresNotas NO se puede reconstruir desde el total almacenado.
    // Cuando FINALIZADA ptosSemestres usa evaluacionActual.notaSemestres.
    // Cuando BORRADOR el coordinador debe re-ingresar los semestres.
    this.semestresNotas = [];
  }

  /**
   * Mapeo inverso: pts almacenados → nota raw mínima del rango.
   * Solo aproximado; la nota exacta original se perdió en la conversión.
   */
  private reverseMapNotaAsignatura(ptos: number): number | null {
    if (ptos >= 10) return 9.50;
    if (ptos >= 9)  return 9.00;
    if (ptos >= 8)  return 8.50;
    if (ptos >= 7)  return 8.00;
    return null;
  }

  // ── Cálculos en tiempo real ───────────────────────────────────

  // ── Cuando FINALIZADA los getters usan los valores almacenados en BD
  //    para mostrar los puntajes reales en el resumen lateral,
  //    independientemente de lo que haya en los campos del formulario.

  get ptosAsignatura(): number {
    if (this.evaluacionActual?.estado === 'FINALIZADA')
      return this.evaluacionActual.notaAsignatura;
    return calcularNotaAsignatura(this.notaAprobacionAsignatura ?? 0);
  }

  get ptosSemestres(): number {
    // El desglose individual no está en BD: usamos el total almacenado.
    if (this.evaluacionActual?.estado === 'FINALIZADA')
      return this.evaluacionActual.notaSemestres;
    return calcularNotaSemestres(this.semestresNotas);
  }

  get ptosExperiencia(): number {
    return Math.min(Math.max(this.notaExperiencia ?? 0, 0), 4);
  }

  get ptosEventos(): number {
    return Math.min(Math.max(this.notaEventos ?? 0, 0), 2);
  }

  get totalActual(): number {
    // FINALIZADA: usar el total real de BD para evitar discrepancias de redondeo
    if (this.evaluacionActual?.estado === 'FINALIZADA')
      return this.evaluacionActual.notaTotal;
    return calcularTotal(this.ptosAsignatura, this.ptosSemestres, this.ptosExperiencia, this.ptosEventos);
  }

  /** Puntos que aporta una nota individual de semestre */
  ptosSemestre(nota: number): number {
    return puntajesPorNota(nota);
  }

  get formularioBloqueado(): boolean {
    return this.evaluacionActual?.estado === 'FINALIZADA' || !this.faseActiva;
  }

  get puedeReabrir(): boolean {
    return this.evaluacionActual?.estado === 'FINALIZADA' && this.faseActiva;
  }

  // ── Gestión de semestres dinámicos ────────────────────────────

  agregarSemestre(): void {
    const nota = this.nuevaSemestreNota;
    if (nota == null || nota < 0 || nota > 10) {
      this.toast('La nota del semestre debe estar entre 0.00 y 10.00.', 'warn');
      return;
    }
    this.semestresNotas = [...this.semestresNotas, +nota];
    this.nuevaSemestreNota = null;
  }

  eliminarSemestre(index: number): void {
    this.semestresNotas = this.semestresNotas.filter((_, i) => i !== index);
  }

  // ── Guardado ──────────────────────────────────────────────────

  private validarFormulario(): string | null {
    if (this.notaAprobacionAsignatura == null) return 'Ingresa la nota de aprobación de la asignatura.';
    if (this.notaAprobacionAsignatura < 0 || this.notaAprobacionAsignatura > 10)
      return 'La nota de aprobación debe ser entre 0.00 y 10.00.';
    if (this.notaExperiencia == null) return 'Ingresa la nota de experiencia.';
    if (this.notaEventos == null)     return 'Ingresa la nota de eventos.';
    return null;
  }

  guardarBorrador(): void {
    const err = this.validarFormulario();
    if (err) { this.toast(err, 'warn'); return; }
    this.enviar(false);
  }

  confirmarFinalizar(): void {
    const err = this.validarFormulario();
    if (err) { this.toast(err, 'warn'); return; }
    this.mostrarModalFinalizar = true;
  }

  ejecutarFinalizar(): void {
    this.mostrarModalFinalizar = false;
    this.enviar(true);
  }

  private enviar(finalizar: boolean): void {
    this.guardando = true;
    this.svc.guardar({
      idPostulacion:            this.idPostulacion,
      notaAprobacionAsignatura: this.notaAprobacionAsignatura!,
      semestresNotas:           this.semestresNotas,
      notaExperiencia:          this.notaExperiencia!,
      notaEventos:              this.notaEventos!,
      finalizar,
    }).subscribe({
      next: res => {
        this.guardando = false;
        this.toast(res.mensaje ?? (finalizar ? 'Evaluación finalizada.' : 'Borrador guardado.'), 'ok');
        this.cargar(); // refrescar estado
      },
      error: (err: Error) => { this.guardando = false; this.toast(err.message, 'err'); },
    });
  }

  // ── Reabrir ───────────────────────────────────────────────────

  confirmarReabrir(): void  { this.mostrarModalReabrir = true; }
  cancelarReabrir(): void   { this.mostrarModalReabrir = false; }

  ejecutarReabrir(): void {
    this.mostrarModalReabrir = false;
    this.guardando = true;
    this.svc.reabrir(this.idPostulacion).subscribe({
      next: res => {
        this.guardando = false;
        this.toast(res.mensaje ?? 'Evaluación reabierta.', 'ok');
        this.cargar();
      },
      error: (err: Error) => { this.guardando = false; this.toast(err.message, 'err'); },
    });
  }

  // ── Toast ─────────────────────────────────────────────────────

  private toast(msg: string, tipo: 'ok' | 'err' | 'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg  = msg;
    this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', tipo === 'err' ? 8000 : 5000);
  }

  // ── Helpers de formato ────────────────────────────────────────

  fmt2(n: number): string { return n.toFixed(2); }

  rangoBarra(n: number, max: number): string {
    return `${Math.round((n / max) * 100)}%`;
  }

  labelEstadoEval(estado?: string): string {
    if (estado === 'FINALIZADA') return 'Finalizada';
    if (estado === 'BORRADOR')   return 'Borrador';
    return 'Sin evaluar';
  }

  badgeEstadoEval(estado?: string): string {
    if (estado === 'FINALIZADA') return 'badge-green';
    if (estado === 'BORRADOR')   return 'badge-amber';
    return 'badge-gray';
  }
}
