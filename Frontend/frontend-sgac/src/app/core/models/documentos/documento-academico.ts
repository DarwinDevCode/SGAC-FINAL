export interface RespuestaOperacion<T> {
  valido: boolean;
  mensaje: string;
  datos: T;
}

export interface Facultad {
  id: number;
  nombre: string;
}

export interface Carrera {
  id: number;
  nombre: string;
}

export interface TipoDocumento {
  id: number;
  nombre: string;
  codigo: string;
}

export enum NivelDocumento {
  GLOBAL = 'GLOBAL',
  FACULTAD = 'FACULTAD',
  CARRERA = 'CARRERA',
}

export function resolverNivel(
  idFacultad: number | null,
  idCarrera: number | null
): NivelDocumento {
  if (idFacultad === null && idCarrera === null) return NivelDocumento.GLOBAL;
  if (idFacultad !== null && idCarrera === null) return NivelDocumento.FACULTAD;
  return NivelDocumento.CARRERA;
}

export interface DocumentoCrearRequest {
  archivo: File;
  nombre: string;
  idTipo: number;
  idUsuario: number;
  idFacultad: number | null;
  idCarrera: number | null;
}

export interface DocumentoActualizarRequest {
  idDocumento: number;
  nombreMostrar: string;
  idTipoDoc: number;
  idFacultad: number | null;
  idCarrera: number | null;
  archivo?: File;
  idUsuario: number;
}

export interface DocumentoVisor {
  idDocumento: number;
  nombreMostrar: string;
  rutaArchivo: string;
  extension: string | null;
  pesoBytes: number | null;
  fechaSubida: string;
  tipoDocumento: string;
  nombreFacultad: string | null;
  nombreCarrera: string | null;
}

export interface DocumentoIdResponse {
  id: number;
}

export interface DocumentoEliminadoResponse {
  ruta: string;
}

export interface DocumentoFormState {
  nombreMostrar: string;
  idTipoDoc: number | null;
  nivel: NivelDocumento;
  idFacultad: number | null;
  idCarrera: number | null;
  archivo: File | null;
  esModoEdicion: boolean;
  idDocumentoEdicion?: number;
}

export interface DocumentoFiltros {
  busqueda: string;
  idFacultad?: number | null;
  idCarrera?: number | null;
  idTipo?: number | null;
}
