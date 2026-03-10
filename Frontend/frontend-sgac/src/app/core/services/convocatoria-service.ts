import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { PeriodoAcademicoDTO } from '../dto/periodo-academico';
import { AsignaturaDTO } from '../dto/asignatura';
import { DocenteDTO } from '../dto/docente';
import { DocenteComboDTO, AsignaturaComboDTO } from '../models/convocatoria.model';
import {
  ConvocatoriaEstudianteDTO,
  ConvocatoriasEstudianteWrapperDTO,
  ValidacionContextoEstudianteDTO,
  ValidacionElegibilidadAcademicaDTO
} from '../dto/convocatoria-estudiante';

@Injectable({
  providedIn: 'root',
})
export class ConvocatoriaService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly apiUrl = `${this.baseUrl}/convocatorias`;

  getAll(): Observable<ConvocatoriaDTO[]> {
    return this.http.get<ConvocatoriaDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<ConvocatoriaDTO> {
    return this.http.get<ConvocatoriaDTO>(`${this.apiUrl}/${id}`);
  }

  create(convocatoria: ConvocatoriaDTO): Observable<ConvocatoriaDTO> {
    return this.http.post<ConvocatoriaDTO>(`${this.apiUrl}/crear`, convocatoria);
  }

  update(convocatoria: ConvocatoriaDTO): Observable<ConvocatoriaDTO> {
    return this.http.put<ConvocatoriaDTO>(`${this.apiUrl}/actualizar`, convocatoria);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }


  getPeriodoActivo(): Observable<PeriodoAcademicoDTO[]> {
    return this.http.get<PeriodoAcademicoDTO[]>(`${this.baseUrl}/recursos/periodos`)
  }

  getPeriodoActivo2(): Observable<PeriodoAcademicoDTO> {
    return this.http.get<PeriodoAcademicoDTO>(`${this.baseUrl}/periodos-academicos/activo`);
  }


  getAsignaturas(): Observable<AsignaturaDTO[]> {
    return this.http.get<AsignaturaDTO[]>(`${this.baseUrl}/admin/catalogos/asignaturas`);
  }

  getDocentes(): Observable<DocenteDTO[]> {
    return this.http.get<DocenteDTO[]>(`${this.baseUrl}/recursos/docentes`);
  }

  /** Coordinador: docentes filtrados por carrera (backend resuelve el contexto por JWT). */
  getDocentesCarrera() {
    return this.http.get<DocenteComboDTO[]>(`${this.baseUrl}/coordinador/docentes-seleccionables`);
  }

  /** Coordinador: asignaturas activas filtradas por docente (solo relaciones activas). */
  getAsignaturasPorDocente(idDocente: number) {
    return this.http.get<AsignaturaComboDTO[]>(`${this.baseUrl}/coordinador/docentes/${idDocente}/asignaturas`);
  }

  // ==================================================================================
  // Métodos para Estudiante - Convocatorias Elegibles
  // ==================================================================================

  /**
   * Lista las convocatorias elegibles para un estudiante específico.
   * Endpoint: GET /api/convocatorias/listar-por-estudiante/{idUsuario}
   *
   * La función de PostgreSQL aplica los siguientes filtros:
   * - Valida que el usuario sea un estudiante activo
   * - Valida que esté en 6to semestre o superior
   * - Filtra convocatorias de su carrera
   * - Filtra asignaturas de semestres inferiores al del estudiante
   * - Calcula el estado y si puede postular según las fechas de la fase POSTULACION
   *
   * @param idUsuario ID del usuario logueado
   * @returns Lista de convocatorias elegibles con información de estado y habilitación
   */
  listarConvocatoriasElegibles(idUsuario: number): Observable<ConvocatoriaEstudianteDTO[]> {
    return this.http.get<ConvocatoriaEstudianteDTO[]>(
      `${this.apiUrl}/listar-por-estudiante/${idUsuario}`
    );
  }

  /**
   * @deprecated Usar listarConvocatoriasElegibles en su lugar
   */
  getMisConvocatoriasElegibles(): Observable<ConvocatoriasEstudianteWrapperDTO> {
    return this.http.get<ConvocatoriasEstudianteWrapperDTO>(
      `${this.baseUrl}/estudiante/convocatorias`
    );
  }

  /**
   * Lista las convocatorias elegibles para un usuario específico.
   * @param idUsuario ID del usuario a consultar
   * @returns Lista de convocatorias elegibles
   */
  getConvocatoriasElegiblesPorUsuario(idUsuario: number): Observable<ConvocatoriaEstudianteDTO[]> {
    return this.http.get<ConvocatoriaEstudianteDTO[]>(
      `${this.baseUrl}/estudiante/convocatorias/listar/${idUsuario}`
    );
  }

  /**
   * Valida el contexto del estudiante (transforma id_usuario en id_estudiante).
   * @param idUsuario ID del usuario a validar
   * @returns DTO con resultado de validación
   */
  validarContextoEstudiante(idUsuario: number): Observable<ValidacionContextoEstudianteDTO> {
    return this.http.get<ValidacionContextoEstudianteDTO>(
      `${this.baseUrl}/estudiante/convocatorias/validar-contexto/${idUsuario}`
    );
  }

  /**
   * Valida la elegibilidad académica del estudiante (semestre >= 6).
   * @param idEstudiante ID del estudiante a validar
   * @returns DTO con resultado de validación
   */
  validarElegibilidadAcademica(idEstudiante: number): Observable<ValidacionElegibilidadAcademicaDTO> {
    return this.http.get<ValidacionElegibilidadAcademicaDTO>(
      `${this.baseUrl}/estudiante/convocatorias/validar-elegibilidad/${idEstudiante}`
    );
  }
}
