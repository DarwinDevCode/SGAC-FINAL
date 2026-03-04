import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { BehaviorSubject, Observable, catchError, of, tap } from 'rxjs';
import {Notificacion, NotificacionResponseDTO} from '../dto/notificacion';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root',
})
export class NotificacionService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly apiUrl = `${this.baseUrl}/notificaciones`;

  private client: Client | null = null;
  private currentUserId: number | null = null;

  private readonly notificacionesSubject = new BehaviorSubject<Notificacion[]>([]);
  readonly notificaciones$ = this.notificacionesSubject.asObservable();

  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  conectar(idUsuario: number): void {
    this.currentUserId = idUsuario;

    // Carga inicial (últimas 10) vía REST
    this.obtenerNotificaciones().subscribe();

    if (this.client?.active) {
      return;
    }

    const token = localStorage.getItem('token');

    // Para SockJS, se usa URL HTTP del endpoint (no ws://)
    const wsBase = this.baseUrl.replace(/\/api\/?$/, ''); // http://localhost:8080
    const sock = new SockJS(`${wsBase}/ws-sgac`);

    const client = new Client({
      webSocketFactory: () => sock as any,
      reconnectDelay: 4000,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      debug: () => { },
    });

    client.onConnect = () => {
      client.subscribe(`/queue/notificaciones/${idUsuario}`, (message: IMessage) => {
        this.onMessage(message);
      });
    };

    client.onStompError = (frame: import('@stomp/stompjs').IFrame) => {
      // eslint-disable-next-line no-console
      console.error('STOMP error', frame.headers, frame.body);
    };

    client.activate();
    this.client = client;
  }

  desconectar(): void {
    this.currentUserId = null;
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }

  obtenerNotificaciones(): Observable<Notificacion[]> {
    // Endpoint actual: GET /api/notificaciones/ultimas
    return this.http.get<Notificacion[]>(`${this.apiUrl}/ultimas`).pipe(
      tap((list) => this.setNotificaciones(list)),
      catchError((err) => {
        // No bloqueamos el arranque del layout si falla el backend.
        // eslint-disable-next-line no-console
        console.error('No se pudieron cargar notificaciones', err);
        this.setNotificaciones([]);
        return of([] as Notificacion[]);
      })
    );
  }

  marcarComoLeida(idNotificacion: number): Observable<void> {
    // Endpoint actual: PUT /api/notificaciones/{id}/leida
    return this.http.put<void>(`${this.apiUrl}/${idNotificacion}/leida`, {}).pipe(
      tap(() => this.marcarLocalComoLeida(idNotificacion)),
      catchError((err) => {
        // eslint-disable-next-line no-console
        console.error('No se pudo marcar como leída', err);
        return of(void 0);
      })
    );
  }

  private onMessage(message: IMessage): void {
    try {
      const payload = JSON.parse(message.body) as Notificacion;
      const current = this.notificacionesSubject.getValue();

      // Deduplicación básica por id
      const exists = current.some((n) => n.idNotificacion === payload.idNotificacion);
      const next = exists ? current : [payload, ...current].slice(0, 10);

      this.setNotificaciones(next);
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('No se pudo parsear notificación', e, message.body);
    }
  }

  private setNotificaciones(list: Notificacion[]): void {
    this.notificacionesSubject.next(list);
    const unread = list.filter((n) => !n.leido).length;
    this.unreadCountSubject.next(unread);
  }

  private marcarLocalComoLeida(idNotificacion: number): void {
    const current = this.notificacionesSubject.getValue();
    const next = current.map((n) => (n.idNotificacion === idNotificacion ? { ...n, leido: true } : n));
    this.setNotificaciones(next);
  }

  listarMisNotificaciones(idUsuario: number): Observable<NotificacionResponseDTO[]> {
    return this.http.get<NotificacionResponseDTO[]>(`${this.apiUrl}/mis-notificaciones/${idUsuario}`);
  }
}
