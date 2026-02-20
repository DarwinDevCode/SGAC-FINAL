export interface AsignaturaDTO
{
  idAsignatura?: number;
  nombreAsignatura: string;
  semestre: number;
  idCarrera: number;
  nombreCarrera: string;
  activo?: boolean;
}
