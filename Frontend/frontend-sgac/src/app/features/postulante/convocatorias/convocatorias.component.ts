import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulaciones/postulante-service';
import { ConvocatoriaService } from '../../../core/services/convocatoria-service';
import { AuthService } from '../../../core/services/auth-service';
import { TipoRequisitoPostulacionResponseDTO } from '../../../core/dto/postulacion';
import { ConvocatoriaEstudianteDTO } from '../../../core/dto/convocatoria-estudiante';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-convocatorias',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './convocatorias.html',
  styleUrl: './convocatorias.css',
})
export class ConvocatoriasComponent implements OnInit, OnDestroy {
  postulanteService = inject(PostulanteService);
  convocatoriaService = inject(ConvocatoriaService);
  authService = inject(AuthService);
  private subs = new Subscription();

  convocatoriasList: ConvocatoriaEstudianteDTO[] = [];
  convocatoriasFiltradas: ConvocatoriaEstudianteDTO[] = [];
  tiposRequisito: TipoRequisitoPostulacionResponseDTO[] = [];

  loading = true;
  mostrarModal = false;
  busqueda = '';
  convocatoriaSeleccionada: ConvocatoriaEstudianteDTO | null = null;

  /** Mapa: idConvocatoria → true si el estudiante ya se postuló a esa convocatoria */
  postulacionPorConvocatoria: Record<number, boolean> = {};

  mensajeError: string | null = null;
  mensajeInfo: string | null = null;
  esElegible = true;

