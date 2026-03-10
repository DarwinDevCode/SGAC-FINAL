export interface TipoSancionResponse {
  id: number;
  nombre_tipo_sancion: string;
  codigo: string
  activo: boolean;
}

export interface TipoSancionRequest {
  nombre_tipo_sancion: string;
  codigo: string;
}
