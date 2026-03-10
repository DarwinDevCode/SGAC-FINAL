import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoEstadoRequisito } from '../../models/catalogos/TipoEstadoRequisito';

@Injectable({ providedIn: 'root' })
export class TipoEstadoRequisitoService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/estados-requisito';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoEstadoRequisito[]>> {
    return this.http.get<StandardResponse<TipoEstadoRequisito[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoEstadoRequisito): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoEstadoRequisito): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

