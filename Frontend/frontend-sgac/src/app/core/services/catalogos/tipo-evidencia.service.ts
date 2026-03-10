import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { TipoEvidencia } from '../../models/catalogos/TipoEvidencia';

@Injectable({ providedIn: 'root' })
export class TipoEvidenciaService {
  private baseUrl = 'http://localhost:8080/api' + '/catalogos/tipos-evidencia';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<TipoEvidencia[]>> {
    return this.http.get<StandardResponse<TipoEvidencia[]>>(this.baseUrl + '/listar');
  }

  crear(data: TipoEvidencia): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: TipoEvidencia): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}

