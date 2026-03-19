export interface DecanoResponseDTO {
    idDecano: number;
    idUsuario: number;
    nombreCompletoUsuario: string;
    idFacultad: number;
    nombreFacultad: string;
    fechaInicioGestion: string;
    fechaFinGestion: string;
    activo: boolean;
}

export interface ActividadCoordinadorDTO {
    nombreCoordinador: string;
    totalConvocatorias: number;
}

export interface DecanoEstadisticasDTO {
    totalConvocatorias: number;
    convocatoriasActivas: number;
    convocatoriasInactivas: number;
    totalPostulantes: number;
    postulantesSeleccionados: number;
    postulantesNoSeleccionados: number;
    postulantesEnEvaluacion: number;
    postulantesPendientes: number;
    actividadPorCoordinador: ActividadCoordinadorDTO[];
}

export interface ConvocatoriaReporteDTO {
    idConvocatoria: number;
    nombreAsignatura: string;
    nombreCarrera: string;
    nombreCoordinador: string;
    fechaInicio: string;  // se maneja como string ISO
    fechaFin: string;     // se maneja como string ISO
    estado: string;
    numeroPostulantes: number;
}

export interface LogAuditoriaDTO {
    idLog: number;
    nombreUsuario: string;
    accion: string;
    tablaAfectada: string;
    fechaHora: string; // LocalDateTime devuelto como string
}

export interface CoordinadorPostulanteReporteDTO {
    idPostulacion: number;
    nombreEstudiante: string;
    cedula: string;
    nombreAsignatura: string;
    nombrePeriodo: string;
    fechaPostulacion: string;
    estadoEvaluacion: string;
}
