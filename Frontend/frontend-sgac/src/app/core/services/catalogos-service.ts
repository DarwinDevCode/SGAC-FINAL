import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, shareReplay, Subject} from 'rxjs';
import { environment } from '../../../environments/environment';
import {FacultadDTO} from '../dto/facultad';
import {CarreraDTO} from '../dto/carrera';
import {AsignaturaDTO} from '../dto/asignatura';
import {PeriodoAcademicoDTO} from '../dto/periodo-academico';
import {TipoEstadoEvidenciaAyudantiaDTO} from '../dto/tipo-estado-evidencia-ayudantia';
import {TipoEstadoRequisitoDTO} from '../dto/tipo-estado-requisito';
import {TipoRequisitoPostulacionDTO} from '../dto/tipo-requisito-postulacion';
import {TipoRolDTO} from '../dto/tipo-rol';
import {TipoSancionAyudanteCatedraDTO} from '../dto/tipo-sancion-ayudante-catedra';
import {TipoEstadoAyudantia} from '../dto/tipo-estado-ayudantia';
import {TipoEvidencia} from '../dto/tipo-evidencia';
import {TipoEstadoEvidencia} from '../dto/tipo-estado-evidencia';
import {TipoEstadoRegistro} from '../dto/tipo-estado-registro';

@Injectable({
  providedIn: 'root',
})
export class CatalogosService {
  private http = inject(HttpClient);
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API = `${this.baseUrl}/admin/catalogos`;
  public rolActualizado$ = new Subject<void>();

  private estadosRegistro$:  Observable<TipoEstadoRegistro[]>  | null = null;
  private estadosEvidencia$: Observable<TipoEstadoEvidencia[]> | null = null;
  private tiposEvidencia$:   Observable<TipoEvidencia[]>       | null = null;
  private estadosAyudantia$: Observable<TipoEstadoAyudantia[]> | null = null;

  getFacultades(): Observable<FacultadDTO[]> {
    return this.http.get<FacultadDTO[]>(`${this.API}/facultades`);
  }

  postFacultad(facultad: FacultadDTO): Observable<FacultadDTO> {
    const { idFacultad, activo, ...requestBody } = facultad;
    return this.http.post<FacultadDTO>(`${this.API}/facultades`, requestBody);
  }

  putFacultad(id: number, facultad: FacultadDTO): Observable<FacultadDTO> {
    const { idFacultad, activo, ...requestBody } = facultad;
    return this.http.put<FacultadDTO>(`${this.API}/facultades/${id}`, requestBody);
  }

