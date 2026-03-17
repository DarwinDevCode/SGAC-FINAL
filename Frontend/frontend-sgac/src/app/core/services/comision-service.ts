import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ComisionDTO, ComisionRequestDTO } from '../dto/comision';
import {environment} from '../../../environments/environment';


@Injectable({ providedIn: 'root' })
export class ComisionService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  listarPorConvocatoria(idConvocatoria: number): Observable<ComisionDTO[]> {
    return this.http.get<ComisionDTO[]>(`${this.baseUrl}/convocatoria/${idConvocatoria}`);
  }

  crear(dto: ComisionRequestDTO): Observable<ComisionDTO> {
    return this.http.post<ComisionDTO>(`${this.baseUrl}/crear`, dto);
  }

  actualizar(id: number, dto: ComisionRequestDTO): Observable<ComisionDTO> {
    return this.http.put<ComisionDTO>(`${this.baseUrl}/actualizar/${id}`, dto);
  }

  desactivar(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/desactivar/${id}`, { responseType: 'text' });
  }
}
