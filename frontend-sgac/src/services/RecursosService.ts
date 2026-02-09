import http from "../config/http-common";
import type { AxiosResponse } from "axios";

// INTERFAZ EXACTA SEGÚN TU JSON
export interface DocenteDTO {
  idDocente: number;            // Viene en el JSON
  nombreCompletoUsuario: string; // <--- ESTE ES EL IMPORTANTE
  // Los otros campos (correo, cedula) son opcionales si no los usas en el select
}

export interface AsignaturaDTO {
  idAsignatura: number;
  nombreAsignatura: string;
}

export interface PeriodoDTO {
  idPeriodoAcademico: number;
  nombrePeriodo: string;
}

class RecursosService {
  getDocentes(): Promise<AxiosResponse<DocenteDTO[]>> {
    return http.get<DocenteDTO[]>("/recursos/docentes");
  }

  getAsignaturas(): Promise<AxiosResponse<AsignaturaDTO[]>> {
    return http.get<AsignaturaDTO[]>("/recursos/asignaturas");
  }

  getPeriodoActivo(): Promise<AxiosResponse<PeriodoDTO>> {
    return http.get<PeriodoDTO>("/recursos/periodos");
  }
}

export default new RecursosService();
