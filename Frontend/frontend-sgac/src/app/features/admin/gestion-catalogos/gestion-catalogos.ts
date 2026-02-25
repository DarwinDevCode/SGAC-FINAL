import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, Plus, Edit, Power, X } from 'lucide-angular';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { TipoRolDTO } from '../../../core/dto/tipo-rol';
import { FacultadDTO } from '../../../core/dto/facultad';
import { CarreraDTO } from '../../../core/dto/carrera';
import { AsignaturaDTO } from '../../../core/dto/asignatura';
import { TipoRequisitoPostulacionDTO } from '../../../core/dto/tipo-requisito-postulacion';
import { TipoEstadoEvidenciaAyudantiaDTO } from '../../../core/dto/tipo-estado-evidencia-ayudantia';

@Component({
  selector: 'app-gestion-catalogos',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  providers: [
    {
      provide: LUCIDE_ICONS,
      multi: true,
      useValue: new LucideIconProvider({ Plus, Edit, Power, X })
    }
  ],
  templateUrl: './gestion-catalogos.html',
  styleUrl: './gestion-catalogos.css',
})
export class GestionCatalogosComponent implements OnInit, OnDestroy {
  private catalogosService = inject(CatalogosService);
  private subs = new Subscription();

  activeTab: 'roles' | 'facultades' | 'carreras' | 'asignaturas' | 'requisitosPostulacion' | 'estadoEvidencias' = 'roles';

  rolesList: TipoRolDTO[] = [];
  facultadesList: FacultadDTO[] = [];
  carrerasList: CarreraDTO[] = [];
  asignaturasList: AsignaturaDTO[] = [];
  requisitosPostulacionList: TipoRequisitoPostulacionDTO[] = [];
  estadoEvidenciasAyudantiasList: TipoEstadoEvidenciaAyudantiaDTO[] = [];

  loading = false;
  mostrarModal = false;
  isEditMode = false;

  formRol: Partial<TipoRolDTO> = {};
  formFacultad: Partial<FacultadDTO> = {};
  formCarrera: Partial<CarreraDTO> = {};
  formAsignatura: Partial<AsignaturaDTO> = {};
  formRequisito: Partial<TipoRequisitoPostulacionDTO> = {};
  formEstado: Partial<TipoEstadoEvidenciaAyudantiaDTO> = {};

