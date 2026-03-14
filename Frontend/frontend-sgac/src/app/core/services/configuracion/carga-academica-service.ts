import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  AsignaturaJerarquiaDTO,
  DocenteActivoDTO,
  SincronizarCargaRequest,
  SincronizarCargaResponse,
} from '../../models/configuracion/CargaAcademica'

@Injectable({ providedIn: 'root' })
export class CargaAcademicaService {
  private http = inject(HttpClient);
  private base = `${(environment as any).apiUrl ?? 'http://localhost:8080/api'}/carga-academica`;

  getDocentes(): Observable<DocenteActivoDTO[]> {
    return this.http.get<DocenteActivoDTO[]>(`${this.base}/docentes`);
  }

  getAsignaturas(): Observable<AsignaturaJerarquiaDTO[]> {
    return this.http.get<AsignaturaJerarquiaDTO[]>(`${this.base}/asignaturas`);
  }

  sincronizar(req: SincronizarCargaRequest): Observable<SincronizarCargaResponse> {
    return this.http.post<SincronizarCargaResponse>(`${this.base}/sincronizar`, req);
  }
}
