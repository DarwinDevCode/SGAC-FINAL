export interface Ayudante {
  idAyudantia: number;
  idConvocatoria: number;

  idPeriodoAcademico: number;
  periodoAcademico: string;

  idAsignatura: number;
  asignatura: string;

  idUsuarioAyudante: number;
  nombresAyudante: string;
  apellidosAyudante: string;

  fechaInicio: string | null;
  fechaFin: string | null;

  horasMaximas: number | string | null;
  horasCumplidas: number | null;

  estadoAyudantia: string;
}

export interface Evidencia {
  idEvidencia: number;
  nombreArchivo: string;
  rutaArchivo: string;
  mimeType: string | null;
  tamanioBytes: number | null;
  fechaSubida: string | null;

  idTipoEstadoEvidencia: number | null;
  estadoEvidencia: string | null;

  observaciones: string | null;
  fechaObservacion: string | null;

  /** Campo local para el form de evaluación de evidencia */
  _observacionInput?: string;
  _estadoSeleccionado?: number | null;
}

export interface RegistroActividad {
  idRegistroActividad: number;
  idAyudantia: number;

  descripcionActividad: string | null;
  temaTratado: string | null;
  fecha: string | null;

  numeroAsistentes: number | null;
  horasDedicadas: number | string | null;

  idTipoEstadoRegistro: number | null;
  estadoRegistro: string | null;

  observaciones: string | null;
  fechaObservacion: string | null;

  evidencias: Evidencia[];

  /** Campos locales para control de cambio */
  _estadoSeleccionado?: number | null;
  _observacionInput?: string;
}

export interface EvaluacionActividadRequest {
  idTipoEstadoRegistro: number;
  observaciones?: string | null;
}

export interface EvaluacionEvidenciaRequest {
  idTipoEstadoEvidencia: number;
  observaciones?: string | null;
}

