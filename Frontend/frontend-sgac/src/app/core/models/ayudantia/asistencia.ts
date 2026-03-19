export interface ParticipanteRequestDTO {
  accion: string;
  idUsuario?: number;
  nombre: string;
  curso: string;
  paralelo: string;
  idParticipante?: number;
}

export interface PlanificarSesionRequestDTO {
  idUsuario?: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  lugar: string;
  tema: string;
}

export interface MarcadoAsistenciaRequestDTO {
  idUsuario?: number;
  idDetalle: number;
  asistio: boolean;
}

export interface FinalizarSesionRequestDTO {
  idUsuario?: number;
  idRegistro: number;
  descripcion: string;
}
