import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoAyudantiaRequest, TipoEstadoAyudantiaResponse } from '../../models/catalogos/TipoEstadoAyudantia';

@Injectable({ providedIn: 'root' })
export class TipoEstadoAyudantiaService {
  private readonly env = (environment as any).apiUrl || 'http://localhost:8080/api';
  private baseUrl = `${this.env}/admin/catalogos-maestros/estados-ayudantia`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoEstadoAyudantiaResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoEstadoAyudantiaResponse[]>>(this.baseUrl);
  }

  crear(data: TipoEstadoAyudantiaRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoEstadoAyudantiaRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
