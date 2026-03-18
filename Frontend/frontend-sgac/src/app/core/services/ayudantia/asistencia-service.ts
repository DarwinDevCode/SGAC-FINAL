import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  Participante,
  CargaMasivaResponse,
  PreviewResponse,
  DetalleAsistencia,
  GuardarAsistenciaResponse,
  ContextoAsistencia,
  MatrizAsistencia,
} from '../../models/ayudantia/Asistencia';

@Injectable({ providedIn: 'root' })
export class AsistenciaService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/asistencia`;

  obtenerContexto(): Observable<ContextoAsistencia> {
    return this.http.get<ContextoAsistencia>(`${this.base}/contexto`);
  }

  consultarParticipantes(): Observable<Participante[]> {
    return this.http.get<Participante[]>(`${this.base}/participantes`);
  }

  cargarParticipantesMasivo(
    participantes: { nombreCompleto: string; curso: string; paralelo: string }[]
  ): Observable<CargaMasivaResponse> {
    return this.http.post<CargaMasivaResponse>(
      `${this.base}/participantes/masivo`,
      { participantes }
    );
  }

  descargarPlantilla(): Observable<Blob> {
    return this.http.get(`${this.base}/plantilla-excel`, { responseType: 'blob' });
  }

  previewExcel(file: File): Observable<PreviewResponse> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<PreviewResponse>(`${this.base}/preview-excel`, fd);
  }

  inicializarAsistencia(): Observable<{ exito: boolean; mensaje: string }> {
    return this.http.post<{ exito: boolean; mensaje: string }>(
      `${this.base}/inicializar`,
      {}
    );
  }

  guardarAsistencias(
    asistencias: { idParticipante: number; asistio: boolean }[]
  ): Observable<GuardarAsistenciaResponse> {
    return this.http.put<GuardarAsistenciaResponse>(
      `${this.base}/guardar`,
      { asistencias }
    );
  }

  consultarAsistencia(): Observable<DetalleAsistencia[]> {
    return this.http.get<DetalleAsistencia[]>(`${this.base}/detalle`);
  }

  obtenerMatriz(): Observable<MatrizAsistencia> {
    return this.http.get<MatrizAsistencia>(`${this.base}/matriz`);
  }
}
