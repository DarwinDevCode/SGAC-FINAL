export interface TipoEstadoEvidenciaResponse {
  id: number;
  nombre_estado: string;
  descripcion: string;
  codigo: string;
  activo: boolean;
}

export interface TipoEstadoEvidenciaRequest {
  nombreEstado: string;
  descripcion: string;
  codigo: string;
}
