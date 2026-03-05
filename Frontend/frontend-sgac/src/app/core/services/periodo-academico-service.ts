import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PeriodoAcademicoDTO } from '../dto/periodo-academico';

const BASE = 'http://localhost:8080/api/periodos-academicos';

@Injectable({ providedIn: 'root' })
export class PeriodoAcademicoService {
  private http = inject(HttpClient);

  listarTodos(): Observable<PeriodoAcademicoDTO[]> {
    return this.http.get<PeriodoAcademicoDTO[]>(BASE);
  }

  obtenerActivo(): Observable<PeriodoAcademicoDTO> {
    return this.http.get<PeriodoAcademicoDTO>(`${BASE}/activo`);
  }

  crear(data: Partial<PeriodoAcademicoDTO>): Observable<PeriodoAcademicoDTO> {
    return this.http.post<PeriodoAcademicoDTO>(BASE, data);
  }

  actualizar(id: number, data: Partial<PeriodoAcademicoDTO>): Observable<PeriodoAcademicoDTO> {
    return this.http.put<PeriodoAcademicoDTO>(`${BASE}/${id}`, data);
  }

  desactivar(id: number): Observable<any> {
    return this.http.patch(`${BASE}/${id}/desactivar`, {}, { responseType: 'text' });
  }

  activar(id: number): Observable<any> {
    return this.http.patch(`${BASE}/${id}/activar`, {}, { responseType: 'text' });
  }

  importarRequisitos(idDestino: number, idFuente: number): Observable<any> {
    // POST /api/periodos-academicos/{id}/importar-requisitos?fuentePeriodoId={idAnterior}
    return this.http.post(`${BASE}/${idDestino}/importar-requisitos?fuentePeriodoId=${idFuente}`, {});
  }
}
