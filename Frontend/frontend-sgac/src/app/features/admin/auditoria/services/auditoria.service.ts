import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment.development';

export interface LogAuditoria {
  idLogAuditoria: number;
  idUsuario: number;
  nombreUsuario: string;
  accion: string;
  tablaAfectada: string;
  registroAfectado: number;
  fechaHora: string;
  ipOrigen: string;
  valorAnterior: string;
  valorNuevo: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {
  private apiUrl = `${environment.apiUrl}/log-auditoria`;
  constructor(private http: HttpClient) {}

  obtenerLogsPaginados(filtros: any, page: number, size: number, sort: string, direction: string): Observable<PageResponse<LogAuditoria>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort)
      .set('direction', direction);

    if (filtros.queryParams) params = params.set('queryParams', filtros.queryParams);
    if (filtros.tablaAfectada) params = params.set('tablaAfectada', filtros.tablaAfectada);
    if (filtros.accion) params = params.set('accion', filtros.accion);
    if (filtros.fechaInicio) params = params.set('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params = params.set('fechaFin', filtros.fechaFin);

    return this.http.get<PageResponse<LogAuditoria>>(`${this.apiUrl}/paginado`, { params });
  }

  descargarReportePdf(filtros: any): Observable<Blob> {
    let params = new HttpParams();
    
    if (filtros.queryParams) params = params.set('queryParams', filtros.queryParams);
    if (filtros.tablaAfectada) params = params.set('tablaAfectada', filtros.tablaAfectada);
    if (filtros.accion) params = params.set('accion', filtros.accion);
    if (filtros.fechaInicio) params = params.set('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params = params.set('fechaFin', filtros.fechaFin);

    return this.http.get(`${this.apiUrl}/reporte-pdf`, { params, responseType: 'blob' });
  }
}