  desactivarFacultad(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/facultades/${id}/desactivar`, {});
  }


  getCarreras(): Observable<CarreraDTO[]> {
    return this.http.get<CarreraDTO[]>(`${this.API}/carreras`);
  }

  postCarrera(carrera: CarreraDTO): Observable<CarreraDTO> {
    const { idCarrera, nombreFacultad, activo, ...requestBody } = carrera;
    return this.http.post<CarreraDTO>(`${this.API}/carreras`, requestBody);
  }

  putCarrera(id: number, carrera: CarreraDTO): Observable<CarreraDTO> {
    const { idCarrera, nombreFacultad, activo, ...requestBody } = carrera;
    return this.http.put<CarreraDTO>(`${this.API}/carreras/${id}`, requestBody);
  }

  desactivarCarrera(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/carreras/${id}/desactivar`, {});
  }


  getAsignaturas(): Observable<AsignaturaDTO[]> {
    return this.http.get<AsignaturaDTO[]>(`${this.API}/asignaturas`);
  }

  postAsignatura(asignatura: AsignaturaDTO): Observable<AsignaturaDTO> {
    const { idAsignatura, nombreCarrera, activo, ...requestBody } = asignatura;
    return this.http.post<AsignaturaDTO>(`${this.API}/asignaturas`, requestBody);
  }

  putAsignatura(id: number, asignatura: AsignaturaDTO): Observable<AsignaturaDTO> {
    const { idAsignatura, nombreCarrera, activo, ...requestBody } = asignatura;
    return this.http.put<AsignaturaDTO>(`${this.API}/asignaturas/${id}`, requestBody);
  }

  desactivarAsignatura(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/asignaturas/${id}/desactivar`, {});
  }


  getPeriodos(): Observable<PeriodoAcademicoDTO[]> {
    return this.http.get<PeriodoAcademicoDTO[]>(`${this.API}/periodos`);
  }

  postPeriodo(periodo: PeriodoAcademicoDTO): Observable<PeriodoAcademicoDTO> {
    const { idPeriodoAcademico, activo, ...requestBody } = periodo;
    return this.http.post<PeriodoAcademicoDTO>(`${this.API}/periodos`, requestBody);
  }

  putPeriodo(id: number, periodo: PeriodoAcademicoDTO): Observable<PeriodoAcademicoDTO> {
    const { idPeriodoAcademico, activo, ...requestBody } = periodo;
    return this.http.put<PeriodoAcademicoDTO>(`${this.API}/periodos/${id}`, requestBody);
  }

  desactivarPeriodo(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/periodos/${id}/desactivar`, {});
  }


  //getEstadosEvidencia(): Observable<TipoEstadoEvidenciaAyudantiaDTO[]> {
  //  return this.http.get<TipoEstadoEvidenciaAyudantiaDTO[]>(`${this.API}/estados-evidencia`);
  //}

  //postEstadoEvidencia(estado: TipoEstadoEvidenciaAyudantiaDTO): Observable<TipoEstadoEvidenciaAyudantiaDTO> {
  //  const { idTipoEstadoEvidenciaAyudantia, ...requestBody } = estado;
  //   return this.http.post<TipoEstadoEvidenciaAyudantiaDTO>(`${this.API}/estados-evidencia`, requestBody);
  //}

  //putEstadoEvidencia(id: number, estado: TipoEstadoEvidenciaAyudantiaDTO): Observable<TipoEstadoEvidenciaAyudantiaDTO> {
  //  const { idTipoEstadoEvidenciaAyudantia, ...requestBody } = estado;
  //  return this.http.put<TipoEstadoEvidenciaAyudantiaDTO>(`${this.API}/estados-evidencia/${id}`, requestBody);
  //}

  //desactivarEstadoEvidencia(id: number): Observable<void> {
  //  return this.http.patch<void>(`${this.API}/estados-evidencia/${id}/desactivar`, {});
  //}


  getEstadosRequisito(): Observable<TipoEstadoRequisitoDTO[]> {
    return this.http.get<TipoEstadoRequisitoDTO[]>(`${this.API}/estados-requisito`);
  }

  postEstadoRequisito(estado: TipoEstadoRequisitoDTO): Observable<TipoEstadoRequisitoDTO> {
    const { idTipoEstadoRequisito, ...requestBody } = estado;
    return this.http.post<TipoEstadoRequisitoDTO>(`${this.API}/estados-requisito`, requestBody);
  }

  putEstadoRequisito(id: number, estado: TipoEstadoRequisitoDTO): Observable<TipoEstadoRequisitoDTO> {
    const { idTipoEstadoRequisito, ...requestBody } = estado;
    return this.http.put<TipoEstadoRequisitoDTO>(`${this.API}/estados-requisito/${id}`, requestBody);
  }

  desactivarEstadoRequisito(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/estados-requisito/${id}/desactivar`, {});
  }


  getTiposRequisito(): Observable<TipoRequisitoPostulacionDTO[]> {
    return this.http.get<TipoRequisitoPostulacionDTO[]>(`${this.API}/tipos-requisito`);
  }

  postTipoRequisito(tipo: TipoRequisitoPostulacionDTO): Observable<TipoRequisitoPostulacionDTO> {
    const { idTipoRequisitoPostulacion, activo, ...requestBody } = tipo;
    return this.http.post<TipoRequisitoPostulacionDTO>(`${this.API}/tipos-requisito`, requestBody);
  }

  putTipoRequisito(id: number, tipo: TipoRequisitoPostulacionDTO): Observable<TipoRequisitoPostulacionDTO> {
    const { idTipoRequisitoPostulacion, activo, ...requestBody } = tipo;
    return this.http.put<TipoRequisitoPostulacionDTO>(`${this.API}/tipos-requisito/${id}`, requestBody);
  }

  desactivarTipoRequisito(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/tipos-requisito/${id}/desactivar`, {});
  }


  getTiposRol(): Observable<TipoRolDTO[]> {
    return this.http.get<TipoRolDTO[]>(`${this.API}/tipos-rol`);
  }

  postTipoRol(tipo: TipoRolDTO): Observable<TipoRolDTO> {
    const { idTipoRol, activo, ...requestBody } = tipo;
    return this.http.post<TipoRolDTO>(`${this.API}/tipos-rol`, requestBody);
  }

  putTipoRol(id: number, tipo: TipoRolDTO): Observable<TipoRolDTO> {
    const { idTipoRol, activo, ...requestBody } = tipo;
    return this.http.put<TipoRolDTO>(`${this.API}/tipos-rol/${id}`, requestBody);
  }

  desactivarTipoRol(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/tipos-rol/${id}/desactivar`, {});
  }


  getTiposSancion(): Observable<TipoSancionAyudanteCatedraDTO[]> {
    return this.http.get<TipoSancionAyudanteCatedraDTO[]>(`${this.API}/tipos-sancion`);
  }

  postTipoSancion(tipo: TipoSancionAyudanteCatedraDTO): Observable<TipoSancionAyudanteCatedraDTO> {
    const { idTipoSancion, activo, ...requestBody } = tipo;
    return this.http.post<TipoSancionAyudanteCatedraDTO>(`${this.API}/tipos-sancion`, requestBody);
  }

  putTipoSancion(id: number, tipo: TipoSancionAyudanteCatedraDTO): Observable<TipoSancionAyudanteCatedraDTO> {
    const { idTipoSancion, activo, ...requestBody } = tipo;
    return this.http.put<TipoSancionAyudanteCatedraDTO>(`${this.API}/tipos-sancion/${id}`, requestBody);
  }

  desactivarTipoSancion(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/tipos-sancion/${id}/desactivar`, {});
  }

  //Servicios creados para cachear los catálogos que se usan frecuentemente en la aplicación

  getEstadosRegistro(): Observable<TipoEstadoRegistro[]> {
    if (!this.estadosRegistro$) {
      this.estadosRegistro$ = this.http
        .get<TipoEstadoRegistro[]>(`${this.API}/estados-registro`)
        .pipe(shareReplay(1));
    }
    return this.estadosRegistro$;
  }

  getEstadosEvidencia(): Observable<TipoEstadoEvidencia[]> {
    if (!this.estadosEvidencia$) {
      this.estadosEvidencia$ = this.http
        .get<TipoEstadoEvidencia[]>(`${this.API}/estados-evidencia`)
        .pipe(shareReplay(1));
    }
    return this.estadosEvidencia$;
  }

  getTiposEvidencia(): Observable<TipoEvidencia[]> {
    if (!this.tiposEvidencia$) {
      this.tiposEvidencia$ = this.http
        .get<TipoEvidencia[]>(`${this.API}/tipos-evidencia`)
        .pipe(shareReplay(1));
    }
    return this.tiposEvidencia$;
  }

  getEstadosAyudantia(): Observable<TipoEstadoAyudantia[]> {
    if (!this.estadosAyudantia$) {
      this.estadosAyudantia$ = this.http
        .get<TipoEstadoAyudantia[]>(`${this.API}/estados-ayudantia`)
        .pipe(shareReplay(1));
    }
    return this.estadosAyudantia$;
  }

  limpiarCache(): void {
    this.estadosRegistro$ = null;
    this.estadosEvidencia$ = null;
    this.tiposEvidencia$ = null;
    this.estadosAyudantia$ = null;
  }
}
