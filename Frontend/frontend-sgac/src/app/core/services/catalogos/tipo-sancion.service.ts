import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoSancion } from '../../models/catalogos/TipoSancion';

@Injectable({ providedIn: 'root' })
export class TipoSancionService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/tipos-sancion';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoSancion[]>> {
    return this.http.get<StandardResponse<TipoSancion[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoSancion): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoSancion): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

