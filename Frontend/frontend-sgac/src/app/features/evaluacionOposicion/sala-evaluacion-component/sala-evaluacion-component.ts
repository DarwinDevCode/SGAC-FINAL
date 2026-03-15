// sala-evaluacion-component.ts — Fix: reset de estado y desbloqueo de formulario
import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormsModule }         from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { ActivatedRoute }      from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';

import { AuthService }                from '../../../core/services/auth-service';
import { EvaluacionOposicionService } from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import { EvaluacionWsClientService }  from '../../../core/services/evaluaciones/evaluacion-ws-client-service';
import { TurnoOposicion }            from '../../../core/models/evaluaciones/EvaluacionOposicion';

type SyncEstado = 'idle' | 'pendiente' | 'guardando' | 'sincronizado' | 'error';

@Component({
  selector: 'app-sala-evaluacion',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './sala-evaluacion-component.html',
  styleUrls: ['./sala-evaluacion-component.css']
})
export class SalaEvaluacionComponent implements OnInit, OnDestroy {

  private route   = inject(ActivatedRoute);
  private authSrv = inject(AuthService);
  private svc     = inject(EvaluacionOposicionService);
  private wsSvc   = inject(EvaluacionWsClientService);

  loading         = true;
  loadingMsg      = 'Cargando sala de evaluación...';
  sinConvocatoria = false;
  mensajeSinSala  = '';
  salaInfo        = '';

  toastMsg  = '';
  toastTipo = 'ok';
  private toastTimer: any;

  syncEstado: SyncEstado = 'idle';
  private syncTimer: any;

  wsEstado$ = this.wsSvc.estado$;
  private wsSub?: Subscription;

  idUsuario  = 0;
  rolUsuario = '';
  esCoord    = false;
  puedeCalificar = false;

  idConvocatoria = 0;

  turnos:      TurnoOposicion[] = [];
  turnoActual: TurnoOposicion | null = null;

  hayEvaluacionEnCurso = false;

  pMaterial   = 0;
  pExposicion = 0;
  pRespuestas = 0;
  guardando   = false;
  yaFinalizo  = false;

  private notasChange$ = new Subject<void>();
  private autoSaveSub?: Subscription;

  readonly BLOQUE_TOTAL_SEG = 35 * 60;
  timerSegundos = 0;
  timerActivo   = false;
  private timerInterval: any;

  // ═══════════════════════════════════════════════════════════════════
  ngOnInit(): void {
    const user = this.authSrv.getUser();
    if (user) {
      this.idUsuario  = user.idUsuario;
      this.rolUsuario = user.rolActual ?? '';
      this.esCoord    = this.rolUsuario.toUpperCase().includes('COORDINADOR');
    }

    this.autoSaveSub = this.notasChange$.pipe(
      debounceTime(2000),
      //distinctUntilChanged(),
      filter(() => !this.formularioReadonly && !this.guardando)
    ).subscribe(() => this.ejecutarAutoGuardado());

    const paramId = this.route.snapshot.paramMap.get('idConvocatoria');
    if (paramId) {
      this.idConvocatoria = Number(paramId);
      this.arrancarSala();
    } else {
      this.loadingMsg = 'Buscando tu sala asignada...';
      this.svc.resolverMiSala().subscribe({
        next: res => {
          if (res.exito && res.idConvocatoria) {
            this.idConvocatoria = res.idConvocatoria;
            this.salaInfo = `${res.nombreAsignatura ?? ''} · ${res.nombreCarrera ?? ''}`;
            this.arrancarSala();
          } else {
            this.loading         = false;
            this.sinConvocatoria = true;
            this.mensajeSinSala  = res.mensaje ?? 'No tienes ninguna comisión asignada.';
          }
        },
        error: (err: Error) => {
          this.loading         = false;
          this.sinConvocatoria = true;
          this.mensajeSinSala  = err.message;
        }
      });
    }
  }

  private arrancarSala(): void {
    this.cargarCronograma();
    this.conectarWebSocket();
  }

  ngOnDestroy(): void {
    clearInterval(this.timerInterval);
    clearTimeout(this.toastTimer);
    clearTimeout(this.syncTimer);
    this.wsSub?.unsubscribe();
    this.autoSaveSub?.unsubscribe();
    this.notasChange$.complete();
    this.wsSvc.desconectar();
  }

