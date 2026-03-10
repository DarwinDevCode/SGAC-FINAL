/**
 * DTO para una convocatoria elegible para el estudiante.
 * Mapea el resultado de fn_listar_convocatorias_estudiante.
 */
export interface ConvocatoriaEstudianteDTO {
  idConvocatoria: number;
  nombreAsignatura: string;
  semestreAsignatura: number;
  nombreCarrera: string;
  nombreDocente: string;
  cuposDisponibles: number;
  fechaInicioPostulacion: string;  // ISO date string (YYYY-MM-DD)
  fechaFinPostulacion: string;     // ISO date string (YYYY-MM-DD)
  estadoConvocatoria: 'PROXIMAMENTE' | 'ABIERTA' | 'FINALIZADA' | string;
  puedePostular: boolean;
}

/**
 * DTO para el resultado de validación de contexto del estudiante.
 * Mapea el resultado de fn_validar_contexto_estudiante.
 */
export interface ValidacionContextoEstudianteDTO {
  idEstudiante: number | null;
  esValido: boolean;
  mensaje: string;
}

/**
 * DTO para el resultado de validación de elegibilidad académica.
 * Mapea el resultado de fn_verificar_elegibilidad_academica.
 */
export interface ValidacionElegibilidadAcademicaDTO {
  esElegible: boolean;
  mensaje: string;
}

/**
 * @deprecated Ya no se usa un wrapper, el endpoint retorna directamente el array.
 * Se mantiene por compatibilidad con código existente.
 */
export interface ConvocatoriasEstudianteWrapperDTO {
  exito: boolean;
  mensaje: string;
  totalConvocatorias: number;
  convocatorias: ConvocatoriaEstudianteDTO[];
}

