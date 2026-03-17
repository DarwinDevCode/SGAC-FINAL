import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { TipoFaseRequest, TipoFaseResponse } from '../../models/catalogos/TipoFase';

@Injectable({ providedIn: 'root' })
export class TipoFaseService {

  private readonly env = environment.apiUrl;
  private readonly baseUrl = `${this.env}/admin/catalogos-maestros/tipos-fase`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<TipoFaseResponse[]>> {
    return this.http.get<StandardConsultaResponse<TipoFaseResponse[]>>(this.baseUrl);
  }

  crear(data: TipoFaseRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: TipoFaseRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