  // ═══════════════════════════════════════════════════════════════════
  // CARGA DE DATOS
  // ═══════════════════════════════════════════════════════════════════

  cargarCronograma(): void {
    this.loading    = true;
    this.loadingMsg = 'Recuperando estado desde la base de datos...';
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        this.loading = false;
        this.turnos  = res.cronograma ?? [];
        const enCurso = this.turnos.find(t => t.estado === 'EN_CURSO');
        this.hayEvaluacionEnCurso = !!enCurso;
        if (enCurso) this.seleccionarTurno(enCurso);
      },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); }
    });
  }

  /**
   * FIX RAÍZ 1 — recargarTurnoActual ya NO llama a resetForm().
   *
   * El problema original: resetForm() seteaba puedeCalificar = false y el
   * getter formularioVisible devolvía false durante el tiempo que tardaba
   * la petición HTTP. El formulario "parpadeaba" o directamente no reaparecía
   * si Angular evaluaba el getter justo en ese instante.
   *
   * Ahora: resetForm() solo se llama en seleccionarTurno() (cambio de turno).
   * recargarTurnoActual() únicamente actualiza turnoActual con datos frescos
   * y re-evalúa puedeCalificar y yaFinalizo — sin tocar el estado anterior
   * hasta tener los nuevos datos listos.
   */
  private recargarTurnoActual(onComplete?: () => void): void {
    const cur = this.turnoActual;
    if (!cur) return;
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        const actualizado = (res.cronograma ?? []).find(
          t => t.idEvaluacionOposicion === cur.idEvaluacionOposicion
        );
        if (actualizado) {
          this.turnoActual = actualizado;
          // Con los jurados frescos ya en turnoActual, estas funciones
          // tienen la información real de quién está en la comisión.
          this.detectarYaFinalizoYCargarNotas();
          this.detectarPuedeCalificar();
          onComplete?.();
        }
      },
      error: () => {}
    });
  }

  /**
   * FIX RAÍZ 2 — seleccionarTurno hace hard reset COMPLETO.
   *
   * Al cambiar de turno, todo el estado anterior debe quedar en cero antes
   * de asignar el nuevo. De lo contrario puedeCalificar o yaFinalizo pueden
   * pertenecer al turno anterior mientras se carga el nuevo.
   */
  seleccionarTurno(turno: TurnoOposicion): void {
    // Hard reset mandatorio — orden: primero limpiar, luego asignar
    clearInterval(this.timerInterval);
    this.timerSegundos   = 0;
    this.timerActivo     = false;
    this.pMaterial       = 0;
    this.pExposicion     = 0;
    this.pRespuestas     = 0;
    this.yaFinalizo      = false;
    this.puedeCalificar  = false;   // FIX: forzar re-evaluación con datos del nuevo turno
    this.syncEstado      = 'idle';

    this.turnoActual = turno;

    if (turno.estado === 'EN_CURSO') {
      // Cargar jurados frescos desde BD → re-evalúa puedeCalificar
      // Timer arranca dentro del callback para usar serverTimestamp correcto
      this.recargarTurnoActual(() => {
        this.iniciarTimerDesdeServidor(turno.serverTimestamp, turno.horaInicioReal);
      });
    } else if (turno.estado === 'FINALIZADA') {
      this.detectarYaFinalizoYCargarNotas();
      this.detectarPuedeCalificar();
    } else {
      // PROGRAMADA / NO_PRESENTO
      this.detectarPuedeCalificar();
    }
  }

  private detectarPuedeCalificar(): void {
    const cur = this.turnoActual;
    if (!cur) { this.puedeCalificar = false; return; }
    this.puedeCalificar = cur.jurados.some(j => j.idUsuario === this.idUsuario);
  }

  private detectarYaFinalizoYCargarNotas(): void {
    const cur = this.turnoActual;
    if (!cur) { this.yaFinalizo = false; return; }
    const miJurado = cur.jurados.find(j => j.idUsuario === this.idUsuario);
    this.yaFinalizo = miJurado?.finalizo ?? false;
    if (miJurado) {
      this.pMaterial   = miJurado.puntajeMaterial   ?? 0;
      this.pExposicion = miJurado.puntajeExposicion ?? 0;
      this.pRespuestas = miJurado.puntajeRespuestas ?? 0;
    }
  }

  // ═══════════════════════════════════════════════════════════════════
  // WEBSOCKET
  // ═══════════════════════════════════════════════════════════════════

  private conectarWebSocket(): void {
    this.wsSvc.conectar(this.idConvocatoria);

    this.wsSub = this.wsSvc.mensajes$.subscribe(msg => {
      switch (msg.tipo) {

        case 'CAMBIO_ESTADO': {
          this.turnos = this.turnos.map(t =>
            t.idEvaluacionOposicion === msg.idEvaluacionOposicion
              ? { ...t,
                estado:          msg.nuevoEstado     ?? t.estado,
                nombreEstado:    msg.nombreEstado    ?? t.nombreEstado,
                horaInicioReal:  msg.horaInicioReal  ?? t.horaInicioReal,
                serverTimestamp: msg.serverTimestamp ?? t.serverTimestamp,
                horaFinReal:     msg.horaFinReal     ?? t.horaFinReal,
                puntajeFinal:    msg.puntajeFinal    ?? t.puntajeFinal,
              } as TurnoOposicion
              : t
          );
          this.hayEvaluacionEnCurso = this.turnos.some(t => t.estado === 'EN_CURSO');

          const current = this.turnoActual;
          if (current && current.idEvaluacionOposicion === msg.idEvaluacionOposicion) {
            const estadoAnterior = current.estado;

            this.turnoActual = {
              ...current,
              estado:          msg.nuevoEstado     ?? current.estado,
              nombreEstado:    msg.nombreEstado    ?? current.nombreEstado,
              horaInicioReal:  msg.horaInicioReal  ?? current.horaInicioReal,
              serverTimestamp: msg.serverTimestamp ?? current.serverTimestamp,
              horaFinReal:     msg.horaFinReal     ?? current.horaFinReal,
              puntajeFinal:    msg.puntajeFinal    ?? current.puntajeFinal,
            } as TurnoOposicion;

            if (estadoAnterior === 'PROGRAMADA' && msg.nuevoEstado === 'EN_CURSO') {
              // FIX RAÍZ 4 — WS no envía jurados; hay que ir a BD.
              // El timer arranca inmediatamente con el serverTimestamp del mensaje.
              // recargarTurnoActual() llega justo después con los jurados reales
              // y activa puedeCalificar en el callback.
              this.iniciarTimerDesdeServidor(msg.serverTimestamp, msg.horaInicioReal);
              this.recargarTurnoActual(() => {
                if (this.puedeCalificar) {
                  this.toast('¡La evaluación ha iniciado! Ya puedes calificar.', 'ok');
                }
              });
            }

            if (msg.nuevoEstado === 'FINALIZADA') {
              clearInterval(this.timerInterval);
              this.timerActivo          = false;
              this.hayEvaluacionEnCurso = false;
              this.recargarTurnoActual(() => {
                if (msg.puntajeFinal != null) {
                  this.toast(`Acta cerrada. Nota final: ${msg.puntajeFinal}`, 'ok');
                }
              });
            }
          }
          break;
        }

        case 'PUNTAJE_ACTUALIZADO': {
          const cur = this.turnoActual;
          if (cur && cur.idEvaluacionOposicion === msg.idEvaluacionOposicion) {
            if (msg.todosFinalizaron) {
              clearInterval(this.timerInterval);
              this.timerActivo          = false;
              this.hayEvaluacionEnCurso = false;
              this.turnos = this.turnos.map(t =>
                t.idEvaluacionOposicion === msg.idEvaluacionOposicion
                  ? { ...t, estado: 'FINALIZADA', nombreEstado: 'Finalizada',
                    puntajeFinal: msg.puntajeFinal ?? t.puntajeFinal } as TurnoOposicion
                  : t
              );
              this.recargarTurnoActual(() => {
                if (msg.puntajeFinal != null) {
                  this.toast(`Todos los jurados finalizaron. Nota final: ${msg.puntajeFinal}`, 'ok');
                }
              });
            } else {
              this.recargarTurnoActual();
            }
          }
          break;
        }
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // TIMER
  // ═══════════════════════════════════════════════════════════════════

  iniciarTimerDesdeServidor(serverTimestamp?: string, horaFallback?: string): void {
    clearInterval(this.timerInterval);
    this.timerActivo = true;

    if (serverTimestamp) {
      const serverEpoch = new Date(serverTimestamp).getTime();
      const calcular = () => {
        const transcurrido = Math.floor((Date.now() - serverEpoch) / 1000);
        this.timerSegundos = Math.max(0, Math.min(transcurrido, this.BLOQUE_TOTAL_SEG));
      };
      calcular();
      this.timerInterval = setInterval(calcular, 1000);
    } else if (horaFallback) {
      const calcular = () => {
        const ahora = new Date();
        const [h, m, s] = horaFallback.split(':').map(Number);
        const inicio = new Date(ahora);
        inicio.setHours(h, m, s ?? 0, 0);
        const diff = Math.floor((ahora.getTime() - inicio.getTime()) / 1000);
        this.timerSegundos = Math.max(0, Math.min(diff, this.BLOQUE_TOTAL_SEG));
      };
      calcular();
      this.timerInterval = setInterval(calcular, 1000);
    }
  }

  get tiempoRestante():    string { const r = Math.max(0, this.BLOQUE_TOTAL_SEG - this.timerSegundos); return `${Math.floor(r / 60).toString().padStart(2, '0')}:${(r % 60).toString().padStart(2, '0')}`; }
  get timerPorcentaje():   number { return Math.min(100, (this.timerSegundos / this.BLOQUE_TOTAL_SEG) * 100); }
  get svgCircunferencia(): number { return 2 * Math.PI * 44; }
  get svgDashoffset():     number { return this.svgCircunferencia * (1 - this.timerPorcentaje / 100); }

  get segmentoActual(): 'expo' | 'preguntas' | 'transicion' | 'fin' {
    const min = this.timerSegundos / 60;
    if (min < 20) return 'expo';
    if (min < 30) return 'preguntas';
    if (min < 35) return 'transicion';
    return 'fin';
  }
  get colorTimer():    string { return { expo: '#15803d', preguntas: '#1d4ed8', transicion: '#d97706', fin: '#dc2626' }[this.segmentoActual]; }
  get labelSegmento(): string { return { expo: 'Exposición (20 min)', preguntas: 'Preguntas (10 min)', transicion: 'Transición (5 min)', fin: 'Tiempo agotado' }[this.segmentoActual]; }

  // ═══════════════════════════════════════════════════════════════════
  // ACCIONES DEL COORDINADOR
  // ═══════════════════════════════════════════════════════════════════

  /**
   * FIX RAÍZ 3 — iniciarTurnoActual usa la respuesta REST para el timer.
   *
   * El flujo correcto tras un Iniciar exitoso:
   *   1. recargarTurnoActual() → puebla jurados → detectarPuedeCalificar() → formulario visible
   *   2. dentro del callback, arrancar el timer con serverTimestamp de la respuesta
   *
   * Antes el timer nunca arrancaba para el Coordinador porque la respuesta
   * del servidor no se usaba. Ahora se extrae serverTimestamp / horaReal
   * directamente del OposicionResponse.
   */
  iniciarTurnoActual(): void {
    const cur = this.turnoActual;
    if (!cur) return;
    this.svc.iniciarEvaluacion(cur.idEvaluacionOposicion, this.idConvocatoria).subscribe({
      next: res => {
        this.toast('Evaluación iniciada. ¡El tribunal puede calificar!', 'ok');
        // Recargar para poblar jurados del nuevo turno antes de evaluar
        // puedeCalificar. El timer arranca en el callback con el timestamp
        // exacto que acaba de registrar el servidor.
        this.recargarTurnoActual(() => {
          const serverTs = (res as any).serverTimestamp as string | undefined;
          const horaReal = res.horaReal;
          this.iniciarTimerDesdeServidor(serverTs, horaReal);
        });
      },
      error: (err: Error) => this.toast(err.message, 'err')
    });
  }

  finalizarTurnoActual(): void {
    const cur = this.turnoActual;
    if (!cur) return;
    this.svc.finalizarEvaluacion(cur.idEvaluacionOposicion, this.idConvocatoria).subscribe({
      next: res => {
        this.hayEvaluacionEnCurso = false;
        clearInterval(this.timerInterval);
        this.timerActivo = false;
        this.toast(`Acta cerrada. Nota final: ${res.puntajeFinal}`, 'ok');
        this.recargarTurnoActual();
      },
      error: (err: Error) => this.toast(err.message, 'err')
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // GETTERS DE VISIBILIDAD
  // ═══════════════════════════════════════════════════════════════════

  get formularioVisible(): boolean {
    if (!this.turnoActual || !this.puedeCalificar) return false;
    const e = this.turnoActual.estado;
    return e === 'EN_CURSO' || e === 'FINALIZADA';
  }

  get formularioReadonly(): boolean {
    if (!this.turnoActual) return true;
    return this.turnoActual.estado === 'FINALIZADA' || this.yaFinalizo;
  }

  // ═══════════════════════════════════════════════════════════════════
  // AUTO-GUARDADO
  // ═══════════════════════════════════════════════════════════════════

  onNotaChange(): void {
    if (this.formularioReadonly) return;
    this.syncEstado = 'pendiente';
    this.notasChange$.next();
  }

  private ejecutarAutoGuardado(): void {
    const cur = this.turnoActual;
    if (!cur || this.formularioReadonly || this.guardando) return;
    if (!this.rangosValidos()) return;
    this.syncEstado = 'guardando';
    this.svc.registrarPuntaje({
      idEvaluacionOposicion: cur.idEvaluacionOposicion,
      idUsuario:             this.idUsuario,
      puntajeMaterial:       this.pMaterial,
      puntajeExposicion:     this.pExposicion,
      puntajeRespuestas:     this.pRespuestas,
      finalizar:             false,
      idConvocatoria:        this.idConvocatoria,
    }).subscribe({
      next: () => {
        this.syncEstado = 'sincronizado';
        clearTimeout(this.syncTimer);
        this.syncTimer = setTimeout(() => this.syncEstado = 'idle', 3000);
      },
      error: () => {
        this.syncEstado = 'error';
        clearTimeout(this.syncTimer);
        this.syncTimer = setTimeout(() => this.syncEstado = 'idle', 5000);
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // GUARDADO MANUAL
  // ═══════════════════════════════════════════════════════════════════

  get subtotal(): number { return (this.pMaterial || 0) + (this.pExposicion || 0) + (this.pRespuestas || 0); }

  private rangosValidos(): boolean {
    return this.pMaterial  >= 0 && this.pMaterial  <= 10
      && this.pExposicion >= 0 && this.pExposicion <= 4
      && this.pRespuestas >= 0 && this.pRespuestas <= 6;
  }

  guardarPuntaje(finalizar: boolean): void {
    const cur = this.turnoActual;
    if (!cur || this.guardando) return;
    if (!this.rangosValidos()) {
      if (this.pMaterial  > 10) { this.toast('Material: máximo 10 puntos.',  'warn'); return; }
      if (this.pExposicion > 4) { this.toast('Exposición: máximo 4 puntos.', 'warn'); return; }
      if (this.pRespuestas > 6) { this.toast('Respuestas: máximo 6 puntos.', 'warn'); return; }
      this.toast('Los puntajes no pueden ser negativos.', 'warn');
      return;
    }
    this.guardando  = true;
    this.syncEstado = 'guardando';
    this.svc.registrarPuntaje({
      idEvaluacionOposicion: cur.idEvaluacionOposicion,
      idUsuario:             this.idUsuario,
      puntajeMaterial:       this.pMaterial,
      puntajeExposicion:     this.pExposicion,
      puntajeRespuestas:     this.pRespuestas,
      finalizar,
      idConvocatoria:        this.idConvocatoria,
    }).subscribe({
      next: () => {
        this.guardando  = false;
        this.syncEstado = 'sincronizado';
        if (finalizar) {
          this.yaFinalizo = true;
          this.toast('Calificación finalizada y bloqueada.', 'ok');
        }
        clearTimeout(this.syncTimer);
        this.syncTimer = setTimeout(() => this.syncEstado = 'idle', 3000);
      },
      error: (err: Error) => {
        this.guardando  = false;
        this.syncEstado = 'error';
        this.toast(err.message, 'err');
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────

  badgeEstado(e: string): string {
    return ({ PROGRAMADA: 'badge-blue', EN_CURSO: 'badge-amber', FINALIZADA: 'badge-green', NO_PRESENTO: 'badge-red' } as any)[e] ?? 'badge-gray';
  }
  formatHora(h?: string):  string { return h ?? '—'; }
  formatFecha(f?: string): string { if (!f) return '—'; const [y, m, d] = f.split('-'); return `${d}/${m}/${y}`; }

  private toast(msg: string, tipo: 'ok' | 'err' | 'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg  = msg;
    this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', tipo === 'err' ? 9000 : 4500);
  }
}
