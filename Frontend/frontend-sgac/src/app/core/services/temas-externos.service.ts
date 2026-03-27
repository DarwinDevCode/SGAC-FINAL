import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MateriaDTO, TemaDTO } from '../models/temas/temas';

@Injectable({
  providedIn: 'root'
})
export class TemasExternosService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api/temas';

  getMaterias(): Observable<MateriaDTO[]> {
    return this.http.get<MateriaDTO[]>(`${this.apiUrl}/materias`);
  }

  getTemasPorMateria(idMateria: number): Observable<TemaDTO[]> {
    return this.http.get<TemaDTO[]>(`${this.apiUrl}/temas/${idMateria}`);
  }
}
