export interface GestionPermisosRequestDTO {
  rolBd: string;
  esquema: string;
  elemento: string;
  categoria: string;
  privilegio: string;
  otorgar: boolean;
}
