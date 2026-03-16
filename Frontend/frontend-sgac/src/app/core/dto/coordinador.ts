export interface CoordinadorResponseDTO {
    idCoordinador: number;
    idUsuario: number;
    nombreCompletoUsuario: string;
    cedula: string;
    idCarrera: number;
    nombreCarrera: string;
    fechaInicio: string; // LocalDate as ISO string
    fechaFin: string; // LocalDate as ISO string
    activo: boolean;
}

export interface PostulantesPorConvocatoriaDTO {
    tituloConvocatoria: string;
    cantidadPostulantes: number;
}

export interface CoordinadorEstadisticasDTO {
    totalConvocatoriasPropias: number;
    convocatoriasActivas: number;
    convocatoriasInactivas: number;
    totalPostulantesRecibidos: number;
    postulantesAprobados: number;
    postulantesRechazados: number;
    postulantesEnEvaluacion: number;
    postulantesPorConvocatoria: PostulantesPorConvocatoriaDTO[];
}

export interface CoordinadorConvocatoriaReporteDTO {
    idConvocatoria: number;
    nombreAsignatura: string;
    nombreCarrera: string;
    nombrePeriodo: string;
    fechaInicio: string; // LocalDate
    fechaFin: string;    // LocalDate
    cuposAprobados: number;
    estado: string;
    numeroPostulantes: number;
}

export interface CoordinadorPostulanteReporteDTO {
    idPostulacion: number;
    nombreEstudiante: string;
    cedula: string;
    nombreAsignatura: string;
    nombrePeriodo: string;
    fechaPostulacion: Date;
    estadoEvaluacion: string;
    // Campos desglosados
    puntajeMeritos?: number;
    puntajeOposicion?: number;
    puntajeTotal?: number;
    observacionPostulacion?: string;
}
