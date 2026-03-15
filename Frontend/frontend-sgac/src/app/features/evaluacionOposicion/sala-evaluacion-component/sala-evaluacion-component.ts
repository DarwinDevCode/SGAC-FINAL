// sala-evaluacion-component.ts — Refactorización completa
import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormsModule }         from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { ActivatedRoute }      from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { AuthService }                from '../../../core/services/auth-service';
import { EvaluacionOposicionService } from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import { EvaluacionWsClientService }  from '../../../core/services/evaluaciones/evaluacion-ws-client-service';
import { TurnoOposicion }            from '../../../core/models/evaluaciones/EvaluacionOposicion';

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

  // ── Estado general ────────────────────────────────────────────────
  loading     = true;
  loadingMsg  = 'Cargando sala de evaluación...';
  toastMsg    = '';
  toastTipo   = 'ok';
  private toastTimer: any;

  sinConvocatoria = false;
  mensajeSinSala  = '';
  salaInfo        = '';

  // ── Conexión WS ───────────────────────────────────────────────────
  wsEstado$  = this.wsSvc.estado$;
  private wsSub?: Subscription;

  // ── Usuario autenticado ───────────────────────────────────────────
  idUsuario  = 0;
  rolUsuario = '';

  // FIX 4: La coordinadora también califica — esCoord solo controla
  // si se muestran los botones de control (Iniciar/Cerrar), pero
  // puedeCalificar es independiente y se activa para TODOS los miembros.
  esCoord         = false;
  puedeCalificar  = false;   // true cuando el usuario está en la comisión

  idConvocatoria = 0;

  // ── Turnos ────────────────────────────────────────────────────────
  turnos:      TurnoOposicion[] = [];
  turnoActual: TurnoOposicion | null = null;

  /** true si hay evaluación EN_CURSO — usado por CanDeactivate (FIX 5) */
  hayEvaluacionEnCurso = false;

  // ── Formulario de notas ───────────────────────────────────────────
  pMaterial   = 0;
  pExposicion = 0;
  pRespuestas = 0;
  guardando   = false;
  yaFinalizo  = false;

  // FIX 3: Auto-guardado con debounce
  private notasChange$ = new Subject<void>();
  private autoSaveSub?: Subscription;

  // ── Timer ─────────────────────────────────────────────────────────
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

    // FIX 3: Configurar auto-guardado con 500ms de debounce
    this.autoSaveSub = this.notasChange$.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => this.autoGuardar());

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
            this.salaInfo       = `${res.nombreAsignatura ?? ''} · ${res.nombreCarrera ?? ''}`;
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
    this.wsSub?.unsubscribe();
    this.autoSaveSub?.unsubscribe();
    this.notasChange$.complete();
    this.wsSvc.desconectar();
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

          // Actualizar hayEvaluacionEnCurso para el guard (FIX 5)
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

            // FIX 4: activar formulario para TODOS los miembros (incl. Coordinadora)
            if (estadoAnterior === 'PROGRAMADA' && msg.nuevoEstado === 'EN_CURSO') {
              this.detectarPuedeCalificar();
              if (this.puedeCalificar) {
                this.toast('La evaluación ha iniciado. ¡Ya puedes calificar!', 'ok');
              }
              // FIX 2: timer con serverTimestamp del servidor
              this.iniciarTimer(msg.serverTimestamp, msg.horaInicioReal);
            }

            if (msg.nuevoEstado === 'FINALIZADA' && msg.puntajeFinal != null) {
              clearInterval(this.timerInterval);
              this.hayEvaluacionEnCurso = false;
              this.toast(`Acta cerrada. Nota final: ${msg.puntajeFinal}`, 'ok');
            }
          }
          break;
        }

        case 'PUNTAJE_ACTUALIZADO': {
          const cur = this.turnoActual;
          if (cur && cur.idEvaluacionOposicion === msg.idEvaluacionOposicion) {
            this.recargarTurnoActual();
          }
          if (msg.todosFinalizaron && msg.puntajeFinal != null) {
            this.toast(`Todos los jurados finalizaron. Nota final: ${msg.puntajeFinal}`, 'ok');
          }
          break;
        }
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // CARGA DE DATOS
  // ═══════════════════════════════════════════════════════════════════

  cargarCronograma(): void {
    this.loading    = true;
    this.loadingMsg = 'Cargando cronograma...';
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        this.loading = false;
        this.turnos  = res.cronograma ?? [];

        // FIX 3 + FIX 5: Detectar turno EN_CURSO al cargar (efecto F5)
        const enCurso = this.turnos.find(t => t.estado === 'EN_CURSO');
        this.hayEvaluacionEnCurso = !!enCurso;
        if (enCurso) this.seleccionarTurno(enCurso);
      },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); }
    });
  }

  private recargarTurnoActual(): void {
    const cur = this.turnoActual;
    if (!cur) return;
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        const actualizado = (res.cronograma ?? []).find(
          t => t.idEvaluacionOposicion === cur.idEvaluacionOposicion
        );
        if (actualizado) {
          this.turnoActual = actualizado;
          this.detectarYaFinalizo();
          this.detectarPuedeCalificar();
        }
      },
      error: () => {}
    });
  }

  seleccionarTurno(turno: TurnoOposicion): void {
    this.turnoActual = turno;
    this.resetForm();
    this.detectarYaFinalizo();
    this.detectarPuedeCalificar();

    clearInterval(this.timerInterval);
    if (turno.estado === 'EN_CURSO') {
      // FIX 2: usar serverTimestamp para sincronización maestra
      this.iniciarTimer(turno.serverTimestamp, turno.horaInicioReal);
    } else {
      this.timerSegundos = 0;
      this.timerActivo   = false;
    }
  }

  /** FIX 4: determina si el usuario logueado es miembro del tribunal
   *  independientemente de si es Coordinadora, Decano o Docente. */
  private detectarPuedeCalificar(): void {
    const cur = this.turnoActual;
    if (!cur) { this.puedeCalificar = false; return; }
    const esMiembro = cur.jurados.some(j => j.idUsuario === this.idUsuario);
    this.puedeCalificar = esMiembro;
  }

  private detectarYaFinalizo(): void {
    const cur = this.turnoActual;
    if (!cur) { this.yaFinalizo = false; return; }
    const miJurado = cur.jurados.find(j => j.idUsuario === this.idUsuario);
    this.yaFinalizo = miJurado?.finalizo ?? false;
    // FIX 3: pre-cargar notas guardadas para no perder el borrador
    if (miJurado) {
      this.pMaterial   = miJurado.puntajeMaterial   ?? 0;
      this.pExposicion = miJurado.puntajeExposicion ?? 0;
      this.pRespuestas = miJurado.puntajeRespuestas ?? 0;
    }
  }

  private resetForm(): void {
    this.pMaterial = this.pExposicion = this.pRespuestas = 0;
    this.yaFinalizo = false;
    this.puedeCalificar = false;
  }

  // ═══════════════════════════════════════════════════════════════════
  // FIX 2: TIMER MAESTRO — fórmula obligatoria
  //
  //   Tiempo_Restante = BLOQUE - (Date.now() - new Date(serverTimestamp)) / 1000
  //
  // Todos los usuarios ven el mismo segundo exacto porque el cálculo
  // siempre es relativo al timestamp del servidor, no a un contador local.
  // ═══════════════════════════════════════════════════════════════════

  iniciarTimer(serverTimestamp?: string, horaFallback?: string): void {
    clearInterval(this.timerInterval);
    this.timerActivo = true;

    if (serverTimestamp) {
      const serverEpoch = new Date(serverTimestamp).getTime();
      const calcular = () => {
        // Fórmula maestra: tiempo basado en diferencia real con el servidor
        const transcurrido = Math.floor((Date.now() - serverEpoch) / 1000);
        this.timerSegundos = Math.max(0, Math.min(transcurrido, this.BLOQUE_TOTAL_SEG));
      };
      calcular();
      this.timerInterval = setInterval(calcular, 1000);

    } else if (horaFallback) {
      // Fallback para backends sin serverTimestamp
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
    return `${Math.floor(r / 60).toString().padStart(2,'0')}:${(r % 60).toString().padStart(2,'0')}`;
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
    return { expo:'#15803d', preguntas:'#1d4ed8', transicion:'#d97706', fin:'#dc2626' }[this.segmentoActual];
  }

  get labelSegmento(): string {
    return { expo:'Exposición (20 min)', preguntas:'Preguntas (10 min)', transicion:'Transición (5 min)', fin:'Tiempo agotado' }[this.segmentoActual];
  }

  // ═══════════════════════════════════════════════════════════════════
  // ACCIONES DEL COORDINADOR
  // ═══════════════════════════════════════════════════════════════════

  iniciarTurnoActual(): void {
    const cur = this.turnoActual;
    if (!cur) return;
    this.svc.iniciarEvaluacion(cur.idEvaluacionOposicion, this.idConvocatoria).subscribe({
      next: () => this.toast('Evaluación iniciada. ¡El tribunal puede calificar!', 'ok'),
      error: (err: Error) => this.toast(err.message, 'err')
    });
  }

  finalizarTurnoActual(): void {
    const cur = this.turnoActual;
    if (!cur) return;
    this.svc.finalizarEvaluacion(cur.idEvaluacionOposicion, this.idConvocatoria).subscribe({
      next: res => { this.hayEvaluacionEnCurso = false; this.toast(`Acta cerrada. Nota final: ${res.puntajeFinal}`, 'ok'); },
      error: (err: Error) => this.toast(err.message, 'err')
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // FIX 3: AUTO-GUARDADO con debounce
  // Se llama desde (ngModelChange) en cada campo de nota del template.
  // Tras 500ms de inactividad dispara un borrador automático.
  // ═══════════════════════════════════════════════════════════════════

  onNotaChange(): void {
    if (!this.formularioReadonly) {
      this.notasChange$.next();
    }
  }

  private autoGuardar(): void {
    const cur = this.turnoActual;
    if (!cur || this.formularioReadonly || this.guardando) return;
    if (this.pMaterial > 10 || this.pExposicion > 4 || this.pRespuestas > 6) return;
    if (this.pMaterial < 0 || this.pExposicion < 0 || this.pRespuestas < 0) return;

    // FIX 1: idUsuario NO va en el body — el backend lo extrae del JWT.
    // El servicio ya lo omite en registrarPuntaje(), pero lo reforzamos aquí.
    this.svc.registrarPuntaje({
      idEvaluacionOposicion: cur.idEvaluacionOposicion,
      idUsuario:             this.idUsuario,  // el servicio lo stripea del body
      puntajeMaterial:       this.pMaterial,
      puntajeExposicion:     this.pExposicion,
      puntajeRespuestas:     this.pRespuestas,
      finalizar:             false,
      idConvocatoria:        this.idConvocatoria,
    }).subscribe({
      next: () => this.toast('Borrador guardado automáticamente.', 'ok'),
      error: () => {} // fallo silencioso en auto-save
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // FORMULARIO DE NOTAS — guardado manual
  // ═══════════════════════════════════════════════════════════════════

  get subtotal(): number {
    return (this.pMaterial || 0) + (this.pExposicion || 0) + (this.pRespuestas || 0);
  }

  // FIX 4: El formulario es visible para cualquier miembro del tribunal,
  // incluyendo la Coordinadora. Ya no se bloquea por esCoord.
  get formularioVisible(): boolean {
    if (!this.turnoActual) return false;
    const estado = this.turnoActual.estado;
    return (estado === 'EN_CURSO' || estado === 'FINALIZADA') && this.puedeCalificar;
  }

  get formularioReadonly(): boolean {
    if (!this.turnoActual) return true;
    return this.turnoActual.estado === 'FINALIZADA' || this.yaFinalizo;
  }

  guardarPuntaje(finalizar: boolean): void {
    const cur = this.turnoActual;
    if (!cur || this.guardando) return;

    if (this.pMaterial  < 0 || this.pExposicion < 0 || this.pRespuestas < 0) { this.toast('Los puntajes no pueden ser negativos.', 'warn'); return; }
    if (this.pMaterial  > 10) { this.toast('Material: máximo 10 puntos.',   'warn'); return; }
    if (this.pExposicion > 4) { this.toast('Exposición: máximo 4 puntos.',  'warn'); return; }
    if (this.pRespuestas > 6) { this.toast('Respuestas: máximo 6 puntos.',  'warn'); return; }

    this.guardando = true;
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
        this.guardando = false;
        if (finalizar) this.yaFinalizo = true;
        this.toast(finalizar ? 'Calificación bloqueada.' : 'Puntaje guardado.', 'ok');
      },
      error: (err: Error) => { this.guardando = false; this.toast(err.message, 'err'); }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────

  badgeEstado(e: string): string {
    return ({ PROGRAMADA:'badge-blue', EN_CURSO:'badge-amber', FINALIZADA:'badge-green', NO_PRESENTO:'badge-red' } as any)[e] ?? 'badge-gray';
  }

  formatHora(h?: string):  string { return h ?? '—'; }
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
