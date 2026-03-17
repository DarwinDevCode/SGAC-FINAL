import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import { environment }        from '../../../../environments/environment';
import { FinalizarSeleccionResponse } from '../../models/convocatoria/finalizar-seleccion';
import {AuthService} from '../auth-service';
import {catchError} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class FinalizarSeleccionService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly base = `${this.baseUrl}/convocatorias`;
  private authService = inject(AuthService);

  private headers(): HttpHeaders {
    const rolActivo = this.authService.getUser()?.rolActual ?? 'COORDINADOR';
    return new HttpHeaders().set('X-Active-Role', rolActivo);
  }

  finalizarSeleccion(idConvocatoria: number): Observable<FinalizarSeleccionResponse> {
    return this.http
      .post<FinalizarSeleccionResponse>(`${this.base}/${idConvocatoria}/finalizar`, { headers: this.headers()})
        .pipe(
          catchError(err => throwError(() =>
            new Error(err.error?.mensaje ?? err.error?.message
              ?? 'Error al comunicarse con el servidor.')))

    );
  }
}
