import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {PostulacionDTO} from '../../../../core/dto/postulacion';
import {UsuarioDTO} from '../../../../core/dto/usuario';
import {PostulacionService} from '../../../../core/services/postulacion-service';
import {UsuarioService} from '../../../../core/services/usuario-service';
import {EvaluacionOposicionService} from '../../../../core/services/evaluacion-oposicion-service';
import {EvaluacionMeritosService} from '../../../../core/services/evaluacion-meritos-service';
import {ComisionSeleccionService} from '../../../../core/services/comision-seleccion-service';
import {EvaluacionMeritosDTO} from '../../../../core/dto/evaluacion-meritos';
import {EvaluacionOposicionDTO} from '../../../../core/dto/evaluacion-oposicion';

@Component({
  selector: 'app-verificar-postulaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verificar-postulaciones.html',
  styleUrl: './verificar-postulaciones.css'
})

export class VerificarPostulacionesComponent implements OnInit {
  postulantes = signal<PostulacionDTO[]>([]);
  idConvocatoria: number = 0;

  docentes: UsuarioDTO[] = [];
  miembrosComision: any[] = [];
  showComisionModal = signal(false);
  selectedDocenteId: number | null = null;
  currentComisionId: number | null = null;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private postulacionService = inject(PostulacionService);
  private comisionService = inject(ComisionSeleccionService);
  private usuarioService = inject(UsuarioService);
  private evaluacionOposicionService = inject(EvaluacionOposicionService);
  private evaluacionMeritosService = inject(EvaluacionMeritosService);


  showCalificacionesModal = signal(false);
  selectedPostulacionForGrades: PostulacionDTO | null = null;
  evaluacionMeritos: EvaluacionMeritosDTO | null = null;
  evaluacionOposicion: EvaluacionOposicionDTO | null = null;

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.idConvocatoria = Number(params.get('id'));
      if (this.idConvocatoria) {
        this.cargarPostulantes();
        this.cargarDatosComision();
      }
    });
  }

  cargarPostulantes() {
    this.postulacionService.listarPorConvocatoria(this.idConvocatoria).subscribe({
      next: (data: PostulacionDTO[]) => this.postulantes.set(data),
      error: (err: any) => console.error(err)
    });
  }

  cargarDatosComision() {
    this.usuarioService.listarUsuarios().subscribe((users: UsuarioDTO[]) => {
      this.docentes = users.filter((u: UsuarioDTO) => u.roles?.some((r: any) => r.nombreTipoRol === 'DOCENTE'));
    });

    this.comisionService.listarComisionPorConvocatoria(this.idConvocatoria).subscribe((comisiones: any[]) => {
      if (comisiones && comisiones.length > 0) {
        this.currentComisionId = comisiones[0].idComisionSeleccion;
      }
    });
  }

  abrirModalComision() {
    this.showComisionModal.set(true);
  }

  cerrarModalComision() {
    this.showComisionModal.set(false);
    this.selectedDocenteId = null;
  }

  asignarDocente() {
    if (!this.selectedDocenteId) return;

    if (!this.currentComisionId) {
      const nuevaComision = {
        idConvocatoria: this.idConvocatoria,
        nombreComision: 'Comisión Convocatoria ' + this.idConvocatoria,
        fechaConformacion: new Date(),
        activo: true
      };
      this.comisionService.crearComision(nuevaComision).subscribe({
        next: (comision: any) => {
          this.currentComisionId = comision.idComisionSeleccion;
          this.agregarMiembro(this.currentComisionId!, this.selectedDocenteId!);
        },
        error: (err: any) => alert('Error al crear comisión: ' + err.message)
      });
    } else {
      this.agregarMiembro(this.currentComisionId, this.selectedDocenteId);
    }
  }

  agregarMiembro(idComision: number, idUsuario: number) {
    const evaluador = {
      idComisionSeleccion: idComision,
      idUsuario: idUsuario,
      rolIntegrante: 'MIEMBRO',
      idEvaluacionOposicion: 0
    };

    this.comisionService.asignarEvaluador(evaluador).subscribe({
      next: () => {
        alert('Docente asignado correctamente');
        this.cerrarModalComision();
      },
      error: (err: any) => alert('Error al asignar docente: ' + err.message)
    });
  }

  abrirModalCalificaciones(postulacion: PostulacionDTO) {
    this.selectedPostulacionForGrades = postulacion;
    this.showCalificacionesModal.set(true);
    this.evaluacionMeritos = null;
    this.evaluacionOposicion = null;

    this.evaluacionMeritosService.obtenerMeritosPorPostulacion(postulacion.idPostulacion).subscribe({
      next: (data: EvaluacionMeritosDTO) => this.evaluacionMeritos = data,
      error: (err: any) => console.log('Sin méritos registrados')
    });

    this.evaluacionOposicionService.obtenerOposicionPorPostulacion(postulacion.idPostulacion).subscribe({
      next: (data: EvaluacionOposicionDTO) => this.evaluacionOposicion = data,
      error: (err: any) => console.log('Sin oposición registrada')
    });
  }

  cerrarModalCalificaciones() {
    this.showCalificacionesModal.set(false);
    this.selectedPostulacionForGrades = null;
  }

  verDetalle(postulacion: PostulacionDTO) {
    alert(`Ver detalle de: ${postulacion.nombreCompletoEstudiante}`);
  }

  volver() {
    this.router.navigate(['../dashboard'], { relativeTo: this.route });
  }
}
