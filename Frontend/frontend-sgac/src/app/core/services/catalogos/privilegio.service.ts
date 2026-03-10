import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StandardResponse } from '../../models/catalogos/StandardResponse';
import { Privilegio } from '../../models/catalogos/Privilegio';

@Injectable({ providedIn: 'root' })
export class PrivilegioService {
  private baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api' + '/catalogos/privilegios';

  constructor(private http: HttpClient) {}

  listar(): Observable<StandardResponse<Privilegio[]>> {
    return this.http.get<StandardResponse<Privilegio[]>>(this.baseUrl + '/listar');
  }

  crear(data: Privilegio): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(this.baseUrl + '/crear', data);
  }

  actualizar(id: number, data: Privilegio): Observable<StandardResponse<number>> {
    return this.http.put<StandardResponse<number>>(this.baseUrl + `/actualizar/${id}`, data);
  }

  desactivar(id: number): Observable<StandardResponse<number>> {
    return this.http.patch<StandardResponse<number>>(this.baseUrl + `/desactivar/${id}`, {});
  }
}
