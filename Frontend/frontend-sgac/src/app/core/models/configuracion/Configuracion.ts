export interface StandardResponse<T = void> {
  exito: boolean;
  mensaje?: string;
  id?: number;
  datos?: T;
}

export interface PeriodoAcademicoRequest {
  nombrePeriodo: string;
  fechaInicio: string;
  fechaFin: string;
}

export interface PeriodoFaseResponse {
  idPeriodoFase: number;
  idPeriodoAcademico: number;
  idTipoFase: number;
  nombreFase: string;
  orden: number;
  fechaInicio: string;
  fechaFin: string;
}

export interface FaseCronogramaRequest {
  idTipoFase: number;
  fechaInicio: string;
  fechaFin: string;
}

export interface AjusteCronogramaRequest {
  idPeriodo: number;
  fases: FaseCronogramaRequest[];
}
