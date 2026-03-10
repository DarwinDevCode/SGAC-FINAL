import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../../environments/environment';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoAyudantia } from '../../models/catalogos/TipoEstadoAyudantia';

@Injectable({ providedIn: 'root' })
export class TipoEstadoAyudantiaService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/estados-ayudantia';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoEstadoAyudantia[]>> {
    return this.http.get<StandardResponse<TipoEstadoAyudantia[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoEstadoAyudantia): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoEstadoAyudantia): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

