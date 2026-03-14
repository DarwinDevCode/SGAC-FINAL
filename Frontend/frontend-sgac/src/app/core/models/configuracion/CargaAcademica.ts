export interface DocenteActivoDTO {
  idDocente:        number;
  nombres:          string;
  apellidos:        string;
  cedula:           string;
  correo:           string;
  totalAsignaturas: number;
}

export interface AsignaturaJerarquiaDTO {
  idAsignatura:     number;
  nombreAsignatura: string;
  semestre:         number;
  idCarrera:        number;
  nombreCarrera:    string;
  idFacultad:       number;
  nombreFacultad:   string;
  etiqueta:         string;
}

export interface SincronizarCargaRequest {
  idDocente:      number;
  asignaturasIds: number[];
}

export interface SincronizarCargaResponse {
  exito:                boolean;
  idDocente:            number;
  nombreDocente:        string;
  correoDocente:        string;
  revocadas:            number;
  asignadas:            number;
  sinCambio:            number;
  asignaturasActuales:  string[];
  asignaturasRevocadas: string[];
  mensaje:              string;
}
