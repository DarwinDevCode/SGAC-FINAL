export interface TipoEstadoPostulacionResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  activo: boolean;
  fecha_creacion: string;
}

export interface TipoEstadoPostulacionRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
}

