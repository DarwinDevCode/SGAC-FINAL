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

  private headers(): HttpHeaders {
    const rolActivo = this.authService.getUser()?.rolActual ?? 'ESTUDIANTE';
    return new HttpHeaders().set('X-Active-Role', rolActivo);
  }

  obtenerResultados(): Observable<RankingResponse> {
    console.log(`Obteniendo resultados con rol: ${this.headers()}`);


    return this.http
      .get<RankingResponse>(`${this.API}/resultados`, { headers: this.headers() })
      .pipe(
        catchError(err => throwError(() =>
          new Error(err.error?.mensaje ?? err.error?.message
            ?? 'Error al comunicarse con el servidor.')))
      );
  }

  exportarExcel(): Observable<Blob> {
    return this.http
      .get(`${this.API}/exportar/excel`, {
        headers:      this.headers(),
        responseType: 'blob',
      })
      .pipe(
        catchError(err => throwError(() =>
          new Error(err.error?.message ?? 'Error al generar el archivo Excel.')))
      );
  }

  exportarPdf(): Observable<Blob> {
    return this.http
      .get(`${this.API}/exportar/pdf`, {
        headers:      this.headers(),
        responseType: 'blob',
      })
      .pipe(
        catchError(err => throwError(() =>
          new Error(err.error?.message ?? 'Error al generar el archivo PDF.')))
      );
  }
}
