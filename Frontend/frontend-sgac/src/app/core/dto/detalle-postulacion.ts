/**
 * DTOs para la visualización y gestión de postulación activa del estudiante.
 * Mapean la respuesta de fn_ver_detalle_postulacion.
 */

/**
 * Etapa del cronograma dinámico del periodo.
 * Nuevo formato basado en planificacion.periodo_fase y planificacion.tipo_fase.
 */
export interface EtapaCronogramaDTO {
  /** Nombre legible de la fase (ej: "Postulación", "Revisión Requisitos") */
  fase: string;
  /** Código único de la fase (ej: "POSTULACION", "REVISION_REQUISITOS") */
  codigo: string;
  /** Fecha de inicio de la fase (ISO date string) */
  inicio: string;
  /** Fecha de fin de la fase (ISO date string) */
  fin: string;
  /** Estado actual de la fase: 'PENDIENTE', 'EN CURSO', 'FINALIZADA' */
  estado: 'PENDIENTE' | 'EN CURSO' | 'FINALIZADA' | string;
}

/**
 * Documento adjunto de la postulación.
 * Este DTO se usa para obtener documentos por separado (no viene de fn_ver_detalle_postulacion).
 */
export interface DocumentoPostulacionDTO {
  id_requisito_adjunto: number;
  id_tipo_requisito: number;
  nombre_requisito: string;
  descripcion_requisito: string;
  tipo_documento_permitido: string;
  nombre_archivo: string;
  fecha_subida: string;
  estado: string;
  id_tipo_estado_requisito: number;
  observacion: string;
  es_editable: boolean;
  tiene_archivo: boolean;
  // === NUEVOS CAMPOS PARA GESTIÓN DE VENTANA 24 HORAS ===
  /** Timestamp del momento en que el coordinador marcó el documento como OBSERVADO */
  fecha_observacion?: string;
  /** Fecha y hora límite para subsanar el documento (fecha_observacion + 24 horas) */
  fecha_limite_subsanacion?: string;
  /** Tiempo restante en segundos para subsanar el documento */
  tiempo_restante_segundos?: number;
  /** Indica si el plazo de 24 horas ya expiró */
  plazo_expirado?: boolean;
}

/**
 * Información básica de la postulación.
 */
export interface PostulacionInfoDTO {
  id_postulacion: number;
  fecha_postulacion: string;
  /** Código del estado de la postulación (ej: "PENDIENTE", "EN_REVISION", "APROBADA") */
  estado_codigo?: string;
  /** Nombre del estado de la postulación (ej: "Pendiente", "En Revisión", "Aprobada") */
  estado_nombre: string;
  observaciones: string;
}

/**
 * Información de la convocatoria asociada a la postulación.
 */
export interface ConvocatoriaPostulacionDTO {
  id_convocatoria: number;
  nombre_asignatura: string;
  semestre_asignatura: number;
  nombre_carrera: string;
  nombre_docente: string;
  cupos_disponibles: number;
  /** Estado administrativo de la convocatoria */
  estado_admin: string;
}

/**
 * Resumen de estados de los documentos.
 * Incluye el nuevo estado 'corregidos'.
 */
export interface ResumenDocumentosDTO {
  pendientes: number;
  aprobados: number;
  observados: number;
  rechazados: number;
  corregidos: number;
}

/**
 * Respuesta completa del detalle de postulación activa.
 * Mapea el JSONB estructurado de fn_ver_detalle_postulacion.
 */
export interface DetallePostulacionResponseDTO {
  exito: boolean;
  codigo?: string;
  mensaje: string;
  postulacion?: PostulacionInfoDTO;
  convocatoria?: ConvocatoriaPostulacionDTO;
  cronograma?: EtapaCronogramaDTO[];
  /** Lista de documentos con información de estado y plazos de subsanación */
  documentos?: DocumentoDetalleDTO[];
  resumen_documentos?: ResumenDocumentosDTO;
  /**
   * Indica si actualmente es posible subsanar/reemplazar documentos observados.
   * TRUE si estamos en las fases POSTULACION o EVALUACION_REQUISITOS del periodo académico.
   */
  es_periodo_subsanacion?: boolean;
  /**
   * Indica si la postulación ha sido rechazada definitivamente.
   * Cuando es TRUE, todas las acciones sobre la postulación deben estar bloqueadas.
   */
  es_postulacion_rechazada?: boolean;
}

/**
 * Documento del detalle de postulación con información de plazos 24h.
 * Mapea los documentos retornados por fn_ver_detalle_postulacion.
 */
export interface DocumentoDetalleDTO {
  id_requisito_adjunto: number;
  tipo_requisito: string;
  nombre_archivo: string;
  fecha_subida: string;
  estado_nombre: string;
  observacion: string;
  tiene_archivo: boolean;
  es_editable: boolean;
  // Campos de ventana de 24 horas
  fecha_observacion?: string;
  fecha_limite_subsanacion?: string;
  tiempo_restante_segundos?: number;
  plazo_expirado?: boolean;
}

/**
 * Respuesta de la operación de subsanación de documento observado.
 * Mapea el resultado de fn_subsanar_documento_estudiante.
 */
export interface SubsanacionDocumentoResponseDTO {
  exito: boolean;
  codigo?: string;
  mensaje: string;
  id_requisito_adjunto?: number;
  nuevo_estado?: string;
  notificacion_enviada?: boolean;
  // Campos adicionales para feedback de plazo
  fecha_observacion?: string;
  fecha_limite?: string;
}

