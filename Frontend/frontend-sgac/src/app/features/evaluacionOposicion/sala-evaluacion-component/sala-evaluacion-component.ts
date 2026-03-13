// src/app/features/comite/sala-evaluacion/sala-evaluacion.ts
import {
  Component, OnInit, OnDestroy, inject, Input
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormsModule }         from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';

import { AuthService }                from '../../../core/services/auth-service';
import { EvaluacionOposicionService } from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import { TurnoOposicion, PuntajeJurado } from '../../../core/models/evaluaciones/EvaluacionOposicion';

@Component({
  selector: 'app-sala-evaluacion',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './sala-evaluacion-component.html',
  styleUrls: ['./sala-evaluacion-component.ts']
})
export class SalaEvaluacionComponent implements OnInit, OnDestroy {
  // idConvocatoria vendría como route param en producción.
  // Lo recibimos como @Input para reutilizabilidad.
  @Input() idConvocatoria = 0;

  private authSrv = inject(AuthService);
  private svc     = inject(EvaluacionOposicionService);

  // ── Estado general ─────────────────────────────────────────
  loading    = true;
  toastMsg   = '';
  toastTipo  = 'ok';
  private toastTimer: any;

  // ── Usuario actual ─────────────────────────────────────────
  idUsuario   = 0;
  rolUsuario  = '';
  esCoord     = false;

  // ── Turno activo (EN_CURSO) ────────────────────────────────
  turnos:       TurnoOposicion[] = [];
  turnoActual:  TurnoOposicion | null = null;

  // ── Formulario de notas (jurado) ───────────────────────────
  pMaterial    = 0;
  pExposicion  = 0;
  pRespuestas  = 0;
  guardando    = false;
  /** true si el usuario ya finalizó sus notas */
  yaFinalizo   = false;

  // ── Timer circular ─────────────────────────────────────────
  /**
   * El bloque total es 35 minutos.
   * Dividimos visualmente en 3 segmentos sobre el SVG:
   *   0-20 min  → segmento verde  (Exposición)
   *   20-30 min → segmento azul   (Preguntas)
   *   30-35 min → segmento naranja (Transición)
   */
  readonly BLOQUE_TOTAL_SEG = 35 * 60; // 2100 segundos
  timerSegundos  = 0;         // segundos transcurridos desde hora_inicio_real
  timerActivo    = false;
  private timerInterval: any;

  // ── Ciclo de vida ──────────────────────────────────────────
  ngOnInit(): void {
    const user = this.authSrv.getUser();
    if (user) {
      this.idUsuario  = user.idUsuario;
      this.rolUsuario = user.rolActual ?? '';
      this.esCoord    = this.rolUsuario.toUpperCase() === 'COORDINADOR';
    }
    this.cargarCronograma();
  }

  ngOnDestroy(): void {
    clearInterval(this.timerInterval);
    clearTimeout(this.toastTimer);
  }

