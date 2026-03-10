import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoFase } from '../../models/catalogos/TipoFase';

@Injectable({ providedIn: 'root' })
export class TipoFaseService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/tipos-fase';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoFase[]>> {
    return this.http.get<StandardResponse<TipoFase[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoFase): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoFase): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

