import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MensajeInterno {
  idMensajeInterno?: number;
  idAyudantia: number;
  idUsuarioEmisor: number;
  nombreEmisor?: string;
  mensaje: string;
  fechaEnvio?: string;
  rutaArchivoAdjunto?: string;
  leido?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ComunicacionService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/comunicacion`;

  obtenerHistorial(idAyudantia: number): Observable<MensajeInterno[]> {
    return this.http.get<MensajeInterno[]>(`${this.apiUrl}/historial/${idAyudantia}`);
  }

  enviarMensaje(mensaje: MensajeInterno): Observable<MensajeInterno> {
    return this.http.post<MensajeInterno>(`${this.apiUrl}/enviar`, mensaje);
  }

  buscarMensajes(idAyudantia: number, criterio: string): Observable<MensajeInterno[]> {
    return this.http.get<MensajeInterno[]>(`${this.apiUrl}/buscar/${idAyudantia}`, { params: { q: criterio } });
  }
}
