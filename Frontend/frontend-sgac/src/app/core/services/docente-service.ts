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
  private readonly API = `${(environment as any).apiUrl || 'http://localhost:8080/api'}/docentes`;
  private readonly API_INFORMES = `${(environment as any).apiUrl || 'http://localhost:8080/api'}/informes`;

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

  // ── Informes Mensuales ────────────────────────────────────────────────────
  getInformesPendientesDocente(idDocente: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_INFORMES}/docente/${idDocente}/pendientes`);
  }

  aprobarInformeDocente(idInforme: number, observaciones: string): Observable<any> {
    return this.http.post<any>(`${this.API_INFORMES}/revisar-docente/${idInforme}`, observaciones, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  rechazarInformeDocente(idInforme: number, observaciones: string): Observable<any> {
    return this.http.post<any>(`${this.API_INFORMES}/rechazar-docente/${idInforme}`, observaciones, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }
}
