export interface ParticipanteIdResponseDTO {
  id: number;
}

export interface SnapshotAsistenciaResponseDTO {
  total: number;
}

export interface PlanificacionResponseDTO {
  idRegistro: number;
  detalleAsistencia: SnapshotAsistenciaResponseDTO;
}

export interface SesionInfoDTO {
  idRegistro: number;
  tema: string;
  fecha: string;
  horario: string;
  lugar: string;
  puedeEditar: boolean;
}

export interface EstudianteAsistenciaDTO {
  idDetalle: number;
  nombreCompleto: string;
  curso: string;
  paralelo: string;
  asistio: boolean;
}

export interface AsistenciaSesionActualResponseDTO {
  sesion: SesionInfoDTO;
  estudiantes: EstudianteAsistenciaDTO[];
}

export interface DetalleBorradorDTO {
  idRegistro: number;
  tema: string;
  fecha: string;
  lugar: string;
  descripcionActual: string;
  codigoEstado: string;
}

export interface EvidenciaResponseDTO {
  idEvidencia: number;
  nombreArchivo: string;
  rutaArchivo: string;
  mimeType: string;
  tamanioBytes: number;
  fechaSubida: string;
}

export interface BorradorSesionResponseDTO {
  detalle: DetalleBorradorDTO;
  evidencias: EvidenciaResponseDTO[];
}

export interface EvidenciaIdResponseDTO {
  idEvidencia: number;
}

export interface EvidenciaEliminadaResponseDTO {
  idEvidenciaEliminada: number;
  rutaArchivo: string;
}

export interface ResumenAsistenciaDTO {
  total: number;
  asistieron: number;
  faltaron: number;
}

export interface FinalizarSesionResponseDTO {
  idRegistro: number;
  resumenAsistencia: ResumenAsistenciaDTO;
  evidenciasAdjuntas: number;
}
