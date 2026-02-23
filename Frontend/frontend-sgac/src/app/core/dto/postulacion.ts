export interface PostulacionDTO {
  idPostulacion: number;
  idConvocatoria: number;
  asignaturaConvocatoria: string;
  idEstudiante: number;
  nombreCompletoEstudiante: string;
  matriculaEstudiante: string;
  fechaPostulacion: string;
  estadoPostulacion: string;
  observaciones: string;
  activo: boolean;
}
