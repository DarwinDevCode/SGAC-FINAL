import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError }         from 'rxjs/operators';
import { environment }        from '../../../../environments/environment';
import { RankingResponse }    from '../../models/resultados/Ranking';
import {AuthService} from '../auth-service';

@Injectable({ providedIn: 'root' })
export class RankingService {
  private http = inject(HttpClient);
  private readonly API = `${(environment as any).apiUrl ?? 'http://localhost:8080/api'}/ranking`;
  private authService = inject(AuthService);


  obtenerResultados(): Observable<RankingResponse> {
    const rolActivo = this.authService.getUser()?.rolActual || 'ESTUDIANTE';
    const headers = new HttpHeaders().set('X-Active-Role', rolActivo);

    return this.http
      .get<RankingResponse>(`${this.API}/resultados`, { headers })
      .pipe(catchError(err => {
        const msg = err.error?.mensaje ?? err.error?.message
          ?? 'Error al comunicarse con el servidor.';
        return throwError(() => new Error(msg));
      }));
  }
}
