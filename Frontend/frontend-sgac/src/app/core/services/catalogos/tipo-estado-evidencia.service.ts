import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoEvidenciaRequest, TipoEstadoEvidenciaResponse } from '../../models/catalogos/TipoEstadoEvidencia';

@Injectable({ providedIn: 'root' })
export class TipoEstadoEvidenciaService {
  private readonly env   = environment.apiUrl;
  private readonly baseUrl = `${this.env}/admin/catalogos-maestros/estados-evidencia`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoEstadoEvidenciaResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoEstadoEvidenciaResponse[]>>(this.baseUrl);
  }

  crear(data: TipoEstadoEvidenciaRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoEstadoEvidenciaRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
