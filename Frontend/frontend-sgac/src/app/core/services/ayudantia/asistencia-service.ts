import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Participante, CargaMasivaResponse, PreviewResponse, DetalleAsistencia, GuardarAsistenciaResponse } from '../../models/ayudantia/Asistencia';

@Injectable({ providedIn: 'root' })
export class AsistenciaService {
  private http = inject(HttpClient);
  private readonly base = `${(environment as any).apiUrl ?? 'http://localhost:8080/api'}/asistencia`;

  consultarParticipantes(idAyudantia: number): Observable<Participante[]> {
    return this.http.get<Participante[]>(
      `${this.base}/ayudantia/${idAyudantia}/participantes`
    );
  }

  cargarParticipantesMasivo(
    idAyudantia: number,
    participantes: { nombreCompleto: string; curso: string; paralelo: string }[]
  ): Observable<CargaMasivaResponse> {
    return this.http.post<CargaMasivaResponse>(
      `${this.base}/ayudantia/${idAyudantia}/participantes/masivo`,
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

  inicializarAsistencia(idRegistro: number): Observable<{ exito: boolean; mensaje: string }> {
    return this.http.post<{ exito: boolean; mensaje: string }>(
      `${this.base}/registro/${idRegistro}/inicializar`,
      {}
    );
  }

  guardarAsistencias(
    idRegistro: number,
    asistencias: { idParticipante: number; asistio: boolean }[]
  ): Observable<GuardarAsistenciaResponse> {
    return this.http.put<GuardarAsistenciaResponse>(
      `${this.base}/registro/${idRegistro}/guardar`,
      { asistencias }
    );
  }

  consultarAsistencia(idRegistro: number): Observable<DetalleAsistencia[]> {
    return this.http.get<DetalleAsistencia[]>(
      `${this.base}/registro/${idRegistro}`
    );
  }
}
