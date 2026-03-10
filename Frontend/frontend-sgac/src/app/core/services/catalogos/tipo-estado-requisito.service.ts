import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoRequisitoRequest, TipoEstadoRequisitoResponse } from '../../models/catalogos/TipoEstadoRequisito';

@Injectable({ providedIn: 'root' })
export class TipoEstadoRequisitoService {

  private readonly env = (environment as any).apiUrl || 'http://localhost:8080/api';
  private baseUrl = `${this.env}/admin/catalogos-maestros/estados-requisito`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoEstadoRequisitoResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoEstadoRequisitoResponse[]>>(this.baseUrl);
  }

  crear(data: TipoEstadoRequisitoRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoEstadoRequisitoRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
