import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoRegistro } from '../../models/catalogos/TipoEstadoRegistro';

@Injectable({ providedIn: 'root' })
export class TipoEstadoRegistroService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/estados-registro';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoEstadoRegistro[]>> {
    return this.http.get<StandardResponse<TipoEstadoRegistro[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoEstadoRegistro): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoEstadoRegistro): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

