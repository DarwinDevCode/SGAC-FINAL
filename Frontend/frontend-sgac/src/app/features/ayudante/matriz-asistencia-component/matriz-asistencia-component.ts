import {
  Component, OnInit, OnDestroy, inject, signal, computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router }       from '@angular/router';
import { FormsModule }  from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';

import { AsistenciaService } from '../../../core/services/asistencia-service';
import {
  MatrizAsistencia,
  SesionMatriz,
  EstudianteMatriz,
} from '../../../core/models/Asistencia';

@Component({
  selector: 'app-matriz-asistencia',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './matriz-asistencia-component.html',
  styleUrl:    './matriz-asistencia-component.css',
})
export class MatrizAsistenciaComponent implements OnInit, OnDestroy {

  private router   = inject(Router);
  private svc      = inject(AsistenciaService);
  private destroy$ = new Subject<void>();

  cargando = signal(true);
  errorMsg = signal<string | null>(null);
  sesiones    = signal<SesionMatriz[]>([]);
  estudiantes = signal<EstudianteMatriz[]>([]);
  textoBusqueda = signal('');

  estudiantesFiltrados = computed(() => {
    const texto = this.textoBusqueda().toLowerCase().trim();
    if (!texto) return this.estudiantes();
    return this.estudiantes().filter(e =>
      e.nombre.toLowerCase().includes(texto) ||
      e.curso.toLowerCase().includes(texto)
    );
  });

  totalSesiones = computed(() => this.sesiones().length);
  totalEstudiantes = computed(() => this.estudiantes().length);

  porcentajeGlobal = computed(() => {
    const ests = this.estudiantes();
    const sess = this.sesiones();
    if (!ests.length || !sess.length) return 0;

    let presentes = 0;
    let totales   = 0;

    for (const est of ests) {
      for (const ses of sess) {
        const val = est.asistencias?.[ses.id.toString()];
        if (val !== null && val !== undefined) {
          totales++;
          if (val === true) presentes++;
        }
      }
    }
    return totales > 0 ? Math.round((presentes / totales) * 100) : 0;
  });

  ngOnInit(): void {
    this.cargarMatriz();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarMatriz(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);

    this.svc.obtenerMatriz()
      .pipe(takeUntil(this.destroy$), finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (data: MatrizAsistencia) => {
          this.sesiones.set(data?.sesiones    ?? []);
          this.estudiantes.set(data?.estudiantes ?? []);
        },
        error: (err) => {
          this.errorMsg.set(
            err?.error?.message ?? 'No se pudo cargar la matriz de asistencia.'
          );
        },
      });
  }

  estadoCelda(est: EstudianteMatriz, sesionId: number): 'presente' | 'ausente' | 'noRegistrado' {
    const val = est.asistencias?.[sesionId.toString()];
    if (val === true)  return 'presente';
    if (val === false) return 'ausente';
    return 'noRegistrado'; // null o undefined
  }

  sesionesPresente(est: EstudianteMatriz): number {
    return this.sesiones().filter(s =>
      est.asistencias?.[s.id.toString()] === true
    ).length;
  }

  sesionesRegistradas(est: EstudianteMatriz): number {
    return this.sesiones().filter(s => {
      const val = est.asistencias?.[s.id.toString()];
      return val !== null && val !== undefined;
    }).length;
  }

  porcentajeEstudiante(est: EstudianteMatriz): number {
    const reg = this.sesionesRegistradas(est);
    return reg > 0
      ? Math.round((this.sesionesPresente(est) / reg) * 100)
      : 0;
  }

  colorBarra(pct: number): string {
    if (pct >= 75) return 'mz-bar-verde';
    if (pct >= 50) return 'mz-bar-amarillo';
    return 'mz-bar-rojo';
  }

  porcentajeSesion(sesionId: number): number {
    const key  = sesionId.toString();
    const ests = this.estudiantes();
    const registrados = ests.filter(e => {
      const val = e.asistencias?.[key];
      return val !== null && val !== undefined;
    });
    if (!registrados.length) return 0;
    const presentes = registrados.filter(e => e.asistencias?.[key] === true).length;
    return Math.round((presentes / registrados.length) * 100);
  }

  formatearFecha(iso: string): string {
    if (!iso) return '—';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }

  abreviarTema(tema: string, max = 22): string {
    if (!tema) return '—';
    return tema.length > max ? tema.slice(0, max) + '…' : tema;
  }

  volver(): void { this.router.navigate(['/ayudante/asistencia']); }
  irASesiones(): void { this.router.navigate(['/ayudante/sesiones']); }

  trackBySesion    = (_: number, s: SesionMatriz)     => s.id;
  trackByEstudiante = (_: number, e: EstudianteMatriz) => e.idParticipante;
}
