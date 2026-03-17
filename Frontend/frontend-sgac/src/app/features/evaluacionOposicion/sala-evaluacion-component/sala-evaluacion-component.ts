// src/app/features/evaluacionOposicion/sala-evaluacion-component/sala-evaluacion-component.ts
import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormsModule }         from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { ActivatedRoute }      from '@angular/router';  // ← FIX: leer idConvocatoria de la URL

import { AuthService }                from '../../../core/services/auth-service';
import { EvaluacionOposicionService } from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import { TurnoOposicion }            from '../../../core/models/evaluaciones/EvaluacionOposicion';

@Component({
  selector: 'app-sala-evaluacion',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './sala-evaluacion-component.html',
  styleUrls: ['./sala-evaluacion-component.css']   // ← FIX: era .ts por error tipográfico
})
export class SalaEvaluacionComponent implements OnInit, OnDestroy {

  private route   = inject(ActivatedRoute);           // ← FIX: ya no hardcodeamos el ID
  private authSrv = inject(AuthService);
  private svc     = inject(EvaluacionOposicionService);

  // ── Estado general ────────────────────────────────────────────────
  loading    = true;
  toastMsg   = '';
  toastTipo  = 'ok';
  private toastTimer: any;

  // ── Identificación del usuario autenticado ────────────────────────
  idUsuario   = 0;
  rolUsuario  = '';
  esCoord     = false;

  // ── La convocatoria se lee de la URL (nunca hardcodeada) ──────────
  idConvocatoria = 0;

  // ── Turnos y selección ────────────────────────────────────────────
  turnos:      TurnoOposicion[] = [];
  turnoActual: TurnoOposicion | null = null;

  // ── Formulario de notas ───────────────────────────────────────────
  pMaterial    = 0;
  pExposicion  = 0;
  pRespuestas  = 0;
  guardando    = false;
  yaFinalizo   = false;

  // ── Timer circular (35 minutos = 2100 segundos) ───────────────────
  readonly BLOQUE_TOTAL_SEG = 35 * 60;
  timerSegundos = 0;
  timerActivo   = false;
  private timerInterval: any;

  // ── Polling de sincronización ─────────────────────────────────────
  // Cuando hay un turno EN_CURSO o PROGRAMADA, otros usuarios de la sala
  // (jurados o coordinador) necesitan ver los cambios de estado en tiempo
  // real. Sin WebSockets, la solución más simple es un polling cada 30
  // segundos que recarga el cronograma desde el servidor.
  // El polling se detiene automáticamente cuando no hay turnos activos.
  private pollingTimer: any;
  readonly POLLING_INTERVALO_MS = 30_000;  // 30 segundos

  // ── Ciclo de vida ─────────────────────────────────────────────────

  ngOnInit(): void {
    // Leer el rol del usuario autenticado desde el token almacenado
    const user = this.authSrv.getUser();
    if (user) {
      this.idUsuario  = user.idUsuario;
      this.rolUsuario = user.rolActual ?? '';
      // El Coordinador ve los controles de inicio/cierre; el jurado ve el formulario de notas.
      this.esCoord = this.rolUsuario.toUpperCase().includes('COORDINADOR');
    }

    // Leer el idConvocatoria del parámetro de ruta: /comision/sala/:idConvocatoria
    const paramId = this.route.snapshot.paramMap.get('idConvocatoria');
    this.idConvocatoria = paramId ? Number(paramId) : 0;

    this.cargarCronograma();
    this.iniciarPolling();
  }

  ngOnDestroy(): void {
    clearInterval(this.timerInterval);
    clearInterval(this.pollingTimer);  // ← FIX: limpiar el polling al salir de la vista
    clearTimeout(this.toastTimer);
  }

