import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { DocenteService } from '../../../core/services/docente-service';
import { AuthService } from '../../../core/services/auth-service';
import { finalize } from 'rxjs';

export interface InformeMensual {
  idInformeMensual: number;
  mes: number;
  anio: number;
  estado: string;
  observaciones?: string;
  fechaGeneracion?: string;
  fechaRevisionDocente?: string;
  ayudantia?: {
    idAyudantia: number;
    postulacion?: {
      estudiante?: {
        usuario?: { nombres: string; apellidos: string };
      }
      convocatoria?: {
        asignatura?: { nombre: string };
      }
    }
  };
}

const MESES = ['', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

@Component({
  selector: 'app-aprobar-informes-docente',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './aprobar-informes.html',
  styleUrls: ['./aprobar-informes.css']
})
export class AprobarInformesDocenteComponent implements OnInit {
  private docenteService = inject(DocenteService);
  private authService = inject(AuthService);

  informes: InformeMensual[] = [];
  isLoading = false;
  errorMsg = '';
  successMsg = '';

  // Modal de decisión
  modalVisible = false;
  modalAccion: 'aprobar' | 'rechazar' = 'aprobar';
  modalInforme: InformeMensual | null = null;
  modalObservaciones = '';
  guardando = false;

  readonly MESES = MESES;

  ngOnInit() {
    this.cargarInformes();
  }

  cargarInformes() {
    const idDocente = this.authService.getUser()?.idUsuario;
    if (!idDocente) return;
    this.isLoading = true;
    this.errorMsg = '';
    this.docenteService.getInformesPendientesDocente(idDocente)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (data) => { this.informes = data ?? []; },
        error: () => { this.errorMsg = 'No se pudieron cargar los informes pendientes.'; }
      });
  }

  abrirModal(informe: InformeMensual, accion: 'aprobar' | 'rechazar') {
    this.modalInforme = informe;
    this.modalAccion = accion;
    this.modalObservaciones = '';
    this.modalVisible = true;
  }

  cerrarModal() {
    this.modalVisible = false;
    this.modalInforme = null;
  }

  confirmar() {
    if (!this.modalInforme) return;
    if (this.modalAccion === 'rechazar' && !this.modalObservaciones.trim()) {
      alert('Debes escribir las observaciones para rechazar el informe.');
      return;
    }
    this.guardando = true;
    const idInforme = this.modalInforme.idInformeMensual;
    const obs$ = this.modalAccion === 'aprobar'
      ? this.docenteService.aprobarInformeDocente(idInforme, this.modalObservaciones || 'Revisado y aprobado por el docente.')
      : this.docenteService.rechazarInformeDocente(idInforme, this.modalObservaciones);

    obs$.pipe(finalize(() => this.guardando = false)).subscribe({
      next: () => {
        this.cerrarModal();
        this.successMsg = this.modalAccion === 'aprobar'
          ? 'Informe aprobado correctamente. Pasa a revisión del Coordinador.'
          : 'Informe rechazado. El Ayudante deberá corregirlo.';
        this.cargarInformes();
        setTimeout(() => this.successMsg = '', 4000);
      },
      error: () => { alert('Error al procesar el informe.'); }
    });
  }

  getNombreAyudante(inf: InformeMensual): string {
    const u = inf.ayudantia?.postulacion?.estudiante?.usuario;
    return u ? `${u.nombres} ${u.apellidos}` : '—';
  }

  getNombreAsignatura(inf: InformeMensual): string {
    return inf.ayudantia?.postulacion?.convocatoria?.asignatura?.nombre ?? '—';
  }

  getMesNombre(mes: number): string {
    return MESES[mes] ?? `Mes ${mes}`;
  }

  getEstadoClass(estado: string): string {
    const e = (estado || '').toUpperCase();
    if (e === 'GENERADO') return 'badge-pendiente';
    if (e === 'REVISADO_DOCENTE') return 'badge-revisado';
    if (e.includes('APROBADO')) return 'badge-aprobado';
    if (e.includes('RECHAZADO')) return 'badge-rechazado';
    return 'badge-default';
  }
}
