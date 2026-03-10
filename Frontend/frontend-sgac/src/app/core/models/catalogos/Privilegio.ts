export interface PrivilegioResponse {
  id: number;
  nombre_privilegio: string;
  codigo_interno: string;
  descripcion: string;
  activo: boolean;
}

export interface PrivilegioRequest {
  nombre_privilegio: string;
  codigo_interno: string;
  descripcion: string;
}

