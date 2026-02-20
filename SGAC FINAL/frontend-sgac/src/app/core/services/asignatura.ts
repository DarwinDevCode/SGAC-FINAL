import { Injectable } from '@angular/core';
import {AsignaturaDTO} from '../dto/Asignatura';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class AsignaturaService {
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API_RECURSOS = `${this.baseUrl}/admin/catalogos`;

  constructor(private http:HttpClient) {
  }

  getAsignaturaList(): Observable<AsignaturaDTO[]>{
    return this.http.get<AsignaturaDTO[]>(`${this.API_RECURSOS}/asignaturas`);
  }
}
