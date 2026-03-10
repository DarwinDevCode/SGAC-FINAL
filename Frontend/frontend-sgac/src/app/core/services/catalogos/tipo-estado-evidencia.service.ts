import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoEvidencia } from '../../models/catalogos/TipoEstadoEvidencia';

@Injectable({ providedIn: 'root' })
export class TipoEstadoEvidenciaService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/estados-evidencia';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoEstadoEvidencia[]>> {
    return this.http.get<StandardResponse<TipoEstadoEvidencia[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoEstadoEvidencia): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoEstadoEvidencia): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

