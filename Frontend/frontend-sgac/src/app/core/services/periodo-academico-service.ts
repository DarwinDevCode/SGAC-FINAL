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
}
