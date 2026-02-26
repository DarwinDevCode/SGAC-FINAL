import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CoordinadorResponseDTO } from '../dto/coordinador';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { PostulacionResponseDTO } from '../dto/postulacion';

export interface ComisionSeleccionDTO {
  idComisionSeleccion?: number;
  idConvocatoria: number;
  nombreComision: string;
  fechaConformacion?: string;
  activo?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CoordinadorService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API_COORDINADORES = `${this.baseUrl}/coordinadores`;
  private readonly API_CONVOCATORIAS = `${this.baseUrl}/convocatorias`;
  private readonly API_POSTULACIONES = `${this.baseUrl}/postulaciones`;
  private readonly API_COMISIONES = `${this.baseUrl}/comisiones`;

  obtenerCoordinadorPorUsuario(idUsuario: number): Observable<CoordinadorResponseDTO> {
    return this.http.get<CoordinadorResponseDTO>(`${this.API_COORDINADORES}/usuario/${idUsuario}`);
  }

  listarConvocatoriasPorCarrera(idCarrera: number): Observable<ConvocatoriaDTO[]> {
    return this.http.get<ConvocatoriaDTO[]>(`${this.API_CONVOCATORIAS}/listar-vista`);
  }

  listarPostulacionesPorConvocatoria(idConvocatoria: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/convocatoria/${idConvocatoria}`);
  }

  listarPostulacionesPorCarrera(idCarrera: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/carrera/${idCarrera}`);
  }

  listarPendientesPorCarrera(idCarrera: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/pendientes/carrera/${idCarrera}`);
  }

  cambiarEstadoPostulacion(idPostulacion: number, estado: string, observacion: string): Observable<any> {
    return this.http.put(`${this.API_POSTULACIONES}/cambiar-estado/${idPostulacion}`,
      null, { params: { estado, observacion }, responseType: 'text' });
  }

  // Comisión de selección
  crearComision(datos: ComisionSeleccionDTO): Observable<ComisionSeleccionDTO> {
    return this.http.post<ComisionSeleccionDTO>(`${this.API_COMISIONES}/crear`, datos);
  }

  listarComisionesPorConvocatoria(idConvocatoria: number): Observable<ComisionSeleccionDTO[]> {
    return this.http.get<ComisionSeleccionDTO[]>(`${this.API_COMISIONES}/convocatoria/${idConvocatoria}`);
  }

  // Requisitos adjuntos (documentos del postulante)
  listarDocumentosPorPostulacion(idPostulacion: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/requisitos-adjuntos/postulacion/${idPostulacion}`);
  }

  getUrlDescargaDocumento(idRequisito: number): string {
    return `${this.baseUrl}/requisitos-adjuntos/descargar/${idRequisito}`;
  }
}
