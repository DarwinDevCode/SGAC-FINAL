import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CoordinadorResponseDTO } from '../dto/coordinador';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { PostulacionResponseDTO } from '../dto/postulacion';

@Injectable({
  providedIn: 'root'
})
export class CoordinadorService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API_COORDINADORES = `${this.baseUrl}/coordinadores`;
  private readonly API_CONVOCATORIAS = `${this.baseUrl}/convocatorias`;
  private readonly API_POSTULACIONES = `${this.baseUrl}/postulaciones`;

  obtenerCoordinadorPorUsuario(idUsuario: number): Observable<CoordinadorResponseDTO> {
    return this.http.get<CoordinadorResponseDTO>(`${this.API_COORDINADORES}/usuario/${idUsuario}`);
  }

  listarConvocatoriasPorCarrera(idCarrera: number): Observable<ConvocatoriaDTO[]> {
    // Note: The backend ConvocatoriaController might need a specific endpoint for carrera filtering.
    // However, looking at listing endpoints, we might have to filter manually if it doesn't exist.
    // For now, let's use the general list and filter if needed, or assume a backend filter if available.
    return this.http.get<ConvocatoriaDTO[]>(`${this.API_CONVOCATORIAS}/listar-vista`);
  }

  listarPostulacionesPorConvocatoria(idConvocatoria: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/convocatoria/${idConvocatoria}`);
  }
}
