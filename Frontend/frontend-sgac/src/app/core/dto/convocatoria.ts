export interface ConvocatoriaDTO {
  idConvocatoria?: number;
  nombrePeriodo?: string;
  nombreAsignatura?: string;
  idPeriodoAcademico?: number;
  idAsignatura?: number;
  idDocente?: number;
  idUsuarioDocente?: number;
  idUsuarioCoordinador?: number;
  cuposDisponibles?: number;
  fechaPublicacion?: string | number[];
  fechaCierre?: string | number[];
  estado?: string;
  activo?: boolean;
}
