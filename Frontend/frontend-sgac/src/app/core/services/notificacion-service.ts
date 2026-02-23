import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {NotificacionDTO} from '../dto/notificacion';

@Injectable({
  providedIn: 'root',
})
export class NotificacionService {
  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/notificaciones`;

  listarMisNotificaciones(idUsuario: number): Observable<NotificacionDTO[]> {
    return this.http.get<NotificacionDTO[]>(`${this.apiUrl}/mis-notificaciones/${idUsuario}`);
  }

  marcarComoLeida(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/marcar-leida/${id}`, {});
  }
}
