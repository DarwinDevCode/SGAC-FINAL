import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DecanoResponseDTO } from '../dto/decano';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { PostulacionResponseDTO } from '../dto/postulacion';

@Injectable({
  providedIn: 'root'
})
export class DecanoService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API_DECANOS = `${this.baseUrl}/decanos`;
  private readonly API_CONVOCATORIAS = `${this.baseUrl}/convocatorias`;
  private readonly API_POSTULACIONES = `${this.baseUrl}/postulaciones`;

  // --- Decanos API ---
  obtenerDecanoPorUsuario(idUsuario: number): Observable<DecanoResponseDTO> {
    return this.http.get<DecanoResponseDTO>(`${this.API_DECANOS}/usuario/${idUsuario}`);
  }

  // --- Convocatorias API ---
  listarConvocatoriasActivas(): Observable<ConvocatoriaDTO[]> {
    return this.http.get<ConvocatoriaDTO[]>(`${this.API_CONVOCATORIAS}/listar-vista`);
  }

  // --- Postulaciones API ---
  listarPostulacionesPorConvocatoria(idConvocatoria: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/convocatoria/${idConvocatoria}`);
  }
}
