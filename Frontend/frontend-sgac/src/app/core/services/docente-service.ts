import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  DocenteDTO,
  DocenteDashboardDTO,
  AyudanteResumenDTO,
  RegistroActividadDocenteDTO,
  CambiarEstadoRequest
} from '../dto/docente';

@Injectable({
  providedIn: 'root',
})
export class DocenteService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly API = `${this.baseUrl}/docentes`;

  listarActivos(): Observable<DocenteDTO[]> {
    return this.http.get<DocenteDTO[]>(this.API);
  }

  getDashboard(): Observable<DocenteDashboardDTO> {
    return this.http.get<DocenteDashboardDTO>(`${this.API}/dashboard`);
  }

  getAyudantes(): Observable<AyudanteResumenDTO[]> {
    return this.http.get<AyudanteResumenDTO[]>(`${this.API}/ayudantes`);
  }

  getActividadesAyudante(idAyudantia: number): Observable<RegistroActividadDocenteDTO[]> {
    return this.http.get<RegistroActividadDocenteDTO[]>(`${this.API}/ayudantes/${idAyudantia}/actividades`);
  }

  getDetalleActividad(idActividad: number): Observable<RegistroActividadDocenteDTO> {
    return this.http.get<RegistroActividadDocenteDTO>(`${this.API}/actividades/${idActividad}`);
  }

  cambiarEstadoActividad(idActividad: number, req: CambiarEstadoRequest): Observable<void> {
    return this.http.put<void>(`${this.API}/actividades/${idActividad}/estado`, req);
  }

  cambiarEstadoEvidencia(idEvidencia: number, req: CambiarEstadoRequest): Observable<void> {
    return this.http.put<void>(`${this.API}/evidencias/${idEvidencia}/estado`, req);
  }
}
