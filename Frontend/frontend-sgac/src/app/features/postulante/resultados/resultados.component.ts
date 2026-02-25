import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription, forkJoin } from 'rxjs';
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
  private subs = new Subscription();

  // Extendemos la interfaz localmente para añadir datos de evaluación a la vista
  postulaciones: (PostulacionResponseDTO & {
    notaFinal?: number,
    evaluacionCompletada?: boolean
  })[] = [];

  loading = true;
  idEstudianteBase = 0;

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.idEstudianteBase = user.idUsuario;
    }
    this.cargarResultados();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cargarResultados() {
    this.loading = true;
    this.subs.add(
      this.postulanteService.misPostulaciones(this.idEstudianteBase).subscribe({
        next: (postulaciones) => {
          this.postulaciones = postulaciones || [];
          this.cargarPuntajesTotales();
        },
        error: (err) => {
          console.error('Error al cargar resultados:', err);
          this.loading = false;
        }
      })
    );
  }

  cargarPuntajesTotales() {
    if (this.postulaciones.length === 0) {
      this.loading = false;
      return;
    }

    // Para cada postulación intentamos traer sus méritos y OP (si existen)
    let peticionesTerminadas = 0;

    this.postulaciones.forEach(post => {
      const postUI = post as any;
      forkJoin({
        meritos: this.postulanteService.obtenerMeritosPorPostulacion(post.idPostulacion),
        oposicion: this.postulanteService.obtenerOposicionPorPostulacion(post.idPostulacion)
      }).subscribe({
        next: (result) => {
          // Sumamos si existen (según lógica de negocio simple, ajusta de ser necesario)
          let notaM = 0;
          if (result.meritos) {
            notaM = Number(result.meritos.notaAsignatura || 0) +
              Number(result.meritos.notaEventos || 0) +
              Number(result.meritos.notaExperiencia || 0) +
              Number(result.meritos.notaSemestres || 0);
          }
          // La oposición a veces puede no ser una nota sino un estado en este backend, 
          // pero si hubiera nota se sumaría. Aquí marcamos como completada si tiene M y OP

          postUI.notaFinal = notaM;
          postUI.evaluacionCompletada = !!(result.meritos && result.oposicion);

          peticionesTerminadas++;
          if (peticionesTerminadas === this.postulaciones.length) this.loading = false;
        },
        error: () => {
          // Si da 404 (no evaluado aún)
          postUI.evaluacionCompletada = false;
          peticionesTerminadas++;
          if (peticionesTerminadas === this.postulaciones.length) this.loading = false;
        }
      });
    });
  }
}
