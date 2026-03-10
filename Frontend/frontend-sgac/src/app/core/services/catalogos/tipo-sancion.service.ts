import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoSancionRequest, TipoSancionResponse } from '../../models/catalogos/TipoSancion';

@Injectable({ providedIn: 'root' })
export class TipoSancionService {

  private readonly env = (environment as any).apiUrl || 'http://localhost:8080/api';
  private baseUrl = `${this.env}/admin/catalogos-maestros/tipos-sancion`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoSancionResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoSancionResponse[]>>(this.baseUrl);
  }

  crear(data: TipoSancionRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoSancionRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
