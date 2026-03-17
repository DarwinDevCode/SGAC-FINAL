import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoRegistroRequest, TipoEstadoRegistroResponse } from '../../models/catalogos/TipoEstadoRegistro';

@Injectable({ providedIn: 'root' })
export class TipoEstadoRegistroService {

  private readonly env = environment.apiUrl;
  private readonly baseUrl = `${this.env}/admin/catalogos-maestros/estados-registro`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoEstadoRegistroResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoEstadoRegistroResponse[]>>(this.baseUrl);
  }

  crear(data: TipoEstadoRegistroRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoEstadoRegistroRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
