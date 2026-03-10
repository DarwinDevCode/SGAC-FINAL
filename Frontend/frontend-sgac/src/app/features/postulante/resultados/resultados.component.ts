import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulacionResponseDTO } from '../../../core/dto/postulacion';

@Component({
  selector: 'app-resultados',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './resultados.html',
  styleUrl: './resultados.css',
})
export class ResultadosComponent implements OnInit, OnDestroy {
  postulanteService = inject(PostulanteService);
  authService = inject(AuthService);
  private http = inject(HttpClient);
  private subs = new Subscription();

  private readonly BASE = 'http://localhost:8080/api';

  postulaciones: (PostulacionResponseDTO & {
    resultado?: any;       // { totalMeritos, promedioOposicion, totalFinal, estado, posicion, aprobado }
    cargandoResult?: boolean;
  })[] = [];

  loading = true;
  idEstudianteBase = 0;

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) { this.idEstudianteBase = user.idUsuario; }
    this.cargarResultados();
  }

  ngOnDestroy(): void { this.subs.unsubscribe(); }

  cargarResultados(): void {
    this.loading = true;
    this.subs.add(
      this.postulanteService.misPostulaciones(this.idEstudianteBase).subscribe({
        next: (postulaciones) => {
          this.postulaciones = postulaciones || [];
          this.postulaciones.forEach(p => this.cargarResultadoPostulacion(p));
          if (this.postulaciones.length === 0) this.loading = false;
        },
        error: () => { this.loading = false; }
      })
    );
  }

  private cargarResultadoPostulacion(p: any): void {
    p.cargandoResult = true;
    this.http.get<any>(`${this.BASE}/evaluaciones/resultado-postulante/${p.idPostulacion}`)
      .subscribe({
        next: (res) => {
          p.resultado = res;
          p.cargandoResult = false;
          this.checkAllLoaded();
        },
        error: () => {
          p.resultado = null;
          p.cargandoResult = false;
          this.checkAllLoaded();
        }
      });
  }

  private checkAllLoaded(): void {
    const allDone = this.postulaciones.every((p: any) => !p.cargandoResult);
    if (allDone) this.loading = false;
  }

  /** Etapa del stepper según estado de la postulación + existencia de resultado */
  getPhase(p: any): number {
    const estado = (p.estadoPostulacion || '').toUpperCase();
    if (p.resultado) return 4;                                                          // Resultado publicado
    if (estado === 'OPOSICION_EVALUADA') return 3;
    if (estado === 'EN_EVALUACION' || estado === 'MERITOS_EVALUADOS') return 2;
    return 1;
  }

  /** CSS para el badge de estado del resultado */
  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'GANADOR': return 'resultado-ganador';
      case 'APTO': return 'resultado-apto';
      case 'NO_APTO': return 'resultado-no-apto';
      case 'DESIERTO': return 'resultado-desierto';
      default: return 'resultado-pendiente';
    }
  }

  getEstadoLabel(estado: string): string {
    switch (estado) {
      case 'GANADOR': return '🎉 Seleccionado como Ayudante';
      case 'APTO': return '✅ Aprobado';
      case 'NO_APTO': return '❌ No alcanzó el puntaje mínimo';
      case 'DESIERTO': return '⚠️ Concurso Desierto';
      default: return estado;
    }
  }
}
