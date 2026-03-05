export interface NotificacionResponseDTO {
  idNotificacion: number;
  titulo: string;
  mensaje: string;
  tipo: string;
  idReferencia: number | null;
  leido: boolean;
  fechaCreacion: string;
  fechaLectura?: string | null;
}

// Alias más corto para el front
export type Notificacion = NotificacionResponseDTO;
