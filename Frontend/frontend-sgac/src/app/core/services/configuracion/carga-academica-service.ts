import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  AsignaturaJerarquiaDTO,
  DocenteActivoDTO,
  SincronizarCargaRequest,
  SincronizarCargaResponse,
} from '../../models/configuracion/CargaAcademica';

@Injectable({ providedIn: 'root' })
export class CargaAcademicaService {
  private http = inject(HttpClient);
  private readonly env = environment.apiUrl;
  private readonly base = `${this.env}/carga-academica`;

  getDocentes(): Observable<DocenteActivoDTO[]> {
    return this.http.get<DocenteActivoDTO[]>(`${this.base}/docentes`);
  }

  getAsignaturasDocente(idDocente: number): Observable<AsignaturaJerarquiaDTO[]> {
    return this.http.get<AsignaturaJerarquiaDTO[]>(`${this.base}/docentes/${idDocente}/asignaturas`);
  }

  getAsignaturas(): Observable<AsignaturaJerarquiaDTO[]> {
    return this.http.get<AsignaturaJerarquiaDTO[]>(`${this.base}/asignaturas`);
  }

  sincronizar(req: SincronizarCargaRequest): Observable<SincronizarCargaResponse> {
    return this.http.post<SincronizarCargaResponse>(`${this.base}/sincronizar`, req);
  }
}
