import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardConsultaResponse, StandardModificacionResponse } from '../../models/catalogos/StandardResponse';
import { PrivilegioRequest, PrivilegioResponse } from '../../models/catalogos/Privilegio';

@Injectable({ providedIn: 'root' })
export class PrivilegioService {
  private readonly env   = environment.apiUrl;
  private readonly baseUrl = `${this.env}/admin/catalogos-maestros/privilegios`;

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardConsultaResponse<PrivilegioResponse[]>> {
    return this.http.get<StandardConsultaResponse<PrivilegioResponse[]>>(this.baseUrl);
  }

  crear(data: PrivilegioRequest): Observable<StandardModificacionResponse> {
    return this.http.post<StandardModificacionResponse>(this.baseUrl, data);
  }

  actualizar(id: number, data: PrivilegioRequest): Observable<StandardModificacionResponse> {
    return this.http.put<StandardModificacionResponse>(`${this.baseUrl}/${id}`, data);
  }

  desactivar(id: number): Observable<StandardModificacionResponse> {
    return this.http.patch<StandardModificacionResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
