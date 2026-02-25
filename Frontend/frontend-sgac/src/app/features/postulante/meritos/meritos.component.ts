import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulante-service';
import { EvaluacionMeritosResponseDTO } from '../../../core/dto/evaluacion';

@Component({
  selector: 'app-meritos',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './meritos.html',
  styleUrl: './meritos.css',
})
export class MeritosComponent implements OnInit, OnDestroy {
  route = inject(ActivatedRoute);
  postulanteService = inject(PostulanteService);
  private subs = new Subscription();

  idPostulacion: number | null = null;
  evaluacion: EvaluacionMeritosResponseDTO | null = null;
  loading = true;
  errorMensaje = '';

  ngOnInit(): void {
    this.subs.add(
      this.route.paramMap.subscribe(params => {
        const id = params.get('id');
        if (id) {
          this.idPostulacion = Number(id);
          this.cargarMeritos(this.idPostulacion);
        } else {
          this.errorMensaje = 'ID de postulación no proporcionado.';
          this.loading = false;
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cargarMeritos(id: number) {
    this.loading = true;
    this.subs.add(
      this.postulanteService.obtenerMeritosPorPostulacion(id).subscribe({
        next: (data) => {
          this.evaluacion = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error al cargar méritos:', err);
          if (err.status === 404) {
            this.errorMensaje = 'Aún no se ha registrado la calificación de méritos para esta postulación.';
          } else {
            this.errorMensaje = 'Error al obtener la información de méritos.';
          }
          this.loading = false;
        }
      })
    );
  }

  getTotal(): number {
    if (!this.evaluacion) return 0;
    return Number(this.evaluacion.notaAsignatura || 0) +
      Number(this.evaluacion.notaSemestres || 0) +
      Number(this.evaluacion.notaEventos || 0) +
      Number(this.evaluacion.notaExperiencia || 0);
  }
}
