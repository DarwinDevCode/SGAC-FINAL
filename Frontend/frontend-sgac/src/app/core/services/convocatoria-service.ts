// src/app/core/services/convocatoria-service.ts
import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

import {
  ConvocatoriaDTO,
  ConvocatoriaCrearRequest,
  ConvocatoriaActualizarRequest,
  ConvocatoriaNativaResponse,
  VerificarFaseResponse,
  VerificarPostulantesResponse,
} from '../models/convocatoria/convocatoria';
import { PeriodoAcademicoDTO }  from '../dto/periodo-academico';
import { AsignaturaDTO }        from '../dto/asignatura';
import { DocenteDTO }           from '../dto/docente';
import { DocenteComboDTO, AsignaturaComboDTO } from '../models/convocatoria/convocatoria.model';
import {
  ConvocatoriaEstudianteDTO,
  ConvocatoriasEstudianteWrapperDTO,
  ValidacionContextoEstudianteDTO,
  ValidacionElegibilidadAcademicaDTO,
} from '../dto/convocatoria-estudiante';

@Injectable({ providedIn: 'root' })
export class ConvocatoriaService {

  private readonly http    = inject(HttpClient);
  private readonly base    = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly apiUrl  = `${this.base}/convocatorias`;

  // ══════════════════════════════════════════════════════════════════
  // LECTURA (endpoints heredados)
  // ══════════════════════════════════════════════════════════════════

  getAll(): Observable<ConvocatoriaDTO[]> {
    return this.http.get<ConvocatoriaDTO[]>(`${this.apiUrl}/listar-vista`)
      .pipe(catchError(this.handleError));
  }

