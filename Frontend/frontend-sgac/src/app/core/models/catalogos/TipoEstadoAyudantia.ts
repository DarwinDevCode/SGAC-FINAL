export interface TipoEstadoAyudantiaResponse {
  id: number;
  nombre_estado: string;
  descripcion: string;
  codigo: string;
  activo: boolean;
}

export interface TipoEstadoAyudantiaRequest {
  nombreEstado: string;
  descripcion: string;
  codigo: string;
}

