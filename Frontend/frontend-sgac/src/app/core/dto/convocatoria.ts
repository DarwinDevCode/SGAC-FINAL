export interface ConvocatoriaDTO {
  idConvocatoria?: number;
  nombrePeriodo: string;
  nombreAsignatura: string;
  idPeriodoAcademico: number;
  idAsignatura: number;
  idDocente: number;
  cuposDisponibles: number;
  fechaPublicacion: string;
  fechaCierre: string;
  estado: string;
  activo: boolean;
}
