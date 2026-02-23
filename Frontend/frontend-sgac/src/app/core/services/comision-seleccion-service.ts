import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {UsuarioComisionDTO} from '../dto/usuario-comision';
import {EvaluacionMeritosDTO} from '../dto/evaluacion-meritos';
import {EvaluacionOposicionDTO} from '../dto/evaluacion-oposicion';

@Injectable({
  providedIn: 'root',
})
export class ComisionSeleccionService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly apiUrlComisiones = `${this.baseUrl}/comisiones`;
  private readonly apiUrlEvaluaciones = `${this.baseUrl}/evaluaciones`;

  listarMisAsignaciones(idUsuario: number): Observable<UsuarioComisionDTO[]> {
    return this.http.get<UsuarioComisionDTO[]>(`${this.apiUrlComisiones}/mis-asignaciones/${idUsuario}`);
  }

  registrarMeritos(evaluacion: EvaluacionMeritosDTO): Observable<any> {
    return this.http.post(`${this.apiUrlEvaluaciones}/meritos`, evaluacion);
  }

  registrarOposicion(evaluacion: UsuarioComisionDTO): Observable<any> {
    return this.http.post(`${this.apiUrlEvaluaciones}/oposicion`, evaluacion);
  }

  crearComision(request: any): Observable<any> {
    return this.http.post(`${this.apiUrlComisiones}/crear`, request);
  }

  asignarEvaluador(request: any): Observable<any> {
    return this.http.post(`${this.apiUrlComisiones}/asignar-evaluador`, request);
  }

  listarComisionPorConvocatoria(idConvocatoria: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrlComisiones}/convocatoria/${idConvocatoria}`);
  }
}