  // ══════════════════════════════════════════════════════════
  // CARGA Y SELECCIÓN DE TURNO ACTIVO
  // ══════════════════════════════════════════════════════════
  cargarCronograma(): void {
    this.loading = true;
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        this.loading = false;
        this.turnos  = res.cronograma ?? [];
        // Seleccionar el turno EN_CURSO automáticamente
        const enCurso = this.turnos.find(t => t.estado === 'EN_CURSO');
        if (enCurso) this.seleccionarTurno(enCurso);
      },
      error: (err: Error) => {
        this.loading = false;
        this.toast(err.message, 'err');
      }
    });
  }

  seleccionarTurno(turno: TurnoOposicion): void {
    this.turnoActual = turno;
    this.resetForm();
    this.detectarYaFinalizo();

    // Iniciar timer si el turno está EN_CURSO y tiene hora real registrada
    clearInterval(this.timerInterval);
    if (turno.estado === 'EN_CURSO' && turno.horaInicioReal) {
      this.iniciarTimer(turno.horaInicioReal);
    } else {
      this.timerSegundos = 0;
      this.timerActivo   = false;
    }
  }

  private detectarYaFinalizo(): void {
    if (!this.turnoActual) { this.yaFinalizo = false; return; }
    const miJurado = this.turnoActual.jurados.find(j => j.idUsuario === this.idUsuario);
    this.yaFinalizo = miJurado?.finalizo ?? false;
    if (miJurado) {
      this.pMaterial   = miJurado.puntajeMaterial   ?? 0;
      this.pExposicion = miJurado.puntajeExposicion ?? 0;
      this.pRespuestas = miJurado.puntajeRespuestas ?? 0;
    }
  }

  private resetForm(): void {
    this.pMaterial = this.pExposicion = this.pRespuestas = 0;
    this.yaFinalizo = false;
  }

  // ══════════════════════════════════════════════════════════
  // TIMER CIRCULAR
  // ══════════════════════════════════════════════════════════

  private iniciarTimer(horaInicioReal: string): void {
    this.timerActivo = true;
    const calcularSegundos = () => {
      const ahora    = new Date();
      const [h, m, s] = horaInicioReal.split(':').map(Number);
      const inicio   = new Date(ahora);
      inicio.setHours(h, m, s ?? 0, 0);
      const diff = Math.floor((ahora.getTime() - inicio.getTime()) / 1000);
      this.timerSegundos = Math.max(0, Math.min(diff, this.BLOQUE_TOTAL_SEG));
    };
    calcularSegundos();
    this.timerInterval = setInterval(calcularSegundos, 1000);
  }

  /** Porcentaje de avance del timer (0 → 100) */
  get timerPorcentaje(): number {
    return Math.min(100, (this.timerSegundos / this.BLOQUE_TOTAL_SEG) * 100);
  }

  /** Segmento actual: 'expo' | 'preguntas' | 'transicion' | 'fin' */
  get segmentoActual(): string {
    const min = this.timerSegundos / 60;
    if (min < 20)  return 'expo';
    if (min < 30)  return 'preguntas';
    if (min < 35)  return 'transicion';
    return 'fin';
  }

  /** Color del arco SVG según segmento */
  get colorTimer(): string {
    const m: Record<string, string> = {
      expo:       '#15803d',
      preguntas:  '#1d4ed8',
      transicion: '#d97706',
      fin:        '#dc2626'
    };
    return m[this.segmentoActual];
  }

  get labelSegmento(): string {
    const m: Record<string, string> = {
      expo:       'Exposición (20 min)',
      preguntas:  'Preguntas (10 min)',
      transicion: 'Transición (5 min)',
      fin:        'Tiempo agotado'
    };
    return m[this.segmentoActual];
  }

  get tiempoRestante(): string {
    const resto = Math.max(0, this.BLOQUE_TOTAL_SEG - this.timerSegundos);
    const m = Math.floor(resto / 60).toString().padStart(2, '0');
    const s = (resto % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  /**
   * Calcula el dashoffset del arco SVG.
   * Círculo: r=44 → circunferencia = 2π*44 ≈ 276.46
   */
  get svgDashoffset(): number {
    const circunferencia = 2 * Math.PI * 44;
    return circunferencia * (1 - this.timerPorcentaje / 100);
  }

  get svgCircunferencia(): number { return 2 * Math.PI * 44; }

  // ══════════════════════════════════════════════════════════
  // ACCIONES DEL COORDINADOR
  // ══════════════════════════════════════════════════════════

  iniciarTurnoActual(): void {
    if (!this.turnoActual) return;
    this.svc.iniciarEvaluacion(this.turnoActual.idEvaluacionOposicion).subscribe({
      next: res => {
        this.toast('Evaluación iniciada. ¡El tribunal puede calificar!', 'ok');
        this.cargarCronograma();
      },
      error: (err: Error) => this.toast(err.message, 'err')
    });
  }

  finalizarTurnoActual(): void {
    if (!this.turnoActual) return;
    this.svc.finalizarEvaluacion(this.turnoActual.idEvaluacionOposicion).subscribe({
      next: res => {
        this.toast(`Acta cerrada. Nota final: ${res.puntajeFinal}`, 'ok');
        clearInterval(this.timerInterval);
        this.cargarCronograma();
      },
      error: (err: Error) => this.toast(err.message, 'err')
    });
  }

  // ══════════════════════════════════════════════════════════
  // GUARDAR PUNTAJE (JURADO)
  // ══════════════════════════════════════════════════════════

  get subtotal(): number {
    return (this.pMaterial || 0) + (this.pExposicion || 0) + (this.pRespuestas || 0);
  }

  guardarPuntaje(finalizar: boolean): void {
    if (!this.turnoActual || this.guardando) return;

    // Validaciones de tope en el frontend
    if (this.pMaterial > 10)   { this.toast('Material: máximo 10 puntos.', 'warn'); return; }
    if (this.pExposicion > 4)  { this.toast('Exposición: máximo 4 puntos.', 'warn'); return; }
    if (this.pRespuestas > 6)  { this.toast('Respuestas: máximo 6 puntos.', 'warn'); return; }
    if (this.pMaterial < 0 || this.pExposicion < 0 || this.pRespuestas < 0) {
      this.toast('Los puntajes no pueden ser negativos.', 'warn'); return;
    }

    this.guardando = true;
    this.svc.registrarPuntaje({
      idEvaluacionOposicion: this.turnoActual.idEvaluacionOposicion,
      idUsuario:             this.idUsuario,
      puntajeMaterial:       this.pMaterial,
      puntajeExposicion:     this.pExposicion,
      puntajeRespuestas:     this.pRespuestas,
      finalizar
    }).subscribe({
      next: res => {
        this.guardando = false;
        if (finalizar) this.yaFinalizo = true;
        if (res.todosFinalizaron) {
          this.toast(`¡Todos finalizaron! Nota final: ${res.puntajeFinal}`, 'ok');
        } else {
          this.toast(finalizar ? 'Calificación bloqueada.' : 'Puntaje guardado.', 'ok');
        }
        this.cargarCronograma();
      },
      error: (err: Error) => { this.guardando = false; this.toast(err.message, 'err'); }
    });
  }

  // ══════════════════════════════════════════════════════════
  // HELPERS
  // ══════════════════════════════════════════════════════════

  get puedeCalificar(): boolean {
    return !!this.turnoActual
      && this.turnoActual.estado === 'EN_CURSO'
      && !this.yaFinalizo
      && !this.esCoord;
  }

  get formularioReadonly(): boolean {
    if (!this.turnoActual) return true;
    return this.turnoActual.estado === 'FINALIZADA' || this.yaFinalizo;
  }

  badgeEstado(estado: string): string {
    const m: Record<string, string> = {
      PROGRAMADA: 'badge-blue', EN_CURSO: 'badge-amber',
      FINALIZADA: 'badge-green', NO_PRESENTO: 'badge-red'
    };
    return m[estado] ?? 'badge-gray';
  }

  formatHora(h: string | undefined): string { return h ?? '—'; }

  formatFecha(f: string | undefined): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  private toast(msg: string, tipo: 'ok'|'err'|'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg  = msg;
    this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', tipo === 'err' ? 9000 : 4000);
  }
}
