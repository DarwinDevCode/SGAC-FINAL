export interface RespuestaOperacion<T> {
  valido: boolean;
  mensaje: string;
  datos: T;
}
