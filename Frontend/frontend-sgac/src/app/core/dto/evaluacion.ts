export interface EvaluacionMeritosResponseDTO {
    idEvaluacionMeritos: number;
    idPostulacion: number;
    notaAsignatura: number;
    notaSemestres: number;
    notaEventos: number;
    notaExperiencia: number;
    fechaEvaluacion: string;
}

export interface EvaluacionOposicionResponseDTO {
    idEvaluacionOposicion: number;
    idPostulacion: number;
    temaExposicion: string;
    fechaEvaluacion: string;
    horaInicio: string;
    horaFin: string;
    lugar: string;
    estado: string;
}
