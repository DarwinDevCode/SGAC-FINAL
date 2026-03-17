export interface MiembroTribunal {
  nombre: string;
  rol: string;
}

export interface EvaluacionOposicion {
  temaExposicion: string;
  lugar: string;
  fechaEvaluacion: string;
  horaInicio: string;
  horaFin: string;
  ordenExposicion: number;
}

export interface TribunalEvaluacionResponse {
  comision: string;
  evaluacion: EvaluacionOposicion;
  miembros: MiembroTribunal[];
}
