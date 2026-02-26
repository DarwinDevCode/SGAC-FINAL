export interface NotificacionResponseDTO {
  idNotificacion: number;
  idUsuario: number;
  mensaje: string;
  fechaEnvio: string; // ISO String mapping LocalDatetime
  leida: boolean;
}
