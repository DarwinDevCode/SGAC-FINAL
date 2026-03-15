// sala-evaluacion-component.ts — Refactorización Total
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

// Estado de sincronización del auto-guardado
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

  // ── Estado de carga ───────────────────────────────────────────────
  loading         = true;
  loadingMsg      = 'Cargando sala de evaluación...';
  sinConvocatoria = false;
  mensajeSinSala  = '';
  salaInfo        = '';

  // ── Toasts (solo para acciones manuales importantes) ──────────────
  toastMsg  = '';
  toastTipo = 'ok';
  private toastTimer: any;

  // ── Indicador de sincronización silencioso (auto-save) ────────────
  syncEstado: SyncEstado = 'idle';
  private syncTimer: any;

  // ── WebSocket ─────────────────────────────────────────────────────
  wsEstado$ = this.wsSvc.estado$;
  private wsSub?: Subscription;

  // ── Usuario autenticado ───────────────────────────────────────────
  idUsuario  = 0;
  rolUsuario = '';
  esCoord    = false;       // controla botones Iniciar/Cerrar Acta
  puedeCalificar = false;   // true si el usuario está en la comisión

  // ── Convocatoria ──────────────────────────────────────────────────
  idConvocatoria = 0;

  // ── Turnos ────────────────────────────────────────────────────────
  turnos:      TurnoOposicion[] = [];
  turnoActual: TurnoOposicion | null = null;

  /** Expuesto para el CanDeactivate guard */
  hayEvaluacionEnCurso = false;

  // ── Formulario de notas ───────────────────────────────────────────
  pMaterial   = 0;
  pExposicion = 0;
  pRespuestas = 0;
  guardando   = false;
  yaFinalizo  = false;

  // ── Auto-guardado reactivo ────────────────────────────────────────
  private notasChange$ = new Subject<void>();
  private autoSaveSub?: Subscription;

  // ── Timer ─────────────────────────────────────────────────────────
  /** Duración total del bloque: 20 expo + 10 preguntas + 5 transición */
  readonly BLOQUE_TOTAL_SEG = 35 * 60;
  timerSegundos = 0;
  timerActivo   = false;
  private timerInterval: any;

  // ═══════════════════════════════════════════════════════════════════
  // CICLO DE VIDA
  // ═══════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    const user = this.authSrv.getUser();
    if (user) {
      this.idUsuario  = user.idUsuario;
      this.rolUsuario = user.rolActual ?? '';
      this.esCoord    = this.rolUsuario.toUpperCase().includes('COORDINADOR');
    }

    // Auto-guardado silencioso: 2 s de inactividad → borrador automático
    this.autoSaveSub = this.notasChange$.pipe(
      debounceTime(2000),
      distinctUntilChanged(),
      filter(() => !this.formularioReadonly && !this.guardando)
    ).subscribe(() => this.ejecutarAutoGuardado());

    // Resolver convocatoria
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
    // Req. 1: carga mandatoria desde BD (SSoT) antes de conectar WS
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
  // CARGA DE DATOS (SSoT — Single Source of Truth)
  // ═══════════════════════════════════════════════════════════════════

  cargarCronograma(): void {
    this.loading    = true;
    this.loadingMsg = 'Recuperando estado desde la base de datos...';

    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        this.loading = false;
        this.turnos  = res.cronograma ?? [];

        // Req. 1: si hay turno EN_CURSO, activar de inmediato sin esperar WS
        const enCurso = this.turnos.find(t => t.estado === 'EN_CURSO');
        this.hayEvaluacionEnCurso = !!enCurso;
        if (enCurso) {
          this.seleccionarTurno(enCurso);
        }
      },
      error: (err: Error) => {
        this.loading = false;
        this.toast(err.message, 'err');
      }
    });
  }

  /** Recarga el turno activo desde BD para obtener la lista real de jurados.
   *  Fix: el mensaje WS de CAMBIO_ESTADO no envía el array `jurados`, por lo que
   *  siempre hay que ir a BD para recuperarlo y poder evaluar puedeCalificar.
   *  @param onComplete callback opcional que se ejecuta tras actualizar el turno.
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
          // Fix 4: limpiar notas del turno anterior ANTES de cargar las del nuevo
          this.resetForm();
          this.turnoActual = actualizado;
          // Ahora jurados está completo → detectarPuedeCalificar() funciona correctamente
          this.detectarYaFinalizoYCargarNotas();
          this.detectarPuedeCalificar();
          onComplete?.();
        }
      },
      error: () => {}
    });
  }

  seleccionarTurno(turno: TurnoOposicion): void {
    // Fix 2: hard reset total antes de cargar cualquier dato del nuevo turno.
    // Orden obligatorio: primero limpiar, luego asignar.
    // Esto elimina el "estado zombi" donde el timer o las notas del turno
    // anterior siguen visibles durante el cambio.
    clearInterval(this.timerInterval);  // detener timer anterior de forma mandatoria
    this.timerSegundos  = 0;
    this.timerActivo    = false;
    this.resetForm();                   // pMaterial/pExposicion/pRespuestas = 0, flags = false

    this.turnoActual = turno;

    // Fix 3: ramificación por estado REAL que viene de la BD.
    // No iniciar timer si el estado ya no es EN_CURSO.
    if (turno.estado === 'EN_CURSO') {
      // Cargar notas previas y detectar acceso al formulario
      this.detectarYaFinalizoYCargarNotas();
      this.detectarPuedeCalificar();
      // Timer desde serverTimestamp del servidor (fórmula maestra anti-deriva)
      this.iniciarTimerDesdeServidor(turno.serverTimestamp, turno.horaInicioReal);

    } else if (turno.estado === 'FINALIZADA') {
      // Turno ya cerrado: cargar notas históricas y bloquear formulario.
      // No se toca el timer (queda en 0 por el hard reset de arriba).
      this.detectarYaFinalizoYCargarNotas();
      this.detectarPuedeCalificar();

    } else {
      // PROGRAMADA / NO_PRESENTO: no hay notas ni timer que restaurar
      this.detectarPuedeCalificar();
    }
  }

  /**
   * Req. 3 + Req. 1: verifica si el usuario es miembro del tribunal.
   * Aplica para Coordinadora, Decano y Docente por igual.
   */
  private detectarPuedeCalificar(): void {
    const cur = this.turnoActual;
    if (!cur) { this.puedeCalificar = false; return; }
    this.puedeCalificar = cur.jurados.some(j => j.idUsuario === this.idUsuario);
  }

  /**
   * Req. 1: Pre-carga las notas parciales desde usuario_comision (vía cronograma).
   * El jurado nunca verá el formulario vacío tras F5 o navegación.
   */
  private detectarYaFinalizoYCargarNotas(): void {
    const cur = this.turnoActual;
    if (!cur) { this.yaFinalizo = false; return; }

    const miJurado = cur.jurados.find(j => j.idUsuario === this.idUsuario);
    this.yaFinalizo = miJurado?.finalizo ?? false;

    if (miJurado) {
      // Restaurar los puntajes guardados en BD — no queda formulario vacío
      this.pMaterial   = miJurado.puntajeMaterial   ?? 0;
      this.pExposicion = miJurado.puntajeExposicion ?? 0;
      this.pRespuestas = miJurado.puntajeRespuestas ?? 0;
    }
  }

  private resetForm(): void {
    this.pMaterial     = 0;
    this.pExposicion   = 0;
    this.pRespuestas   = 0;
    this.yaFinalizo    = false;
    this.puedeCalificar = false;
    this.syncEstado    = 'idle';
  }

  // ═══════════════════════════════════════════════════════════════════
  // WEBSOCKET — Req. 5
  // ═══════════════════════════════════════════════════════════════════

  private conectarWebSocket(): void {
    this.wsSvc.conectar(this.idConvocatoria);

    this.wsSub = this.wsSvc.mensajes$.subscribe(msg => {
      switch (msg.tipo) {

        case 'CAMBIO_ESTADO': {
          // Actualizar lista lateral
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

            // Fix: al recibir EN_CURSO, el mensaje WS NO incluye el array de jurados.
            // Si llamamos detectarPuedeCalificar() sobre el objeto parcial del spread,
            // jurados estará vacío → puedeCalificar = false → formulario invisible.
            // Solución: arrancar el timer ya (serverTimestamp viene en el mensaje)
            // y forzar una recarga desde BD para obtener el array jurados completo.
            // recargarTurnoActual() llama a detectarPuedeCalificar() en su callback.
            if (estadoAnterior === 'PROGRAMADA' && msg.nuevoEstado === 'EN_CURSO') {
              // Timer arranca inmediatamente con el timestamp del servidor
              this.iniciarTimerDesdeServidor(msg.serverTimestamp, msg.horaInicioReal);
              // Recarga desde BD → puebla jurados → detectarPuedeCalificar() → formulario visible
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
              // Recargar para obtener puntajeFinal y bloquear formulario vía BD
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
            // Fix 1: si todos finalizaron, el WS indica que el turno pasó a FINALIZADA.
            // Aplicar el cambio de estado localmente SIN esperar F5:
            //   · detener el timer
            //   · marcar el turno como FINALIZADA en la lista lateral y en turnoActual
            //   · recargar desde BD para obtener puntajes finales y bloquear el formulario
            if (msg.todosFinalizaron) {
              clearInterval(this.timerInterval);
              this.timerActivo          = false;
              this.hayEvaluacionEnCurso = false;

              // Actualizar la lista lateral instantáneamente
              this.turnos = this.turnos.map(t =>
                t.idEvaluacionOposicion === msg.idEvaluacionOposicion
                  ? { ...t, estado: 'FINALIZADA', nombreEstado: 'Finalizada',
                    puntajeFinal: msg.puntajeFinal ?? t.puntajeFinal } as TurnoOposicion
                  : t
              );

              // Recarga completa para obtener notas definitivas y bloquear formulario
              this.recargarTurnoActual(() => {
                if (msg.puntajeFinal != null) {
                  this.toast(`Todos los jurados finalizaron. Nota final: ${msg.puntajeFinal}`, 'ok');
                }
              });
            } else {
              // Actualización parcial: solo refrescar las tarjetas de puntajes
              this.recargarTurnoActual();
            }
          }
          break;
        }
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // TIMER MAESTRO — Req. 1
  //
  // Fórmula obligatoria (Anti-Deriva):
  //   T_restante = BLOQUE - ((Date.now() - new Date(serverTimestamp)) / 1000)
  //
  // Todos los miembros ven el mismo segundo exacto porque el cálculo
  // es relativo al timestamp del servidor, nunca a un contador local.
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
      // Fallback para backends sin serverTimestamp (compatibilidad)
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

  get tiempoRestante(): string {
    const r = Math.max(0, this.BLOQUE_TOTAL_SEG - this.timerSegundos);
    return `${Math.floor(r / 60).toString().padStart(2, '0')}:${(r % 60).toString().padStart(2, '0')}`;
  }

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

  get colorTimer(): string {
    return { expo: '#15803d', preguntas: '#1d4ed8', transicion: '#d97706', fin: '#dc2626' }[this.segmentoActual];
  }

  get labelSegmento(): string {
    return { expo: 'Exposición (20 min)', preguntas: 'Preguntas (10 min)', transicion: 'Transición (5 min)', fin: 'Tiempo agotado' }[this.segmentoActual];
  }

  // ═══════════════════════════════════════════════════════════════════
  // ACCIONES DEL COORDINADOR
  // ═══════════════════════════════════════════════════════════════════

  iniciarTurnoActual(): void {
    const cur = this.turnoActual;
    if (!cur) return;
    this.svc.iniciarEvaluacion(cur.idEvaluacionOposicion, this.idConvocatoria).subscribe({
      next: () => {
        this.toast('Evaluación iniciada. ¡El tribunal puede calificar!', 'ok');
        // Fix 3: recargar desde BD para que el formulario de la coordinadora
        // aparezca al instante sin necesidad de que llegue el mensaje WS.
        // recargarTurnoActual() puebla jurados y activa puedeCalificar.
        this.recargarTurnoActual();
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
        // Fix 4: el coordinador NO recibe su propio mensaje WS de vuelta por
        // limitaciones del broker STOMP. Recargar desde BD para que el formulario
        // pase a modo readonly instantáneamente, igual que los demás miembros.
        this.recargarTurnoActual();
      },
      error: (err: Error) => this.toast(err.message, 'err')
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // FORMULARIO — Getters de visibilidad (Req. 3)
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
  // AUTO-GUARDADO REACTIVO — Req. 2
  //
  // Cada cambio en los inputs emite a notasChange$.
  // debounceTime(2000) espera 2 s de inactividad, luego guarda
  // silenciosamente sin toast. Solo un indicador visual mínimo.
  // ═══════════════════════════════════════════════════════════════════

  /** Llamado desde (ngModelChange) en cada input del formulario */
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

    // Req. 4: idUsuario extraído del AuthService, nunca del formulario
    this.svc.registrarPuntaje({
      idEvaluacionOposicion: cur.idEvaluacionOposicion,
      idUsuario:             this.idUsuario,   // el servicio lo stripea del body
      puntajeMaterial:       this.pMaterial,
      puntajeExposicion:     this.pExposicion,
      puntajeRespuestas:     this.pRespuestas,
      finalizar:             false,            // siempre borrador en auto-save
      idConvocatoria:        this.idConvocatoria,
    }).subscribe({
      next: () => {
        // Req. 2: guardado silencioso — solo actualizar indicador visual
        this.syncEstado = 'sincronizado';
        // Limpiar indicador después de 3 s
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
  // GUARDADO MANUAL — Req. 4
  // ═══════════════════════════════════════════════════════════════════

  get subtotal(): number {
    return (this.pMaterial || 0) + (this.pExposicion || 0) + (this.pRespuestas || 0);
  }

  private rangosValidos(): boolean {
    return this.pMaterial  >= 0 && this.pMaterial  <= 10
      && this.pExposicion >= 0 && this.pExposicion <= 4
      && this.pRespuestas >= 0 && this.pRespuestas <= 6;
  }

  guardarPuntaje(finalizar: boolean): void {
    const cur = this.turnoActual;
    if (!cur || this.guardando) return;

    if (!this.rangosValidos()) {
      if (this.pMaterial  > 10) { this.toast('Material: máximo 10 puntos.',   'warn'); return; }
      if (this.pExposicion > 4) { this.toast('Exposición: máximo 4 puntos.',  'warn'); return; }
      if (this.pRespuestas > 6) { this.toast('Respuestas: máximo 6 puntos.',  'warn'); return; }
      this.toast('Los puntajes no pueden ser negativos.', 'warn');
      return;
    }

    this.guardando = true;
    this.syncEstado = 'guardando';

    // Req. 4: payload íntegro — idUsuario del AuthService
    this.svc.registrarPuntaje({
      idEvaluacionOposicion: cur.idEvaluacionOposicion,
      idUsuario:             this.idUsuario,
      puntajeMaterial:       this.pMaterial,
      puntajeExposicion:     this.pExposicion,
      puntajeRespuestas:     this.pRespuestas,
      finalizar,
      idConvocatoria:        this.idConvocatoria,
    }).subscribe({
      next: res => {
        this.guardando  = false;
        this.syncEstado = 'sincronizado';
        if (finalizar) {
          this.yaFinalizo = true;
          // Req. 2: toast SOLO al finalizar manualmente
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
  formatFecha(f?: string): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  private toast(msg: string, tipo: 'ok' | 'err' | 'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg  = msg;
    this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', tipo === 'err' ? 9000 : 4500);
  }
}
