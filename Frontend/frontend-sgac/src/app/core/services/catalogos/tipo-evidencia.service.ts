import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoEvidenciaRequest, TipoEvidenciaResponse } from '../../models/catalogos/TipoEvidencia';

@Injectable({ providedIn: 'root' })
export class TipoEvidenciaService {

  private readonly env = environment.apiUrl;
  private readonly baseUrl = `${this.env}/admin/catalogos-maestros/tipos-evidencia`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoEvidenciaResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoEvidenciaResponse[]>>(this.baseUrl);
  }

  crear(data: TipoEvidenciaRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoEvidenciaRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
