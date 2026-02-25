export interface ConvocatoriaDTO {
  idConvocatoria?: number;
  nombrePeriodo?: string;
  nombreAsignatura?: string;
  idPeriodoAcademico?: number;
  idAsignatura?: number;
  idDocente?: number;
  cuposDisponibles?: number;
  fechaPublicacion?: string | number[];
  fechaCierre?: string | number[];
  estado?: string;
  activo?: boolean;
}
