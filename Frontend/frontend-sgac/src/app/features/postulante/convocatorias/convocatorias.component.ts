import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';
import { ConvocatoriaDTO } from '../../../core/dto/convocatoria';
import { TipoRequisitoPostulacionResponseDTO } from '../../../core/dto/postulacion';
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
  authService = inject(AuthService);
  private subs = new Subscription();

  // Data
  convocatoriasList: ConvocatoriaDTO[] = [];
  convocatoriasFiltradas: ConvocatoriaDTO[] = [];
  tiposRequisito: TipoRequisitoPostulacionResponseDTO[] = [];

  // UI State
  loading = true;
  mostrarModal = false;
  busqueda = '';
  convocatoriaSeleccionada: ConvocatoriaDTO | null = null;
  yaPostulado = false;

  // Form State
  idEstudianteBase = 0;
  archivosSubidos: { idRequisito: number, file: File }[] = [];
  observacionesGenerales = '';
  observacionesPorRequisito: { [idRequisito: number]: string } = {};

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.idEstudianteBase = user.idUsuario;
      this.checkPostulacionActiva();
    }
    this.listarConvocatorias();
    this.cargarRequisitos();
  }

  checkPostulacionActiva() {
    this.subs.add(
      this.postulanteService.misPostulaciones(this.idEstudianteBase).subscribe({
        next: (postulaciones) => {
          // Si tiene al menos una postulación en estado que no sea RECHAZADO, se considera postulado
          const activas = (postulaciones || []).filter(p => p.estadoPostulacion !== 'RECHAZADO');
          this.yaPostulado = activas.length > 0;
        },
        error: (err) => console.error(err.error?.message || err.message || 'Error al verificar postulaciones activas')
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  listarConvocatorias() {
    this.loading = true;
    this.subs.add(
      this.postulanteService.listarConvocatoriasActivas().subscribe({
        next: (data) => {
          this.convocatoriasList = data || [];
          this.aplicarFiltro(this.busqueda);
          this.loading = false;
        },
        error: () => this.loading = false
      })
    );
  }

  cargarRequisitos() {
    this.subs.add(
      this.postulanteService.listarTiposRequisitos().subscribe({
        next: (data) => this.tiposRequisito = data || [],
        error: (err) => console.error('Error al cargar requisitos', err)
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
      return;
    }

    this.convocatoriasFiltradas = this.convocatoriasList.filter(c =>
      (c.nombreAsignatura || '').toLowerCase().includes(term) ||
      (c.nombrePeriodo || '').toLowerCase().includes(term)
    );
  }

  abrirModalPostulacion(convocatoria: ConvocatoriaDTO) {
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
      const index = this.archivosSubidos.findIndex(a => a.idRequisito === idRequisito);
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

    // Ordenar archivos e IDs para que coincidan en el backend
    const idsRequisitos = this.archivosSubidos.map(a => a.idRequisito);
    const files = this.archivosSubidos.map(a => a.file);

    const datosFormulario = {
      idConvocatoria: this.convocatoriaSeleccionada.idConvocatoria,
      idEstudiante: this.idEstudianteBase,
      observaciones: this.observacionesGenerales,
      idTipoEstado: 1
    };

    this.subs.add(
      this.postulanteService.registrarPostulacion(datosFormulario, files, idsRequisitos).subscribe({
        next: () => {
          alert('Postulación registrada exitosamente');
          this.cerrarModal();
          this.listarConvocatorias();
        },
        error: (err: HttpErrorResponse) => {
          console.error('Error al postular:', err);
          const msg = typeof err.error === 'string' ? err.error : (err.message || 'Error al enviar la postulación');
          alert(msg);
        }
      })
    );
  }
}
