import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

import {
  RespuestaOperacion,
  ParticipanteRequestDTO,
  ParticipanteIdResponseDTO,
  PlanificarSesionRequestDTO,
  PlanificacionResponseDTO,
  AsistenciaSesionActualResponseDTO,
  MarcadoAsistenciaRequestDTO,
  BorradorSesionResponseDTO,
  EvidenciaIdResponseDTO,
  FinalizarSesionRequestDTO,
  FinalizarSesionResponseDTO,
  ParticipantePadronDTO
} from '../../models/general/respuesta-operacion';

@Injectable({
  providedIn: 'root'
})
export class AyudantiaService {

  private readonly URL_PARTICIPANTES = `${environment.apiUrl}/ayudantias/participantes`;
  private readonly URL_SESIONES = `${environment.apiUrl}/ayudantias/sesiones`;
  private readonly URL_CIERRE = `${environment.apiUrl}/ayudantias/sesiones/cierre`;

  constructor(private http: HttpClient) { }

  gestionarParticipante(request: ParticipanteRequestDTO): Observable<RespuestaOperacion<ParticipanteIdResponseDTO>> {
    return this.http.post<RespuestaOperacion<ParticipanteIdResponseDTO>>(
      `${this.URL_PARTICIPANTES}/gestionar`,
      request
    );
  }

  planificarSesion(request: PlanificarSesionRequestDTO): Observable<RespuestaOperacion<PlanificacionResponseDTO>> {
    return this.http.post<RespuestaOperacion<PlanificacionResponseDTO>>(
      `${this.URL_SESIONES}/planificar`,
      request
    );
  }

  obtenerSesionActual(): Observable<RespuestaOperacion<AsistenciaSesionActualResponseDTO>> {
    return this.http.get<RespuestaOperacion<AsistenciaSesionActualResponseDTO>>(
      `${this.URL_SESIONES}/actual`
    );
  }

  marcarAsistencia(request: MarcadoAsistenciaRequestDTO): Observable<RespuestaOperacion<void>> {
    return this.http.patch<RespuestaOperacion<void>>(
      `${this.URL_SESIONES}/marcar-asistencia`,
      request
    );
  }

  obtenerBorrador(idRegistro: number): Observable<RespuestaOperacion<BorradorSesionResponseDTO>> {
    return this.http.get<RespuestaOperacion<BorradorSesionResponseDTO>>(
      `${this.URL_CIERRE}/${idRegistro}/borrador`
    );
  }

  guardarProgreso(idRegistro: number, descripcion: string): Observable<RespuestaOperacion<void>> {
    return this.http.put<RespuestaOperacion<void>>(
      `${this.URL_CIERRE}/${idRegistro}/progreso`,
      descripcion
    );
  }

  cargarEvidencia(
    idRegistro: number,
    idTipoEvidencia: number,
    archivo: File
  ): Observable<RespuestaOperacion<EvidenciaIdResponseDTO>> {

    const formData = new FormData();
    formData.append('idTipoEvidencia', idTipoEvidencia.toString());
    formData.append('archivo', archivo);

    return this.http.post<RespuestaOperacion<EvidenciaIdResponseDTO>>(
      `${this.URL_CIERRE}/${idRegistro}/evidencia`,
      formData
    );
  }

  eliminarEvidencia(idEvidencia: number): Observable<RespuestaOperacion<void>> {
    return this.http.delete<RespuestaOperacion<void>>(
      `${this.URL_CIERRE}/evidencia/${idEvidencia}`
    );
  }

  finalizarSesion(request: FinalizarSesionRequestDTO): Observable<RespuestaOperacion<FinalizarSesionResponseDTO>> {
    return this.http.post<RespuestaOperacion<FinalizarSesionResponseDTO>>(
      `${this.URL_CIERRE}/finalizar`,
      request
    );
  }

  obtenerPadron(): Observable<RespuestaOperacion<ParticipantePadronDTO[]>> {
    return this.http.get<RespuestaOperacion<ParticipantePadronDTO[]>>(
      this.URL_PARTICIPANTES
    );
  }
}
