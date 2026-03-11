export interface TipoEstadoRequisitoResponse {
  id: number;
  nombre_estado: string;
  descripcion: string;
  codigo: string;
  activo: boolean;
}

export interface TipoEstadoRequisitoRequest {
  nombreEstado: string;
  descripcion: string;
  codigo: string;
}