  // ── Polling de sincronización ─────────────────────────────────────
  //
  // ¿Por qué 30 segundos y no 5?  En una sala de evaluación presencial
  // los cambios relevantes (iniciar, calificar) ocurren en bloques de
  // minutos, no segundos. 30s es un equilibrio entre respuesta ágil
  // y no saturar el servidor con decenas de usuarios concurrentes.
  //
  private iniciarPolling(): void {
    this.pollingTimer = setInterval(() => {
      const hayTurnosActivos = this.turnos.some(
        t => t.estado === 'EN_CURSO' || t.estado === 'PROGRAMADA'
      );
      if (hayTurnosActivos) {
        // Recarga silenciosa: no activa el spinner de loading para no interrumpir
        // al jurado que está ingresando notas en ese momento.
        this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
          next: res => {
            const turnosActualizados = res.cronograma ?? [];
            // Si el turno actualmente seleccionado cambió de estado, lo actualizamos
            // en el panel principal sin perder la selección del usuario.
            if (this.turnoActual) {
              const turnoRefrescado = turnosActualizados.find(
                t => t.idEvaluacionOposicion === this.turnoActual!.idEvaluacionOposicion
              );
              if (turnoRefrescado) {
                const estadoAnterior = this.turnoActual.estado;
                this.turnoActual = turnoRefrescado;
                this.detectarYaFinalizo();
                // Si el coordinador acaba de iniciar el turno, arrancar el timer automáticamente
                if (estadoAnterior === 'PROGRAMADA' && turnoRefrescado.estado === 'EN_CURSO') {
                  this.toast('El coordinador inició la evaluación. Puedes calificar.', 'ok');
                  if (turnoRefrescado.horaInicioReal) this.iniciarTimer(turnoRefrescado.horaInicioReal);
                }
              }
            }
            this.turnos = turnosActualizados;
          },
          error: () => {}  // silenciar errores del polling para no interrumpir la UX
        });
      }
    }, this.POLLING_INTERVALO_MS);
  }

  // ══════════════════════════════════════════════════════════════════
  // CARGA Y SELECCIÓN DE TURNO
  // ══════════════════════════════════════════════════════════════════

  cargarCronograma(): void {
    this.loading = true;
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        this.loading = false;
        this.turnos  = res.cronograma ?? [];
        // Selección automática del turno EN_CURSO al cargar la sala
        const enCurso = this.turnos.find(t => t.estado === 'EN_CURSO');
        if (enCurso) this.seleccionarTurno(enCurso);
      },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); }
    });
  }

  seleccionarTurno(turno: TurnoOposicion): void {
    this.turnoActual = turno;
    this.resetForm();
    this.detectarYaFinalizo();

    clearInterval(this.timerInterval);
    if (turno.estado === 'EN_CURSO' && turno.horaInicioReal) {
      this.iniciarTimer(turno.horaInicioReal);
    } else {
      this.timerSegundos = 0;
      this.timerActivo   = false;
    }
  }

  // ── FIX: detectar si el jurado ya finalizó y pre-cargar sus notas ─
  // Si el jurado ya registró notas (incluso sin finalizar), las
  // mostramos en el formulario para que pueda seguir editando.
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

  // ══════════════════════════════════════════════════════════════════
  // TIMER CIRCULAR SVG
  //
  // El timer usa la hora_inicio_real registrada en la BD (cuando el
  // coordinador pulsó "Iniciar").  Recalculamos los segundos
  // transcurridos cada segundo restando la hora actual a esa hora base.
  // De esta forma el timer es siempre sincrónico con la BD, aunque el
  // usuario recargue la página o entre tarde a la sala.
  // ══════════════════════════════════════════════════════════════════

  private iniciarTimer(horaInicioReal: string): void {
    this.timerActivo = true;
    const calcularSegundos = () => {
      const ahora = new Date();
      const [h, m, s] = horaInicioReal.split(':').map(Number);
      const inicio = new Date(ahora);
      inicio.setHours(h, m, s ?? 0, 0);
      const diff = Math.floor((ahora.getTime() - inicio.getTime()) / 1000);
      this.timerSegundos = Math.max(0, Math.min(diff, this.BLOQUE_TOTAL_SEG));
    };
    calcularSegundos();  // cálculo inmediato para evitar el salto del primer segundo
    this.timerInterval = setInterval(calcularSegundos, 1000);
  }

  get timerPorcentaje(): number {
    return Math.min(100, (this.timerSegundos / this.BLOQUE_TOTAL_SEG) * 100);
  }

  get segmentoActual(): 'expo' | 'preguntas' | 'transicion' | 'fin' {
    const min = this.timerSegundos / 60;
    if (min < 20) return 'expo';
    if (min < 30) return 'preguntas';
    if (min < 35) return 'transicion';
    return 'fin';
  }

  get colorTimer(): string {
    return { expo: '#15803d', preguntas: '#1d4ed8', transicion: '#d97706', fin: '#dc2626' }[this.segmentoActual];
  }

  get labelSegmento(): string {
    return {
      expo:       'Exposición (20 min)',
      preguntas:  'Preguntas (10 min)',
      transicion: 'Transición (5 min)',
      fin:        'Tiempo agotado'
    }[this.segmentoActual];
  }

  get tiempoRestante(): string {
    const resto = Math.max(0, this.BLOQUE_TOTAL_SEG - this.timerSegundos);
    return `${Math.floor(resto / 60).toString().padStart(2,'0')}:${(resto % 60).toString().padStart(2,'0')}`;
  }

  get svgDashoffset(): number {
    return this.svgCircunferencia * (1 - this.timerPorcentaje / 100);
  }

  get svgCircunferencia(): number { return 2 * Math.PI * 44; }

  // ══════════════════════════════════════════════════════════════════
  // ACCIONES DEL COORDINADOR
  // ══════════════════════════════════════════════════════════════════

  iniciarTurnoActual(): void {
    if (!this.turnoActual) return;
    this.svc.iniciarEvaluacion(this.turnoActual.idEvaluacionOposicion).subscribe({
      next: res => { this.toast('Evaluación iniciada. ¡El tribunal puede calificar!', 'ok'); this.cargarCronograma(); },
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

  // ══════════════════════════════════════════════════════════════════
  // FORMULARIO DE NOTAS (JURADO)
  // ══════════════════════════════════════════════════════════════════

  get subtotal(): number {
    return (this.pMaterial || 0) + (this.pExposicion || 0) + (this.pRespuestas || 0);
  }

  // ── FIX: validación estricta de topes antes de enviar al servidor ─
  // Angular [max] en el input previene la mayoría de los casos, pero un
  // usuario puede editar el DOM.  Esta validación en TypeScript es la
  // segunda línea de defensa (la tercera está en el servidor PL/pgSQL).
  guardarPuntaje(finalizar: boolean): void {
    if (!this.turnoActual || this.guardando) return;

    if (this.pMaterial  < 0 || this.pExposicion < 0 || this.pRespuestas < 0) {
      this.toast('Los puntajes no pueden ser negativos.', 'warn'); return;
    }
    if (this.pMaterial  > 10) { this.toast('Material: máximo 10 puntos.', 'warn');    return; }
    if (this.pExposicion > 4) { this.toast('Exposición: máximo 4 puntos.', 'warn');   return; }
    if (this.pRespuestas > 6) { this.toast('Respuestas: máximo 6 puntos.', 'warn');   return; }

    // ── FIX: no enviamos idUsuario; el backend lo extrae del JWT ─────
    this.guardando = true;
    this.svc.registrarPuntaje({
      idEvaluacionOposicion: this.turnoActual.idEvaluacionOposicion,
      idUsuario:             this.idUsuario,  // el servicio lo omite del body
      puntajeMaterial:       this.pMaterial,
      puntajeExposicion:     this.pExposicion,
      puntajeRespuestas:     this.pRespuestas,
      finalizar
    }).subscribe({
      next: res => {
        this.guardando = false;
        if (finalizar) this.yaFinalizo = true;   // bloquear el formulario inmediatamente
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

  // ── FIX: formulario es readonly cuando FINALIZADA _o_ el jurado ya finalizó ─
  // Ambas condiciones son independientes:
  //   - FINALIZADA: el acta está cerrada, nadie puede editar nada.
  //   - yaFinalizo: este jurado específico bloqueó sus notas, aunque el
  //     turno siga EN_CURSO porque los otros jurados no han terminado.
  get formularioReadonly(): boolean {
    if (!this.turnoActual) return true;
    return this.turnoActual.estado === 'FINALIZADA' || this.yaFinalizo;
  }

  get puedeCalificar(): boolean {
    return !!this.turnoActual
      && this.turnoActual.estado === 'EN_CURSO'
      && !this.yaFinalizo
      && !this.esCoord;
  }

  // ── Helpers ───────────────────────────────────────────────────────

  badgeEstado(estado: string): string {
    return ({ PROGRAMADA: 'badge-blue', EN_CURSO: 'badge-amber',
      FINALIZADA: 'badge-green', NO_PRESENTO: 'badge-red' } as any)[estado] ?? 'badge-gray';
  }

  formatHora(h?: string): string  { return h ?? '—'; }

  formatFecha(f?: string): string {
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
