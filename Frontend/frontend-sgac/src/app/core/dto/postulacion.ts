export interface PostulacionRequestDTO {
  idConvocatoria: number;
  idEstudiante: number;
  observaciones?: string;
  idTipoRequisito?: number;
  idTipoEstado?: number;
  observacionRequisito?: string;
}

export interface TipoRequisitoPostulacionResponseDTO {
  idTipoRequisitoPostulacion: number;
  nombreRequisito: string;
  descripcion: string;
  activo: boolean;
}

export interface PostulacionResponseDTO {
  idPostulacion: number;
  idConvocatoria: number;
  asignaturaConvocatoria: string;
  idEstudiante: number;
  nombreCompletoEstudiante: string;
  matriculaEstudiante: string;
  idPlazoActividad: number;
  fechaPostulacion: string;
  estadoPostulacion: string;
  observaciones: string;
  activo: boolean;
}

