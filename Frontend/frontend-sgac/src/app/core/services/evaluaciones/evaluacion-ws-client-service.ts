import { Injectable, OnDestroy, inject } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { AuthService } from '../auth-service';

export interface EvaluacionWsMessage {
  tipo:                    'CAMBIO_ESTADO' | 'PUNTAJE_ACTUALIZADO';
  idConvocatoria:          number;
  idEvaluacionOposicion?:  number;
  nuevoEstado?:            string;
  nombreEstado?:           string;
  horaInicioReal?:         string;
  serverTimestamp?:        string;
  horaFinReal?:            string;
  puntajeFinal?:           number;
  idUsuario?:              number;
  todosFinalizaron?:       boolean;
  mensaje?:                string;
  emitidoEn?:              string;
}

export type WsEstado = 'DESCONECTADO' | 'CONECTANDO' | 'CONECTADO' | 'ERROR';

@Injectable({ providedIn: 'root' })
export class EvaluacionWsClientService implements OnDestroy {

  private authSrv = inject(AuthService);

  private client: Client | null = null;
  private mensajesSubject  = new Subject<EvaluacionWsMessage>();
  private estadoSubject    = new BehaviorSubject<WsEstado>('DESCONECTADO');
  readonly mensajes$ = this.mensajesSubject.asObservable();
  readonly estado$   = this.estadoSubject.asObservable();

  conectar(idConvocatoria: number): void {
    this.cerrarConexionActual();
    const token = this.authSrv.getToken() ?? '';

    this.estadoSubject.next('CONECTANDO');

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-sgac'),

      connectHeaders: { Authorization: `Bearer ${token}` },

      reconnectDelay: 5000,   // reintento automático cada 5 s si se cae
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        this.estadoSubject.next('CONECTADO');
        this.client!.subscribe(
          `/topic/evaluacion/${idConvocatoria}`,
          (frame: IMessage) => this.despacharMensaje(frame)
        );
      },

      onDisconnect: () => this.estadoSubject.next('DESCONECTADO'),

      onStompError: (frame) => {
        console.error('[WS-EVAL] Error STOMP:', frame.headers['message']);
        this.estadoSubject.next('ERROR');
      },

      onWebSocketError: (event) => {
        console.error('[WS-EVAL] Error WebSocket:', event);
        this.estadoSubject.next('ERROR');
      },
    });

    this.client.activate();
  }

  desconectar(): void {
    this.cerrarConexionActual();
  }

  get estaConectado(): boolean {
    return this.estadoSubject.value === 'CONECTADO';
  }

  private despacharMensaje(frame: IMessage): void {
    try {
      const msg: EvaluacionWsMessage = JSON.parse(frame.body);
      this.mensajesSubject.next(msg);
    } catch (e) {
      console.warn('[WS-EVAL] Mensaje no parseable:', frame.body);
    }
  }

  private cerrarConexionActual(): void {
    if (this.client) {
      this.client.deactivate().catch(() => {});
      this.client = null;
    }
    this.estadoSubject.next('DESCONECTADO');
  }

  ngOnDestroy(): void {
    this.cerrarConexionActual();
    this.mensajesSubject.complete();
    this.estadoSubject.complete();
  }
}
