export interface StandardModificacionResponse {
  exito: boolean;
  mensaje: string;
  id: number;
}

export interface StandardConsultaResponse<T> {
  exito: boolean;
  datos: T;
}