  idEstudianteBase = 0;
  archivosSubidos: { idRequisito: number; file: File }[] = [];
  observacionesGenerales = '';
  observacionesPorRequisito: { [idRequisito: number]: string } = {};

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.idEstudianteBase = user.idUsuario;
    }
    this.listarConvocatoriasElegibles();
    this.cargarRequisitos();
  }

  checkPostulacionesGranulares() {
    if (!this.idEstudianteBase || this.convocatoriasFiltradas.length === 0) return;
    this.postulacionPorConvocatoria = {};

    this.convocatoriasFiltradas.forEach((conv) => {
      if (!conv.idConvocatoria) return;

      this.subs.add(
        this.postulanteService
          .existePostulacion(this.idEstudianteBase, conv.idConvocatoria)
          .subscribe({
            next: (existe: boolean) => {
              this.postulacionPorConvocatoria[conv.idConvocatoria!] = existe;
            },
            error: () => {
              this.postulacionPorConvocatoria[conv.idConvocatoria!] = false;
            },
          })
      );
    });
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  listarConvocatoriasElegibles() {
    this.loading = true;
    this.mensajeError = null;
    this.mensajeInfo = null;
    this.esElegible = true;

    this.subs.add(
      this.convocatoriaService
        .listarConvocatoriasElegibles(this.idEstudianteBase)
        .subscribe({
          next: (convocatorias: ConvocatoriaEstudianteDTO[]) => {
            this.convocatoriasList = convocatorias || [];
            this.aplicarFiltro(this.busqueda);

            if (this.convocatoriasList.length === 0) {
              this.mensajeInfo =
                'No hay convocatorias disponibles para tu carrera y nivel académico en este momento.';
            }

            this.checkPostulacionesGranulares();
            this.loading = false;
          },
          error: (err: HttpErrorResponse) => {
            console.error('Error al cargar convocatorias:', err);

            const errorMsg = err.error?.message || err.error || err.message;
            if (
              errorMsg &&
              (errorMsg.includes('Requisito no cumplido') ||
                errorMsg.includes('Acceso denegado'))
            ) {
              this.esElegible = false;
              this.mensajeError = errorMsg;
            } else {
              this.mensajeError =
                'Error al cargar las convocatorias. Por favor, intenta de nuevo.';
            }

            this.convocatoriasList = [];
            this.convocatoriasFiltradas = [];
            this.loading = false;
          },
        })
    );
  }

  cargarRequisitos() {
    this.subs.add(
      this.postulanteService.listarTiposRequisitos().subscribe({
        next: (data) => (this.tiposRequisito = data || []),
        error: (err) => console.error('Error al cargar requisitos', err),
      })
    );
  }

  filtrarConvocatorias(texto: string) {
    this.busqueda = texto;
    this.aplicarFiltro(texto);
  }

  private aplicarFiltro(texto: string) {
    const term = (texto || '').toLowerCase().trim();

    if (!term) {
      this.convocatoriasFiltradas = [...this.convocatoriasList];
      this.checkPostulacionesGranulares();
      return;
    }

    this.convocatoriasFiltradas = this.convocatoriasList.filter(
      (c) =>
        (c.nombreAsignatura || '').toLowerCase().includes(term) ||
        (c.nombreCarrera || '').toLowerCase().includes(term) ||
        (c.nombreDocente || '').toLowerCase().includes(term)
    );

    this.checkPostulacionesGranulares();
  }

  abrirModalPostulacion(convocatoria: ConvocatoriaEstudianteDTO) {
    this.convocatoriaSeleccionada = convocatoria;
    this.archivosSubidos = [];
    this.observacionesGenerales = '';
    this.observacionesPorRequisito = {};
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.convocatoriaSeleccionada = null;
  }

  onFileChange(event: any, idRequisito: number) {
    const file = event.target.files[0];
    if (file) {
      const index = this.archivosSubidos.findIndex(
        (a) => a.idRequisito === idRequisito
      );

      if (index !== -1) {
        this.archivosSubidos[index].file = file;
      } else {
        this.archivosSubidos.push({ idRequisito, file });
      }
    }
  }

  enviarPostulacion() {
    if (!this.convocatoriaSeleccionada || !this.convocatoriaSeleccionada.idConvocatoria) return;

    if (this.archivosSubidos.length !== this.tiposRequisito.length) {
      alert('Debe subir un archivo por cada requisito obligatorio.');
      return;
    }

    const idsRequisitos = this.archivosSubidos.map((a) => a.idRequisito);
    const files = this.archivosSubidos.map((a) => a.file);

    const datosFormulario = {
      idConvocatoria: this.convocatoriaSeleccionada.idConvocatoria,
      idEstudiante: this.idEstudianteBase,
      observaciones: this.observacionesGenerales,
      idTipoEstado: 1,
    };

    this.subs.add(
      this.postulanteService
        .registrarPostulacion(datosFormulario, files, idsRequisitos)
        .subscribe({
          next: () => {
            alert('Postulación registrada exitosamente');
            this.cerrarModal();
            this.listarConvocatoriasElegibles();
          },
          error: (err: HttpErrorResponse) => {
            console.error('Error al postular:', err);
            const msg =
              typeof err.error === 'string'
                ? err.error
                : err.message || 'Error al enviar la postulación';
            alert(msg);
          },
        })
    );
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return 'N/A';

    try {
      const date = new Date(fecha);
      return date.toLocaleDateString('es-EC', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });
    } catch {
      return fecha;
    }
  }

  getEstadoVisual(conv: ConvocatoriaEstudianteDTO): 'PENDIENTE' | 'ABIERTA' | 'FINALIZADA' {
    const estado = (conv.estadoConvocatoria || '').toUpperCase().trim();

    if (estado === 'FINALIZADA') {
      return 'FINALIZADA';
    }

    if (conv.puedePostular) {
      return 'ABIERTA';
    }

    return 'PENDIENTE';
  }

  getTextoBoton(conv: ConvocatoriaEstudianteDTO): string {
    const estadoVisual = this.getEstadoVisual(conv);

    if (estadoVisual === 'PENDIENTE') return 'Pendiente';
    if (estadoVisual === 'FINALIZADA') return 'Cerrada';
    if (conv.cuposDisponibles === 0) return 'Sin Cupos';
    return 'Postular';
  }

  getIconoBoton(conv: ConvocatoriaEstudianteDTO): string {
    const estadoVisual = this.getEstadoVisual(conv);

    if (estadoVisual === 'PENDIENTE') return 'clock';
    if (estadoVisual === 'FINALIZADA') return 'lock';
    return 'send';
  }

  getTituloBoton(conv: ConvocatoriaEstudianteDTO): string {
    const estadoVisual = this.getEstadoVisual(conv);

    if (estadoVisual === 'PENDIENTE') {
      return 'La convocatoria aún no está habilitada para postulación';
    }

    if (estadoVisual === 'FINALIZADA') {
      return 'El periodo de postulación ha finalizado';
    }

    if (conv.cuposDisponibles === 0) {
      return 'No hay cupos disponibles';
    }

    return 'Postular a la convocatoria';
  }
}
