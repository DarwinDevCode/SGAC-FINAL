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
  ContextoAsistencia
} from '../../models/ayudantia/Asistencia';

@Injectable({ providedIn: 'root' })
export class AsistenciaService {
  private http = inject(HttpClient);
  private readonly baseUrl   = environment.apiUrl;
  private readonly base = `${this.baseUrl}/asistencia`;

  obtenerContexto(): Observable<ContextoAsistencia> {
    return this.http.get<ContextoAsistencia>(`${this.base}/contexto`);
  }

  /**
   * Consulta los participantes de la ayudantía activa.
   * El ID se resuelve internamente en el backend.
   */
  consultarParticipantes(): Observable<Participante[]> {
    return this.http.get<Participante[]>(`${this.base}/participantes`);
  }

  /**
   * Realiza la carga masiva de alumnos.
   * No requiere ID en la URL; el backend lo toma del contexto del ayudante.
   */
  cargarParticipantesMasivo(
    participantes: { nombreCompleto: string; curso: string; paralelo: string }[]
  ): Observable<CargaMasivaResponse> {
    return this.http.post<CargaMasivaResponse>(
      `${this.base}/participantes/masivo`,
      { participantes }
    );
  }

  /**
   * Descarga la plantilla de Excel para la carga de alumnos.
   */
  descargarPlantilla(): Observable<Blob> {
    return this.http.get(`${this.base}/plantilla-excel`, { responseType: 'blob' });
  }

  /**
   * Envía el Excel para obtener una previsualización de los datos.
   */
  previewExcel(file: File): Observable<PreviewResponse> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<PreviewResponse>(`${this.base}/preview-excel`, fd);
  }

  /**
   * Inicializa la lista de asistencia para la sesión actual.
   */
  inicializarAsistencia(): Observable<{ exito: boolean; mensaje: string }> {
    return this.http.post<{ exito: boolean; mensaje: string }>(
      `${this.base}/inicializar`,
      {}
    );
  }

  /**
   * Guarda los cambios realizados en la toma de asistencia.
   */
  guardarAsistencias(
    asistencias: { idParticipante: number; asistio: boolean }[]
  ): Observable<GuardarAsistenciaResponse> {
    return this.http.put<GuardarAsistenciaResponse>(
      `${this.base}/guardar`,
      { asistencias }
    );
  }

  /**
   * Consulta el detalle de asistencia (quién asistió y quién no) de la sesión.
   */
  consultarAsistencia(): Observable<DetalleAsistencia[]> {
    return this.http.get<DetalleAsistencia[]>(`${this.base}/detalle`);
  }
}
