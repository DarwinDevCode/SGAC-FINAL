import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PeriodoAcademicoDTO } from '../dto/periodo-academico';
import {environment} from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PeriodoAcademicoService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly API = `${this.baseUrl}/periodos-academicos`

  listarTodos(): Observable<PeriodoAcademicoDTO[]> {
    return this.http.get<PeriodoAcademicoDTO[]>(this.API);
  }

  obtenerActivo(): Observable<PeriodoAcademicoDTO> {
    return this.http.get<PeriodoAcademicoDTO>(`${this.API}/activo`);
  }

  crear(data: Partial<PeriodoAcademicoDTO>): Observable<PeriodoAcademicoDTO> {
    return this.http.post<PeriodoAcademicoDTO>(this.API, data);
  }

  actualizar(id: number, data: Partial<PeriodoAcademicoDTO>): Observable<PeriodoAcademicoDTO> {
    return this.http.put<PeriodoAcademicoDTO>(`${this.API}/${id}`, data);
  }

  desactivar(id: number): Observable<any> {
    return this.http.patch(`${this.API}/${id}/desactivar`, {});
  }

  activar(id: number): Observable<any> {
    return this.http.patch(`${this.API}/${id}/activar`, {});
  }

  importarRequisitos(idDestino: number, idFuente: number): Observable<any> {
    return this.http.post(`${this.API}/${idDestino}/importar-requisitos?fuentePeriodoId=${idFuente}`, {});
  }
}
