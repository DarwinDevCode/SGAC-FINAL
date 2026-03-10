export interface TipoFaseResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  orden: number;
  activo: boolean;
}

export interface TipoFaseRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
  orden: number;
}
