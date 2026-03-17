import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth-service';
import { environment } from '../../../../environments/environment';
import { finalize } from 'rxjs';

export interface HistorialAyudantia {
  idAyudantia: number;
  nombreEstudiante: string;
  nombreAsignatura: string;
  codigoAsignatura: string;
  nombrePeriodo: string;
  inicioPeriodo: string;
  finPeriodo: string;
  fechaInicio: string;
  fechaFin: string;
  horasCumplidas: number;
  totalSesiones: number;
  resultadoFinal: string;
  estadoAyudantia: string;
}

@Component({
  selector: 'app-historial-ayudante',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './historial.html',
  styleUrls: ['./historial.css']
})
export class HistorialAyudanteComponent implements OnInit {
  private auth = inject(AuthService);
  private http = inject(HttpClient);
  private apiUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

  historial: HistorialAyudantia[] = [];
  isLoading = false;
  errorMessage = '';

  ngOnInit() {
    this.cargarHistorial();
  }

  cargarHistorial() {
    const userId = this.auth.getUser()?.idUsuario;
    if (!userId) return;
    this.isLoading = true;
    this.errorMessage = '';
    this.http.get<HistorialAyudantia[]>(`${this.apiUrl}/ayudantias/historial/${userId}`)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (data) => { this.historial = data ?? []; },
        error: () => { this.errorMessage = 'No se pudo cargar el historial de ayudantías.'; }
      });
  }

  get horasTotales(): number {
    return this.historial.reduce((acc, h) => acc + (h.horasCumplidas || 0), 0);
  }

  get sesionesTotales(): number {
    return this.historial.reduce((acc, h) => acc + (h.totalSesiones || 0), 0);
  }

  get aprobadas(): number {
    return this.historial.filter(h => (h.resultadoFinal || '').toUpperCase() === 'APROBADO').length;
  }

  getResultadoClass(resultado: string): string {
    const r = (resultado || '').toUpperCase();
    if (r === 'APROBADO') return 'badge-aprobado';
    if (r === 'REPROBADO') return 'badge-reprobado';
    return 'badge-en-curso';
  }

  getEstadoClass(estado: string): string {
    const e = (estado || '').toUpperCase();
    if (e === 'ACTIVA') return 'badge-activo';
    return 'badge-default';
  }

  formatDate(date: string): string {
    if (!date) return '—';
    return new Date(date).toLocaleDateString('es-EC', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
