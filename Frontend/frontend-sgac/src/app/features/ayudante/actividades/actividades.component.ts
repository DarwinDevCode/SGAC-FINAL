import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription, switchMap, catchError, of, forkJoin } from 'rxjs';
import { AyudanteService } from '../../../core/services/ayudante-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulanteService } from '../../../core/services/postulante-service';
import { HttpClient } from '@angular/common/http';
import {
  AyudantiaResponseDTO,
  RegistroActividadResponseDTO,
  RegistroActividadRequestDTO
} from '../../../core/dto/ayudante';

@Component({
  selector: 'app-ayudante-actividades',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './actividades.html',
  styleUrl: './actividades.css',
})
export class ActividadesComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private ayudanteService = inject(AyudanteService);
  private authService = inject(AuthService);
  private postulanteService = inject(PostulanteService);
  private http = inject(HttpClient);
  private subs = new Subscription();

  ayudantia: AyudantiaResponseDTO | null = null;
  actividades: RegistroActividadResponseDTO[] = [];

  loading = true;
  loadingForm = false;
  errorMensaje = '';
  successMensaje = '';

  actividadForm = this.fb.group({
    temaTratado: ['', [Validators.required, Validators.minLength(5)]],
    descripcionActividad: ['', [Validators.required, Validators.minLength(10)]],
    fecha: [new Date().toISOString().split('T')[0], [Validators.required]],
    horasDedicadas: [1, [Validators.required, Validators.min(0.5), Validators.max(8)]],
    numeroAsistentes: [0, [Validators.min(0)]],
  });

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cargarDatos() {
    this.loading = true;
    const user = this.authService.getUser();
    if (!user) {
      this.errorMensaje = 'Sesión no válida.';
      this.loading = false;
      return;
    }

    // Process: Get Estudiante -> Get Accepted Postulation -> Get Ayudantia -> Load Activities
    this.subs.add(
      this.http.get<any>(`http://localhost:8080/api/estudiantes/usuario/${user.idUsuario}`).pipe(
        switchMap(estudiante => {
          return this.postulanteService.misPostulaciones(estudiante.idEstudiante);
        }),
        switchMap(postulaciones => {
          const aceptada = postulaciones.find(p => p.estadoPostulacion === 'APROBADO' || p.estadoPostulacion === 'ACEPTADO');
          if (!aceptada) throw new Error('No se encontró una postulación aprobada para este ciclo.');
          return this.ayudanteService.obtenerAyudantiaPorPostulacion(aceptada.idPostulacion);
        }),
        switchMap(ayudantia => {
          this.ayudantia = ayudantia;
          return this.ayudanteService.listarActividades(ayudantia.idAyudantia);
        }),
        catchError(err => {
          this.errorMensaje = err.message || 'No se pudo cargar la información de ayudantía.';
          return of([]);
        })
      ).subscribe({
        next: (lista) => {
          if (Array.isArray(lista)) {
            this.actividades = lista;
          }
          this.loading = false;
        }
      })
    );
  }

  onSubmit() {
    if (this.actividadForm.invalid || !this.ayudantia) return;

    this.loadingForm = true;
    const values = this.actividadForm.value;

    const request: RegistroActividadRequestDTO = {
      idAyudantia: this.ayudantia.idAyudantia,
      temaTratado: values.temaTratado!,
      descripcionActividad: values.descripcionActividad!,
      fecha: values.fecha!,
      horasDedicadas: Number(values.horasDedicadas),
      numeroAsistentes: Number(values.numeroAsistentes || 0)
    };

    this.ayudanteService.registrarActividad(request).subscribe({
      next: (nueva) => {
        this.actividades.unshift(nueva);
        this.successMensaje = 'Actividad registrada correctamente.';
        this.actividadForm.reset({
          fecha: new Date().toISOString().split('T')[0],
          horasDedicadas: 1,
          numeroAsistentes: 0
        });
        this.loadingForm = false;
        setTimeout(() => this.successMensaje = '', 3000);
      },
      error: () => {
        this.errorMensaje = 'Error al guardar. Verifica la conexión.';
        this.loadingForm = false;
        setTimeout(() => this.errorMensaje = '', 4000);
      }
    });
  }

  eliminar(id: number) {
    if (confirm('¿Estás seguro de eliminar este registro?')) {
      this.ayudanteService.eliminarActividad(id).subscribe({
        next: () => {
          this.actividades = this.actividades.filter(a => a.idRegistroActividad !== id);
        }
      });
    }
  }
}
