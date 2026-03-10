import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoPostulacion } from '../../models/catalogos/TipoEstadoPostulacion';

@Injectable({ providedIn: 'root' })
export class TipoEstadoPostulacionService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/estados-postulacion';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoEstadoPostulacion[]>> {
    return this.http.get<StandardResponse<TipoEstadoPostulacion[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoEstadoPostulacion): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoEstadoPostulacion): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

