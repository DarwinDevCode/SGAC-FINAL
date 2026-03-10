import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoPostulacionRequest, TipoEstadoPostulacionResponse } from '../../models/catalogos/TipoEstadoPostulacion';

@Injectable({ providedIn: 'root' })
export class TipoEstadoPostulacionService {

  private readonly env = (environment as any).apiUrl || 'http://localhost:8080/api';
  private baseUrl = `${this.env}/admin/catalogos-maestros/estados-postulacion`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoEstadoPostulacionResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoEstadoPostulacionResponse[]>>(this.baseUrl);
  }

  crear(data: TipoEstadoPostulacionRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoEstadoPostulacionRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
