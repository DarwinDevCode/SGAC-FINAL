import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { AyudantiaService } from '../../../core/services/ayudantia/ayudantia-service';
import { GestionarParticipanteDialogComponent } from '../gestionar-participante-dialog-component/gestionar-participante-dialog-component';
import { ParticipantePadronDTO} from '../../../core/models/general/respuesta-operacion';


@Component({
  selector: 'app-padron-estudiantes',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule],
  templateUrl: './padron-estudiantes-component.html',
  styleUrls: ['./padron-estudiantes-component.css']
})
export class PadronEstudiantesComponent implements OnInit, OnDestroy {

  private ayudantiaService = inject(AyudantiaService);
  private dialog = inject(MatDialog);
  private destroy$ = new Subject<void>();

  // Estado
  cargando = signal(true);
  errorMsg = signal<string | null>(null);
  estudiantes = signal<ParticipantePadronDTO[]>([]);
  textoBusqueda = signal('');

  // Computed
  estudiantesFiltrados = computed(() => {
    const texto = this.textoBusqueda().toLowerCase().trim();
    if (!texto) return this.estudiantes();
    return this.estudiantes().filter(e =>
      e.nombreCompleto?.toLowerCase().includes(texto) ||
      e.curso?.toLowerCase().includes(texto)
    );
  });

  totalEstudiantes = computed(() => this.estudiantes().length);

  ngOnInit(): void {
    this.cargarPadron();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarPadron(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);

    // NOTA: Asegúrate de crear este método en tu AyudantiaService y su respectivo endpoint en Spring Boot
    this.ayudantiaService.obtenerPadron()
      .pipe(takeUntil(this.destroy$), finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (res: any) => {
          if (res.valido && res.datos) {
            // Ordenar alfabéticamente
            const ordenados = (res.datos as ParticipantePadronDTO[]).sort((a, b) =>
              a.nombreCompleto.localeCompare(b.nombreCompleto)
            );
            this.estudiantes.set(ordenados);
          } else {
            this.errorMsg.set(res.mensaje ?? 'No se pudo cargar el padrón.');
          }
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(err?.error?.message ?? 'Error al conectar con el servidor.');
        }
      });
  }

  abrirModalGestion(estudiante?: ParticipantePadronDTO): void {
    const dialogRef = this.dialog.open(GestionarParticipanteDialogComponent, {
      width: '500px',
      panelClass: 'sesion-dialog-container', // Reusamos tu estilo de modales
      data: estudiante // Si es undefined, el modal sabrá que es creación
    });

    dialogRef.afterClosed().subscribe((resultado: any) => {
      if (resultado) this.cargarPadron();
    });
  }

  eliminarEstudiante(estudiante: ParticipantePadronDTO): void {
    if (!confirm(`¿Estás seguro de que deseas eliminar a ${estudiante.nombreCompleto}? Se borrará también su historial de asistencia.`)) {
      return;
    }

    this.cargando.set(true);

    this.ayudantiaService.gestionarParticipante({
      accion: 'DEL',
      nombre: estudiante.nombreCompleto,
      curso: estudiante.curso,
      paralelo: estudiante.paralelo,
      idParticipante: estudiante.idParticipanteAyudantia
    })
      .pipe(takeUntil(this.destroy$), finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (res) => {
          if (res.valido) {
            this.cargarPadron(); // Recargar la lista
          } else {
            alert('Error: ' + res.mensaje);
          }
        },
        error: (err: HttpErrorResponse) => alert('Error al eliminar: ' + err.message)
      });
  }

  trackByEstudiante = (_: number, e: ParticipantePadronDTO) => e.idParticipanteAyudantia;
}
