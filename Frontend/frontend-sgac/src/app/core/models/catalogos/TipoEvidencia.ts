export interface TipoEvidenciaResponse {
  id: number;
  nombre: string;
  extension_permitida: string;
  codigo: string;
  activo: boolean;
}

export interface TipoEvidenciaRequest {
  nombre: string;
  extensionPermitida: string;
  codigo: string;
}
