import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CronogramaActivoResponse} from '../../models/configuracion/Cronograma';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';


@Injectable({ providedIn: 'root' })
export class CronogramaActivoService {
  private http = inject(HttpClient);
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';


  obtenerCronogramaActual(): Observable<CronogramaActivoResponse> {
    return this.http.get<CronogramaActivoResponse>(
      `${this.baseUrl}/cronograma/actual`
    );
  }
}