  ngOnInit(): void {
    this.cargarDatosActivos();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  setTab(tab: 'roles' | 'facultades' | 'carreras' | 'asignaturas' | 'requisitosPostulacion' | 'estadoEvidencias') {
    this.activeTab = tab;
    this.cargarDatosActivos();
  }

  cargarDatosActivos() {
    this.loading = true;

    if (this.activeTab === 'roles') {
      this.subs.add(this.catalogosService.getTiposRol().subscribe({
        next: (data) => { this.rolesList = data || []; this.loading = false; },
        error: (err: HttpErrorResponse) => { alert(err.error?.message || 'Error al cargar roles'); this.loading = false; }
      }));
    } else if (this.activeTab === 'facultades') {
      this.subs.add(this.catalogosService.getFacultades().subscribe({
        next: (data) => { this.facultadesList = data || []; this.loading = false; },
        error: (err: HttpErrorResponse) => { alert(err.error?.message || 'Error al cargar facultades'); this.loading = false; }
      }));
    } else if (this.activeTab === 'carreras') {
      this.subs.add(this.catalogosService.getCarreras().subscribe({
        next: (data) => { this.carrerasList = data || []; this.loading = false; },
        error: (err: HttpErrorResponse) => { alert(err.error?.message || 'Error al cargar carreras'); this.loading = false; }
      }));
    } else if (this.activeTab === 'asignaturas') {
      this.subs.add(this.catalogosService.getAsignaturas().subscribe({
        next: (data) => { this.asignaturasList = data || []; this.loading = false; },
        error: (err: HttpErrorResponse) => { alert(err.error?.message || 'Error al cargar asignaturas'); this.loading = false; }
      }));
    } else if (this.activeTab === 'requisitosPostulacion') {
      this.subs.add(this.catalogosService.getTiposRequisito().subscribe({
        next: (data) => { this.requisitosPostulacionList = data || []; this.loading = false; console.log(this.requisitosPostulacionList) },
        error: (err: HttpErrorResponse) => { alert(err.error?.message || 'Error al cargar requisitos'); this.loading = false; }
      }));
    } else if (this.activeTab === 'estadoEvidencias') {
      this.subs.add(this.catalogosService.getEstadosEvidencia().subscribe({
        next: (data) => { this.estadoEvidenciasAyudantiasList = data || []; this.loading = false; console.log(this.estadoEvidenciasAyudantiasList)},
        error: (err: HttpErrorResponse) => { alert(err.error?.message || 'Error al cargar estados'); this.loading = false; }
      }));
    }
  }


  abrirModalNuevoRol() {
    this.isEditMode = false;
    this.formRol = { nombreTipoRol: '', activo: true };
    this.mostrarModal = true;
  }
  abrirModalEditarRol(rol: TipoRolDTO) {
    this.isEditMode = true;
    this.formRol = { ...rol };
    this.mostrarModal = true;
  }
  guardarRol() {
    const peticion = this.isEditMode && this.formRol.idTipoRol
      ? this.catalogosService.putTipoRol(this.formRol.idTipoRol, this.formRol as TipoRolDTO)
      : this.catalogosService.postTipoRol(this.formRol as TipoRolDTO);

    this.procesarGuardado(peticion, 'Rol');
  }
  toggleEstadoRol(rol: TipoRolDTO) {
    if (!rol.idTipoRol) return;
    if (!confirm(`¿Desea cambiar el estado de ${rol.nombreTipoRol}?`)) return;
    this.subs.add(this.catalogosService.desactivarTipoRol(rol.idTipoRol).subscribe({
      next: () => rol.activo = !rol.activo,
      error: (err: HttpErrorResponse) => alert(err.error?.message || 'Error al cambiar estado')
    }));
  }


  abrirModalNuevaFacultad() {
    this.isEditMode = false;
    this.formFacultad = { nombreFacultad: '', activo: true };
    this.mostrarModal = true;
  }
  abrirModalEditarFacultad(facultad: FacultadDTO) {
    this.isEditMode = true;
    this.formFacultad = { ...facultad };
    this.mostrarModal = true;
  }
  guardarFacultad() {
    const peticion = this.isEditMode && this.formFacultad.idFacultad
      ? this.catalogosService.putFacultad(this.formFacultad.idFacultad, this.formFacultad as FacultadDTO)
      : this.catalogosService.postFacultad(this.formFacultad as FacultadDTO);
    this.procesarGuardado(peticion, 'Facultad');
  }
  toggleEstadoFacultad(f: FacultadDTO) {
    if (!f.idFacultad) return;
    if (!confirm(`¿Desea cambiar el estado de ${f.nombreFacultad}?`)) return;
    this.subs.add(this.catalogosService.desactivarFacultad(f.idFacultad).subscribe({
      next: () => f.activo = !f.activo,
      error: (err: HttpErrorResponse) => alert(err.error?.message || 'Error al cambiar estado')
    }));
  }


  abrirModalNuevaCarrera() {
    this.isEditMode = false;
    this.formCarrera = { nombreCarrera: '', idFacultad: undefined, activo: true };
    if (this.facultadesList.length === 0) this.cargarFacultadesSilencioso();
    this.mostrarModal = true;
  }
  abrirModalEditarCarrera(carrera: CarreraDTO) {
    this.isEditMode = true;
    this.formCarrera = { ...carrera };
    if (this.facultadesList.length === 0) this.cargarFacultadesSilencioso();
    this.mostrarModal = true;
  }
  guardarCarrera() {
    const peticion = this.isEditMode && this.formCarrera.idCarrera
      ? this.catalogosService.putCarrera(this.formCarrera.idCarrera, this.formCarrera as CarreraDTO)
      : this.catalogosService.postCarrera(this.formCarrera as CarreraDTO);
    this.procesarGuardado(peticion, 'Carrera');
  }
  toggleEstadoCarrera(c: CarreraDTO) {
    if (!c.idCarrera) return;
    if (!confirm(`¿Desea cambiar el estado de ${c.nombreCarrera}?`)) return;
    this.subs.add(this.catalogosService.desactivarCarrera(c.idCarrera).subscribe({
      next: () => c.activo = !c.activo,
      error: (err: HttpErrorResponse) => alert(err.error?.message || 'Error al cambiar estado')
    }));
  }
  private cargarFacultadesSilencioso() {
    this.subs.add(this.catalogosService.getFacultades().subscribe(d => this.facultadesList = d || []));
  }


  abrirModalNuevaAsignatura() {
    this.isEditMode = false;
    this.formAsignatura = { nombreAsignatura: '', semestre: 1, idCarrera: undefined, activo: true };
    if (this.carrerasList.length === 0) this.cargarCarrerasSilencioso();
    this.mostrarModal = true;
  }
  abrirModalEditarAsignatura(asignatura: AsignaturaDTO) {
    this.isEditMode = true;
    this.formAsignatura = { ...asignatura };
    if (this.carrerasList.length === 0) this.cargarCarrerasSilencioso();
    this.mostrarModal = true;
  }
  guardarAsignatura() {
    const peticion = this.isEditMode && this.formAsignatura.idAsignatura
      ? this.catalogosService.putAsignatura(this.formAsignatura.idAsignatura, this.formAsignatura as AsignaturaDTO)
      : this.catalogosService.postAsignatura(this.formAsignatura as AsignaturaDTO);
    this.procesarGuardado(peticion, 'Asignatura');
  }
  toggleEstadoAsignatura(a: AsignaturaDTO) {
    if (!a.idAsignatura) return;
    if (!confirm(`¿Desea cambiar el estado de ${a.nombreAsignatura}?`)) return;
    this.subs.add(this.catalogosService.desactivarAsignatura(a.idAsignatura).subscribe({
      next: () => a.activo = !a.activo,
      error: (err: HttpErrorResponse) => alert(err.error?.message || 'Error al cambiar estado')
    }));
  }
  private cargarCarrerasSilencioso() {
    this.subs.add(this.catalogosService.getCarreras().subscribe(d => this.carrerasList = d || []));
  }


  abrirModalNuevoRequisito() {
    this.isEditMode = false;
    // @ts-ignore - Ajusta el nombre del campo si tu DTO varía
    this.formRequisito = { nombreRequisito: '', activo: true };
    this.mostrarModal = true;
  }
  abrirModalEditarRequisito(r: TipoRequisitoPostulacionDTO) {
    this.isEditMode = true;
    this.formRequisito = { ...r };
    this.mostrarModal = true;
  }
  guardarRequisito() {
    const peticion = this.isEditMode && this.formRequisito.idTipoRequisitoPostulacion
      ? this.catalogosService.putTipoRequisito(this.formRequisito.idTipoRequisitoPostulacion, this.formRequisito as TipoRequisitoPostulacionDTO)
      : this.catalogosService.postTipoRequisito(this.formRequisito as TipoRequisitoPostulacionDTO);
    this.procesarGuardado(peticion, 'Requisito');
  }
  toggleEstadoRequisito(r: TipoRequisitoPostulacionDTO) {
    if (!r.idTipoRequisitoPostulacion) return;
    if (!confirm(`¿Desea cambiar el estado de este requisito?`)) return;
    this.subs.add(this.catalogosService.desactivarTipoRequisito(r.idTipoRequisitoPostulacion).subscribe({
      next: () => r.activo = !r.activo,
      error: (err: HttpErrorResponse) => alert(err.error?.message || 'Error al cambiar estado')
    }));
  }


  abrirModalNuevoEstado() {
    this.isEditMode = false;
    this.formEstado = { nombreEstado: '', descripcion: '', activo: true };
    this.mostrarModal = true;
  }
  abrirModalEditarEstado(e: TipoEstadoEvidenciaAyudantiaDTO) {
    this.isEditMode = true;
    this.formEstado = { ...e };
    this.mostrarModal = true;
  }
  guardarEstado() {
    const peticion = this.isEditMode && this.formEstado.idTipoEstadoEvidenciaAyudantia
      ? this.catalogosService.putEstadoEvidencia(this.formEstado.idTipoEstadoEvidenciaAyudantia, this.formEstado as TipoEstadoEvidenciaAyudantiaDTO)
      : this.catalogosService.postEstadoEvidencia(this.formEstado as TipoEstadoEvidenciaAyudantiaDTO);
    this.procesarGuardado(peticion, 'Estado de Evidencia');
  }
  toggleEstadoEvidencia(e: TipoEstadoEvidenciaAyudantiaDTO) {
    if (!e.idTipoEstadoEvidenciaAyudantia) return;
    if (!confirm(`¿Desea cambiar el estado de ${e.nombreEstado}?`)) return;
    this.subs.add(this.catalogosService.desactivarEstadoEvidencia(e.idTipoEstadoEvidenciaAyudantia).subscribe({
      next: () => e.activo = !e.activo,
      error: (err: HttpErrorResponse) => alert(err.error?.message || 'Error al cambiar estado')
    }));
  }


  private procesarGuardado(peticionObservable: any, nombreEntidad: string) {
    this.subs.add(
      peticionObservable.subscribe({
        next: () => {
          alert(`${nombreEntidad} ${this.isEditMode ? 'actualizado(a)' : 'registrado(a)'} correctamente.`);
          this.mostrarModal = false;
          this.cargarDatosActivos();

          if (nombreEntidad === 'Rol')
            this.catalogosService.rolActualizado$.next();
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          if (err.error?.errors) {
            const mensajesValidacion = Object.values(err.error.errors).join('\n - ');
            alert(`Por favor corrige:\n - ${mensajesValidacion}`);
          } else if (err.error?.message) {
            alert(`Error: ${err.error.message}`);
          } else {
            alert('Ocurrió un error inesperado al conectar con el servidor.');
          }
        }
      })
    );
  }
}
