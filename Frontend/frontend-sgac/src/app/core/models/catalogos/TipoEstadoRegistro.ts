export interface TipoEstadoRegistroResponse {
  id: number;
  nombre_estado: string;
  descripcion: string;
  codigo: string;
  activo: boolean;
}

export interface TipoEstadoRegistroRequest {
  nombre_estado: string;
  descripcion: string;
  codigo: string;
}
