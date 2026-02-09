export interface Convocatoria {
  idConvocatoria?: number;

  idPeriodoAcademico: number;
  idAsignatura: number;
  idDocente: number;

  nombrePeriodo?: string;
  nombreAsignatura?: string;
  nombreDocente?: string;

  cuposDisponibles: number;
  fechaPublicacion: string;
  fechaCierre: string;
  estado: string;
  activo: boolean;
}
