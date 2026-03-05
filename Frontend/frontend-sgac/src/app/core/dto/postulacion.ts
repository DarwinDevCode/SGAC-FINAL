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
  tipoDocumentoPermitido?: string; // Ej: 'PDF', 'PDF,DOCX'
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
  comisionAsignada?: boolean;
}

export interface RequisitoAdjuntoResponseDTO {
  idRequisitoAdjunto: number;
  idPostulacion: number;
  idTipoRequisitoPostulacion?: number;
  nombreRequisito: string;
  nombreEstado: string; // 'PENDIENTE', 'APROBADO', 'OBSERVADO'
  nombreArchivo: string;
  fechaSubida: string;
  observacion?: string;
}
