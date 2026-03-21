export interface DocenteDashboardUltimaActividadDTO {
  fecha: string; // LocalDate (ISO yyyy-MM-dd)
  nombreEstudiante: string;
  tema: string;
  idRegistro: number;
}

export interface DocenteDashboardDTO {
  totalConvocatoriasActivas: number;
  totalPostulacionesPendientes: number;
  totalAyudantesAsignados: number;
  totalActividadesPorRevisar: number;
  ultimasActividades: DocenteDashboardUltimaActividadDTO[];
}
