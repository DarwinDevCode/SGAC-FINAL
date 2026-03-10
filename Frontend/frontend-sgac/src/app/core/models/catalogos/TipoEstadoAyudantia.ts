export interface TipoEstadoAyudantiaResponse {
  id: number;
  nombre_estado: string;
  descripcion: string;
  codigo: string;
  activo: boolean;
}

export interface TipoEstadoAyudantiaRequest {
  nombre_estado: string;
  descripcion: string;
  codigo: string;
}

