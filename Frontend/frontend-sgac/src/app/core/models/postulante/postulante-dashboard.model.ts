export interface ResumenProceso {
  fechaInicioPeriodo: string | null;
  fechaFinPeriodo: string | null;
  fechaInicioFaseActual: string | null;
  fechaFinFaseActual: string | null;
  porcentajeAvance: number;
}

export interface UltimaPostulacion {
  idPostulacion: number;
  asignatura: string;
  docente: string;
  fechaPostulacion: string;
  estado: string;
  observacion: string | null;
}

export interface ConvocatoriaDestacada {
  idConvocatoria: number;
  nombreAsignatura: string;
  semestreAsignatura: number;
  nombreCarrera: string;
  nombreDocente: string;
  cuposDisponibles: number;
  fechaInicioPostulacion: string;
  fechaFinPostulacion: string;
  estadoConvocatoria: string;
  puedePostular: boolean;
  yaPostulado: boolean;
}

export interface PostulanteDashboard {
  convocatoriasAbiertas: number;
  misPostulaciones: number;
  faseActual: string;
  periodoAcademico: string;
  resumenProceso: ResumenProceso | null;
  ultimaPostulacion: UltimaPostulacion | null;
  convocatoriasDestacadas: ConvocatoriaDestacada[];
}
