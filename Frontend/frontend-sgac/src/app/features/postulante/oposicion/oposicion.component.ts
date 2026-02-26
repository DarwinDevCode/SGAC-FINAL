import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulante-service';
import { EvaluacionOposicionResponseDTO } from '../../../core/dto/evaluacion';

@Component({
  selector: 'app-oposicion',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './oposicion.html',
  styleUrl: './oposicion.css', // Reutilizamos estilos similares
})
export class OposicionComponent implements OnInit, OnDestroy {
  route = inject(ActivatedRoute);
  postulanteService = inject(PostulanteService);
  private subs = new Subscription();

  idPostulacion: number | null = null;
  evaluacion: EvaluacionOposicionResponseDTO | null = null;
  loading = true;
  errorMensaje = '';

  ngOnInit(): void {
    this.subs.add(
      this.route.paramMap.subscribe(params => {
        const id = params.get('id');
        if (id) {
          this.idPostulacion = Number(id);
          this.cargarOposicion(this.idPostulacion);
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

  cargarOposicion(id: number) {
    this.loading = true;
    this.subs.add(
      this.postulanteService.obtenerOposicionPorPostulacion(id).subscribe({
        next: (data) => {
          this.evaluacion = data;
          this.loading = false;
        },
        error: (err) => {
          console.error(err.error?.data?.message || err.error?.message || err.message || 'Error al cargar oposición:');
          if (err.status === 404) {
            this.errorMensaje = 'Aún no te han asignado una fecha para la evaluación de oposición, o no la has rendido.';
          } else {
            this.errorMensaje = 'Error al obtener los datos de la fase de oposición.';
          }
          this.loading = false;
        }
      })
    );
  }
}
