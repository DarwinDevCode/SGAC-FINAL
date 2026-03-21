import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../../environments/environment';
import {
  AuditoriaKpiDTO,
  AuditoriaResponseDTO,
  EvolucionAuditoria,
  Page
} from '../../models/reportes_y_auditoria/reports_audit';

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {

  private apiUrl = `${environment.apiUrl}/auditoria`;

  constructor(private http: HttpClient) {}

  listarAuditorias(
    page: number = 0,
    size: number = 10,
    tabla?: string,
    accion?: string,
    idUsuario?: number,
    fechaInicio?: string,
    fechaFin?: string
  ): Observable<Page<AuditoriaResponseDTO>> {

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (tabla)
      params = params.set('tabla', tabla);
    if (accion)
      params = params.set('accion', accion);
    if (idUsuario)
      params = params.set('idUsuario', idUsuario.toString());
    if (fechaInicio)
      params = params.set('fechaInicio', fechaInicio);
    if (fechaFin)
      params = params.set('fechaFin', fechaFin);

    return this.http.get<Page<AuditoriaResponseDTO>>(`${this.apiUrl}/listar`, { params });
  }

  obtenerEvolucion(): Observable<EvolucionAuditoria[]> {
    return this.http.get<EvolucionAuditoria[]>(`${this.apiUrl}/evolucion`);
  }

  obtenerKpis(): Observable<AuditoriaKpiDTO> {
    return this.http.get<AuditoriaKpiDTO>(`${this.apiUrl}/kpis`);
  }
}
