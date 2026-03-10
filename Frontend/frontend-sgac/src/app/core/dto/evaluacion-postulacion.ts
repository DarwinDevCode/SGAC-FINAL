/**
 * DTOs para la evaluación de postulaciones por parte del coordinador
 */

// Listado de postulaciones para el coordinador
export interface PostulacionListadoCoordinador {
  id_postulacion: number;
  id_convocatoria: number;
  id_estudiante: number;
  nombre_estudiante: string;
  matricula: string;
  semestre: number;
  nombre_asignatura: string;
  nombre_carrera: string;
  fecha_postulacion: string;
  estado_codigo: string;
  estado_nombre: string;
  requiere_atencion: boolean;
  total_documentos: number;
  documentos_pendientes: number;
  documentos_aprobados: number;
  documentos_observados: number;
  observaciones?: string;
}

// Detalle completo de una postulación
export interface DetallePostulacionCoordinador {
  postulacion: PostulacionInfoCoordinador;
  estudiante: EstudianteInfo;
  convocatoria: ConvocatoriaInfo;
  documentos: DocumentoEvaluacion[];
  resumen_documentos: ResumenDocumentosEvaluacion;
  puede_aprobar: boolean;
}

export interface PostulacionInfoCoordinador {
  id_postulacion: number;
  fecha_postulacion: string;
  estado_codigo: string;
  estado_nombre: string;
  observaciones?: string;
}

export interface EstudianteInfo {
  id_estudiante: number;
  nombre_completo: string;
  email: string;
  matricula: string;
  semestre: number;
  estado_academico?: string;
}

export interface ConvocatoriaInfo {
  id_convocatoria: number;
  asignatura: string;
  docente: string;
  fecha_publicacion: string;
  fecha_cierre: string;
  cupos_disponibles: number;
}

export interface DocumentoEvaluacion {
  id_requisito_adjunto: number;
  tipo_requisito: string;
  descripcion_requisito?: string;
  nombre_archivo: string;
  fecha_subida: string;
  estado_codigo: string;
  id_tipo_estado_requisito: number;
  observacion?: string;
  tiene_archivo: boolean;
}

export interface ResumenDocumentosEvaluacion {
  total: number;
  pendientes: number;
  aprobados: number;
  observados: number;
  rechazados: number;
}

// Request para evaluar documento
export interface EvaluarDocumentoRequest {
  id_requisito_adjunto: number;
  accion: 'VALIDAR' | 'OBSERVAR' | 'RECHAZAR';
  observacion?: string;
}

// Response de evaluación de documento
export interface EvaluacionDocumentoResponse {
  exito: boolean;
  codigo: string;
  mensaje: string;
  id_requisito_adjunto?: number;
  nuevo_estado?: string;
  tiene_observados?: boolean;
  todos_validados?: boolean;
  puede_aprobar_postulacion?: boolean;
}

// Request para dictaminar postulación
export interface DictaminarPostulacionRequest {
  id_postulacion: number;
  accion: 'APROBAR' | 'RECHAZAR';
  observacion?: string;
}

// Response de dictamen
export interface DictamenPostulacionResponse {
  exito: boolean;
  codigo: string;
  mensaje: string;
  id_postulacion?: number;
  nuevo_estado?: string;
}

// Response de cambio de estado a revisión
export interface CambioEstadoRevisionResponse {
  exito: boolean;
  mensaje: string;
  estado_anterior?: string;
  estado_actual?: string;
  cambio_realizado?: boolean;
}

// Request para asignar comisión de evaluación de oposición
export interface AsignarComisionRequest {
  idPostulacion: number;
  idComisionSeleccion: number;
  temaExposicion: string;
  fechaEvaluacion: string; // "YYYY-MM-DD"
  horaInicio: string; // "HH:mm" o "HH:mm:ss"
  horaFin: string; // "HH:mm" o "HH:mm:ss"
  lugar: string;
}