  getById(id: number): Observable<ConvocatoriaDTO> {
    return this.http.get<ConvocatoriaDTO>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  // ══════════════════════════════════════════════════════════════════
  // VERIFICACIONES (PL/pgSQL)
  // ══════════════════════════════════════════════════════════════════

  /**
   * GET /convocatorias/verificar-fase
   * Llamar al abrir el formulario para saber si la fecha actual
   * permite crear/editar convocatorias.
   */
  verificarFase(): Observable<VerificarFaseResponse> {
    return this.http.get<VerificarFaseResponse>(`${this.apiUrl}/verificar-fase`)
      .pipe(catchError(this.handleError));
  }

  /**
   * GET /convocatorias/check-postulantes/{id}
   * Llamar antes de abrir edición para decidir PARCIAL vs COMPLETA.
   */
  checkPostulantes(id: number): Observable<VerificarPostulantesResponse> {
    return this.http.get<VerificarPostulantesResponse>(
      `${this.apiUrl}/check-postulantes/${id}`
    ).pipe(catchError(this.handleError));
  }

  // ══════════════════════════════════════════════════════════════════
  // ESCRITURA (PL/pgSQL)
  // ══════════════════════════════════════════════════════════════════

  /**
   * POST /convocatorias/guardar
   * Crea una convocatoria. Las fechas las dicta el cronograma.
   */
  guardar(payload: ConvocatoriaCrearRequest): Observable<ConvocatoriaNativaResponse> {
    return this.http.post<ConvocatoriaNativaResponse>(
      `${this.apiUrl}/guardar`, payload
    ).pipe(catchError(this.handleError));
  }

  /**
   * PUT /convocatorias/actualizar
   * tipoEdicion = 'PARCIAL'  → solo cupos/estado (siempre permitido)
   * tipoEdicion = 'COMPLETA' → valida postulantes + fase del cronograma
   */
  actualizar(payload: ConvocatoriaActualizarRequest): Observable<ConvocatoriaNativaResponse> {
    return this.http.put<ConvocatoriaNativaResponse>(
      `${this.apiUrl}/actualizar`, payload
    ).pipe(catchError(this.handleError));
  }

  /**
   * PATCH /convocatorias/desactivar/{id}
   * Solo desactiva si no hay postulaciones activas.
   */
  desactivar(id: number): Observable<ConvocatoriaNativaResponse> {
    return this.http.patch<ConvocatoriaNativaResponse>(
      `${this.apiUrl}/desactivar/${id}`, {}
    ).pipe(catchError(this.handleError));
  }

  // ══════════════════════════════════════════════════════════════════
  // CATÁLOGOS
  // ══════════════════════════════════════════════════════════════════

  getPeriodoActivo(): Observable<PeriodoAcademicoDTO> {
    return this.http.get<PeriodoAcademicoDTO>(`${this.base}/periodos-academicos/activo`)
      .pipe(catchError(this.handleError));
  }

  getAsignaturas(): Observable<AsignaturaDTO[]> {
    return this.http.get<AsignaturaDTO[]>(`${this.base}/admin/catalogos/asignaturas`)
      .pipe(catchError(this.handleError));
  }

  getDocentes(): Observable<DocenteDTO[]> {
    return this.http.get<DocenteDTO[]>(`${this.base}/recursos/docentes`)
      .pipe(catchError(this.handleError));
  }

  getDocentesCarrera(): Observable<DocenteComboDTO[]> {
    return this.http.get<DocenteComboDTO[]>(`${this.base}/coordinador/docentes-seleccionables`)
      .pipe(catchError(this.handleError));
  }

  getAsignaturasPorDocente(idDocente: number): Observable<AsignaturaComboDTO[]> {
    return this.http.get<AsignaturaComboDTO[]>(
      `${this.base}/coordinador/docentes/${idDocente}/asignaturas`
    ).pipe(catchError(this.handleError));
  }

  // ══════════════════════════════════════════════════════════════════
  // ESTUDIANTE
  // ══════════════════════════════════════════════════════════════════

  listarConvocatoriasElegibles(idUsuario: number): Observable<ConvocatoriaEstudianteDTO[]> {
    return this.http.get<ConvocatoriaEstudianteDTO[]>(
      `${this.apiUrl}/listar-por-estudiante/${idUsuario}`
    ).pipe(catchError(this.handleError));
  }

  getMisConvocatoriasElegibles(): Observable<ConvocatoriasEstudianteWrapperDTO> {
    return this.http.get<ConvocatoriasEstudianteWrapperDTO>(
      `${this.base}/estudiante/convocatorias`
    ).pipe(catchError(this.handleError));
  }

  getConvocatoriasElegiblesPorUsuario(idUsuario: number): Observable<ConvocatoriaEstudianteDTO[]> {
    return this.http.get<ConvocatoriaEstudianteDTO[]>(
      `${this.base}/estudiante/convocatorias/listar/${idUsuario}`
    ).pipe(catchError(this.handleError));
  }

  validarContextoEstudiante(idUsuario: number): Observable<ValidacionContextoEstudianteDTO> {
    return this.http.get<ValidacionContextoEstudianteDTO>(
      `${this.base}/estudiante/convocatorias/validar-contexto/${idUsuario}`
    ).pipe(catchError(this.handleError));
  }

  validarElegibilidadAcademica(idEstudiante: number): Observable<ValidacionElegibilidadAcademicaDTO> {
    return this.http.get<ValidacionElegibilidadAcademicaDTO>(
      `${this.base}/estudiante/convocatorias/validar-elegibilidad/${idEstudiante}`
    ).pipe(catchError(this.handleError));
  }

  // ══════════════════════════════════════════════════════════════════
  // MANEJO DE ERRORES
  // ══════════════════════════════════════════════════════════════════

  private handleError(err: HttpErrorResponse): Observable<never> {
    // GlobalExceptionHandler retorna { message: "..." } en 422/409/400
    const msg = err.error?.message
      ?? err.error?.mensaje
      ?? (typeof err.error === 'string' ? err.error : null)
      ?? `Error ${err.status}: ${err.statusText}`;
    return throwError(() => new Error(msg));
  }
}
