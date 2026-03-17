import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CronogramaActivoResponse} from '../../models/configuracion/Cronograma';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CronogramaActivoService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  obtenerCronogramaActual(): Observable<CronogramaActivoResponse> {
    return this.http.get<CronogramaActivoResponse>(
      `${this.baseUrl}/cronograma/actual`
    );
  }
}
