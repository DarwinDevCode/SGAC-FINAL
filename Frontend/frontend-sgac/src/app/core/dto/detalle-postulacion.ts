/**
 * DTOs para la visualización y gestión de postulación activa del estudiante.
 * Mapean la respuesta de fn_ver_detalle_postulacion.
 */

/**
 * Etapa del cronograma de la convocatoria.
 */
export interface EtapaCronogramaDTO {
  numero: number;
  nombre: string;
  descripcion: string;
  fecha_inicio: string;
  fecha_fin: string;
  estado: 'PENDIENTE' | 'EN_CURSO' | 'COMPLETADA';
}

/**
 * Documento adjunto de la postulación.
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
}

/**
 * Información básica de la postulación.
 */
export interface PostulacionInfoDTO {
  id_postulacion: number;
  fecha_postulacion: string;
  estado_postulacion: string;
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
  estado_convocatoria: string;
  fecha_publicacion: string;
  fecha_cierre: string;
}

/**
 * Resumen de estados de los documentos.
 */
export interface ResumenDocumentosDTO {
  pendientes: number;
  aprobados: number;
  observados: number;
  rechazados: number;
}

/**
 * Respuesta completa del detalle de postulación activa.
 */
export interface DetallePostulacionResponseDTO {
  exito: boolean;
  codigo?: string;
  mensaje: string;
  postulacion?: PostulacionInfoDTO;
  convocatoria?: ConvocatoriaPostulacionDTO;
  cronograma?: EtapaCronogramaDTO[];
  documentos?: DocumentoPostulacionDTO[];
  total_documentos?: number;
  resumen_documentos?: ResumenDocumentosDTO;
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
}

