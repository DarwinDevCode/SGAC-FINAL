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
  fechaPublicacion: string;
  fechaCierre: string;
  estado: string;
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
 * Wrapper para la respuesta del listado de convocatorias del estudiante.
 * Incluye metadatos sobre el resultado de la consulta.
 */
export interface ConvocatoriasEstudianteWrapperDTO {
  exito: boolean;
  mensaje: string;
  totalConvocatorias: number;
  convocatorias: ConvocatoriaEstudianteDTO[];
}

