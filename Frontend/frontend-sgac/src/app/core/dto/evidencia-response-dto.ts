export interface EvidenciaResponseDTO {
  idEvidenciaRegistroActividad: number;
  nombreArchivo: string;
  rutaArchivo: string;

  /** mimeType permite inferir el tipo de archivo en UI */
  mimeType: string;

  tamanioBytes: number;

  /** En el backend puede venir null porque ya no es necesario en UI */
  tipoEvidencia?: string;

  idTipoEstadoEvidencia?: number;

  /** Se conserva por compatibilidad (antes se usaba este campo) */
  estadoEvidencia?: string;

  /** Nuevo: nombre del estado de evidencia (SUBIDO/REVISADO/APROBADO/RECHAZADO/OBSERVADO si aplica) */
  nombreEstadoEvidencia?: string;

  /** Fecha de subida (ISO yyyy-MM-dd) */
  fechaSubida: string;

  /** Observación/retroalimentación del docente sobre la evidencia */
  observacionDocente?: string;

  /** Fecha de observación (ISO yyyy-MM-dd) */
  fechaObservacion?: string;
}
