import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import {LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, Plus, Edit, Power, X, ChevronDown} from 'lucide-angular';

import { CatalogosService } from '../../../core/services/catalogos-service';
import { TipoRolDTO } from '../../../core/dto/tipo-rol';
import { FacultadDTO } from '../../../core/dto/facultad';
import { CarreraDTO } from '../../../core/dto/carrera';
import { AsignaturaDTO } from '../../../core/dto/asignatura';
import { TipoRequisitoPostulacionDTO } from '../../../core/dto/tipo-requisito-postulacion';
import { TipoEstadoEvidenciaAyudantiaDTO } from '../../../core/dto/tipo-estado-evidencia-ayudantia';

import { TipoSancionService } from '../../../core/services/catalogos/tipo-sancion.service';
import { TipoEstadoAyudantiaService } from '../../../core/services/catalogos/tipo-estado-ayudantia.service';
import { TipoEstadoRegistroService } from '../../../core/services/catalogos/tipo-estado-registro.service';
import { TipoEvidenciaService } from '../../../core/services/catalogos/tipo-evidencia.service';
import { TipoEstadoEvidenciaService } from '../../../core/services/catalogos/tipo-estado-evidencia.service';
import { TipoEstadoRequisitoService } from '../../../core/services/catalogos/tipo-estado-requisito.service';
import { TipoFaseService } from '../../../core/services/catalogos/tipo-fase.service';
import { TipoEstadoPostulacionService } from '../../../core/services/catalogos/tipo-estado-postulacion.service';
import { PrivilegioService } from '../../../core/services/catalogos/privilegio.service';

import { TipoSancionRequest, TipoSancionResponse } from '../../../core/models/catalogos/TipoSancion';
import { TipoEstadoAyudantiaRequest, TipoEstadoAyudantiaResponse } from '../../../core/models/catalogos/TipoEstadoAyudantia';
import { TipoEstadoRegistroRequest, TipoEstadoRegistroResponse } from '../../../core/models/catalogos/TipoEstadoRegistro';
import { TipoEvidenciaRequest, TipoEvidenciaResponse } from '../../../core/models/catalogos/TipoEvidencia';
import { TipoEstadoEvidenciaRequest, TipoEstadoEvidenciaResponse } from '../../../core/models/catalogos/TipoEstadoEvidencia';
import { TipoEstadoRequisitoRequest, TipoEstadoRequisitoResponse } from '../../../core/models/catalogos/TipoEstadoRequisito';
import { TipoFaseRequest, TipoFaseResponse } from '../../../core/models/catalogos/TipoFase';
import { TipoEstadoPostulacionRequest, TipoEstadoPostulacionResponse } from '../../../core/models/catalogos/TipoEstadoPostulacion';
import { PrivilegioRequest, PrivilegioResponse } from '../../../core/models/catalogos/Privilegio';
import { StandardModificacionResponse } from '../../../core/models/catalogos/StandardResponse';

type ActiveTab =
  | 'roles' | 'facultades' | 'carreras' | 'asignaturas' | 'requisitosPostulacion'
  | 'tiposSancion' | 'estadosAyudantia' | 'estadosRegistro' | 'tiposEvidencia'
  | 'estadosEvidenciaMaestro' | 'estadosRequisito' | 'tiposFase'
  | 'estadosPostulacion' | 'privilegios';

export interface CatalogoOpcion {
  value: ActiveTab;
  label: string;
  grupo: 'Catálogos Institucionales' | 'Catálogos Maestros';
}

@Component({
  selector: 'app-gestion-catalogos',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gestion-catalogos.html',
  styleUrls: ['./gestion-catalogos.css', '../admin-shared.css'],
})

export class GestionCatalogosComponent implements OnInit, OnDestroy {

  // ── Servicios ────────────────────────────────────────────────────────────
  private catalogosService         = inject(CatalogosService);
  private tipoSancionSvc           = inject(TipoSancionService);
  private tipoEstadoAyudantiaSvc   = inject(TipoEstadoAyudantiaService);
  private tipoEstadoRegistroSvc    = inject(TipoEstadoRegistroService);
  private tipoEvidenciaSvc         = inject(TipoEvidenciaService);
  private tipoEstadoEvidenciaSvc   = inject(TipoEstadoEvidenciaService);   // → /estados-evidencia
  private tipoEstadoRequisitoSvc   = inject(TipoEstadoRequisitoService);
  private tipoFaseSvc              = inject(TipoFaseService);
  private tipoEstadoPostulacionSvc = inject(TipoEstadoPostulacionService);
  private privilegioSvc            = inject(PrivilegioService);

  private subs = new Subscription();

  // ── Estado del componente ────────────────────────────────────────────────
  activeTab: ActiveTab = 'roles';
  loading       = false;
  mostrarModal  = false;
  isEditMode    = false;
  editandoId: number | null = null;

  // ── Definición centralizada del selector ─────────────────────────────────
  // Dividir en dos grupos permite usar <optgroup> en el template sin lógica adicional.
  readonly grupos: { label: string; opciones: CatalogoOpcion[] }[] = [
    {
      label: 'Catálogos Institucionales',
      opciones: [
        { value: 'roles',                 label: 'Roles',                      grupo: 'Catálogos Institucionales' },
        { value: 'facultades',            label: 'Facultades',                  grupo: 'Catálogos Institucionales' },
        { value: 'carreras',              label: 'Carreras',                    grupo: 'Catálogos Institucionales' },
        { value: 'asignaturas',           label: 'Asignaturas',                 grupo: 'Catálogos Institucionales' },
        { value: 'requisitosPostulacion', label: 'Requisitos de Postulación',   grupo: 'Catálogos Institucionales' },
      ]
    },
    {
      label: 'Catálogos Maestros',
      opciones: [
        { value: 'tiposSancion',            label: 'Tipos de Sanción',           grupo: 'Catálogos Maestros' },
        { value: 'estadosAyudantia',        label: 'Estados de Ayudantía',       grupo: 'Catálogos Maestros' },
        { value: 'estadosRegistro',         label: 'Estados de Registro',        grupo: 'Catálogos Maestros' },
        { value: 'tiposEvidencia',          label: 'Tipos de Evidencia',         grupo: 'Catálogos Maestros' },
        { value: 'estadosEvidenciaMaestro', label: 'Estados de Evidencia',       grupo: 'Catálogos Maestros' },
        { value: 'estadosRequisito',        label: 'Estados de Requisito',       grupo: 'Catálogos Maestros' },
        { value: 'tiposFase',              label: 'Tipos de Fase',              grupo: 'Catálogos Maestros' },
        { value: 'estadosPostulacion',     label: 'Estados de Postulación',     grupo: 'Catálogos Maestros' },
        { value: 'privilegios',            label: 'Privilegios',                grupo: 'Catálogos Maestros' },
      ]
    }
  ];

  // Etiqueta del catálogo actualmente seleccionado (usado en el encabezado de la tabla)
  get labelActivo(): string {
    for (const g of this.grupos) {
      const encontrada = g.opciones.find(o => o.value === this.activeTab);
      if (encontrada) return encontrada.label;
    }
    return '';
  }

  // ── Listas de datos ───────────────────────────────────────────────────────
  rolesList:                TipoRolDTO[]                   = [];
  facultadesList:           FacultadDTO[]                  = [];
  carrerasList:             CarreraDTO[]                   = [];
  asignaturasList:          AsignaturaDTO[]                = [];
  requisitosPostulacionList: TipoRequisitoPostulacionDTO[] = [];

  tiposSancionList:       TipoSancionResponse[]           = [];
  estadosAyudantiaList:   TipoEstadoAyudantiaResponse[]   = [];
  estadosRegistroList:    TipoEstadoRegistroResponse[]    = [];
  tiposEvidenciaList:     TipoEvidenciaResponse[]         = [];
  estadosEvidenciaList:   TipoEstadoEvidenciaResponse[]   = [];
  estadosRequisitoList:   TipoEstadoRequisitoResponse[]   = [];
  tiposFaseList:          TipoFaseResponse[]              = [];
  estadosPostulacionList: TipoEstadoPostulacionResponse[] = [];
  privilegiosList:        PrivilegioResponse[]            = [];

  // ── Formularios institucionales ───────────────────────────────────────────
  formRol:        Partial<TipoRolDTO>                  = {};
  formFacultad:   Partial<FacultadDTO>                 = {};
  formCarrera:    Partial<CarreraDTO>                  = {};
  formAsignatura: Partial<AsignaturaDTO>               = {};
  formRequisito:  Partial<TipoRequisitoPostulacionDTO> = {};

  // ── Formularios maestros ──────────────────────────────────────────────────
  formSancion:           Partial<TipoSancionRequest>           = {};
  formFase:              Partial<TipoFaseRequest>              = {};
  formEstadoPostulacion: Partial<TipoEstadoPostulacionRequest> = {};
  formEvidencia:         Partial<TipoEvidenciaRequest>         = {};
  formPrivilegio:        Partial<PrivilegioRequest>            = {};

  // Los catálogos estadosAyudantia, estadosRegistro, estadosEvidenciaMaestro y
  // estadosRequisito tienen exactamente los mismos campos (nombre_estado, codigo,
  // descripcion), así que comparten un único objeto de formulario en el template.
  // El método guardarEstadoGenerico() despacha al servicio correcto según activeTab.
  formEstadoCompartido: { nombreEstado: string; codigo: string; descripcion: string } =
    { nombreEstado: '', codigo: '', descripcion: '' };

  // El template enlaza este getter para no necesitar conocer el tab activo
  get formEstadoActivo() { return this.formEstadoCompartido; }

  // ── Ciclo de vida ─────────────────────────────────────────────────────────
  ngOnInit(): void   { this.cargarDatosActivos(); }
  ngOnDestroy(): void { this.subs.unsubscribe(); }

  // Llamado por el (change) del <select> del template
  onCatalogoChange(valor: string): void {
    this.activeTab    = valor as ActiveTab;
    this.mostrarModal = false;
    this.cargarDatosActivos();
  }

  // ── Carga de datos ────────────────────────────────────────────────────────
  cargarDatosActivos(): void {
    this.loading = true;

    if (this.activeTab === 'roles') {
      this.subs.add(this.catalogosService.getTiposRol().subscribe({
        next: d => { this.rolesList = d || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'roles')
      }));
    } else if (this.activeTab === 'facultades') {
      this.subs.add(this.catalogosService.getFacultades().subscribe({
        next: d => { this.facultadesList = d || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'facultades')
      }));
    } else if (this.activeTab === 'carreras') {
      this.subs.add(this.catalogosService.getCarreras().subscribe({
        next: d => { this.carrerasList = d || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'carreras')
      }));
    } else if (this.activeTab === 'asignaturas') {
      this.subs.add(this.catalogosService.getAsignaturas().subscribe({
        next: d => { this.asignaturasList = d || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'asignaturas')
      }));
    } else if (this.activeTab === 'requisitosPostulacion') {
      this.subs.add(this.catalogosService.getTiposRequisito().subscribe({
        next: d => { this.requisitosPostulacionList = d || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'requisitos')
      }));

    } else if (this.activeTab === 'tiposSancion') {
      this.subs.add(this.tipoSancionSvc.listar().subscribe({
        next: r => { this.tiposSancionList = r.datos || []; this.loading = false;
          console.log(this.tiposSancionList);
          },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'tipos de sanción')
      }));
    } else if (this.activeTab === 'estadosAyudantia') {
      this.subs.add(this.tipoEstadoAyudantiaSvc.listar().subscribe({
        next: r => { this.estadosAyudantiaList = r.datos || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estados de ayudantía')
      }));
    } else if (this.activeTab === 'estadosRegistro') {
      this.subs.add(this.tipoEstadoRegistroSvc.listar().subscribe({
        next: r => { this.estadosRegistroList = r.datos || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estados de registro')
      }));
    } else if (this.activeTab === 'tiposEvidencia') {
      this.subs.add(this.tipoEvidenciaSvc.listar().subscribe({
        next: r => { this.tiposEvidenciaList = r.datos || []; this.loading = false;
          console.log(this.tiposEvidenciaList);
          },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'tipos de evidencia')
      }));
    } else if (this.activeTab === 'estadosEvidenciaMaestro') {
      // Este catálogo consume /api/admin/catalogos-maestros/estados-evidencia
      this.subs.add(this.tipoEstadoEvidenciaSvc.listar().subscribe({
        next: r => { this.estadosEvidenciaList = r.datos || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estados de evidencia')
      }));
    } else if (this.activeTab === 'estadosRequisito') {
      this.subs.add(this.tipoEstadoRequisitoSvc.listar().subscribe({
        next: r => { this.estadosRequisitoList = r.datos || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estados de requisito')
      }));
    } else if (this.activeTab === 'tiposFase') {
      this.subs.add(this.tipoFaseSvc.listar().subscribe({
        next: r => { this.tiposFaseList = r.datos || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'tipos de fase')
      }));
    } else if (this.activeTab === 'estadosPostulacion') {
      this.subs.add(this.tipoEstadoPostulacionSvc.listar().subscribe({
        next: r => { this.estadosPostulacionList = r.datos || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estados de postulación')
      }));
    } else if (this.activeTab === 'privilegios') {
      this.subs.add(this.privilegioSvc.listar().subscribe({
        next: r => { this.privilegiosList = r.datos || []; this.loading = false; },
        error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'privilegios')
      }));
    }
  }

  // =========================================================================
  // CRUD – Catálogos institucionales
  // =========================================================================

  abrirModalNuevoRol() { this.isEditMode = false; this.formRol = { nombreTipoRol: '', activo: true }; this.mostrarModal = true; }
  abrirModalEditarRol(r: TipoRolDTO) { this.isEditMode = true; this.formRol = { ...r }; this.mostrarModal = true; }
  guardarRol() {
    const p = this.isEditMode && this.formRol.idTipoRol
      ? this.catalogosService.putTipoRol(this.formRol.idTipoRol, this.formRol as TipoRolDTO)
      : this.catalogosService.postTipoRol(this.formRol as TipoRolDTO);
    this.procesarGuardadoLegacy(p, 'Rol', () => this.catalogosService.rolActualizado$.next());
  }
  toggleEstadoRol(r: TipoRolDTO) {
    if (!r.idTipoRol || !confirm(`¿Cambiar estado de ${r.nombreTipoRol}?`)) return;
    this.subs.add(this.catalogosService.desactivarTipoRol(r.idTipoRol).subscribe({
      next: () => r.activo = !r.activo,
      error: (e: HttpErrorResponse) => alert(e.error?.message || 'Error al cambiar estado')
    }));
  }

  abrirModalNuevaFacultad() { this.isEditMode = false; this.formFacultad = { nombreFacultad: '', activo: true }; this.mostrarModal = true; }
  abrirModalEditarFacultad(f: FacultadDTO) { this.isEditMode = true; this.formFacultad = { ...f }; this.mostrarModal = true; }
  guardarFacultad() {
    const p = this.isEditMode && this.formFacultad.idFacultad
      ? this.catalogosService.putFacultad(this.formFacultad.idFacultad, this.formFacultad as FacultadDTO)
      : this.catalogosService.postFacultad(this.formFacultad as FacultadDTO);
    this.procesarGuardadoLegacy(p, 'Facultad');
  }
  toggleEstadoFacultad(f: FacultadDTO) {
    if (!f.idFacultad || !confirm(`¿Cambiar estado de ${f.nombreFacultad}?`)) return;
    this.subs.add(this.catalogosService.desactivarFacultad(f.idFacultad).subscribe({
      next: () => f.activo = !f.activo,
      error: (e: HttpErrorResponse) => alert(e.error?.message || 'Error')
    }));
  }

  abrirModalNuevaCarrera() {
    this.isEditMode = false;
    this.formCarrera = { nombreCarrera: '', idFacultad: undefined, activo: true };
    if (!this.facultadesList.length) this.subs.add(this.catalogosService.getFacultades().subscribe(d => this.facultadesList = d || []));
    this.mostrarModal = true;
  }
  abrirModalEditarCarrera(c: CarreraDTO) {
    this.isEditMode = true; this.formCarrera = { ...c };
    if (!this.facultadesList.length) this.subs.add(this.catalogosService.getFacultades().subscribe(d => this.facultadesList = d || []));
    this.mostrarModal = true;
  }
  guardarCarrera() {
    const p = this.isEditMode && this.formCarrera.idCarrera
      ? this.catalogosService.putCarrera(this.formCarrera.idCarrera, this.formCarrera as CarreraDTO)
      : this.catalogosService.postCarrera(this.formCarrera as CarreraDTO);
    this.procesarGuardadoLegacy(p, 'Carrera');
  }
  toggleEstadoCarrera(c: CarreraDTO) {
    if (!c.idCarrera || !confirm(`¿Cambiar estado de ${c.nombreCarrera}?`)) return;
    this.subs.add(this.catalogosService.desactivarCarrera(c.idCarrera).subscribe({
      next: () => c.activo = !c.activo,
      error: (e: HttpErrorResponse) => alert(e.error?.message || 'Error')
    }));
  }

  abrirModalNuevaAsignatura() {
    this.isEditMode = false;
    this.formAsignatura = { nombreAsignatura: '', semestre: 1, idCarrera: undefined, activo: true };
    if (!this.carrerasList.length) this.subs.add(this.catalogosService.getCarreras().subscribe(d => this.carrerasList = d || []));
    this.mostrarModal = true;
  }
  abrirModalEditarAsignatura(a: AsignaturaDTO) {
    this.isEditMode = true; this.formAsignatura = { ...a };
    if (!this.carrerasList.length) this.subs.add(this.catalogosService.getCarreras().subscribe(d => this.carrerasList = d || []));
    this.mostrarModal = true;
  }
  guardarAsignatura() {
    const p = this.isEditMode && this.formAsignatura.idAsignatura
      ? this.catalogosService.putAsignatura(this.formAsignatura.idAsignatura, this.formAsignatura as AsignaturaDTO)
      : this.catalogosService.postAsignatura(this.formAsignatura as AsignaturaDTO);
    this.procesarGuardadoLegacy(p, 'Asignatura');
  }
  toggleEstadoAsignatura(a: AsignaturaDTO) {
    if (!a.idAsignatura || !confirm(`¿Cambiar estado de ${a.nombreAsignatura}?`)) return;
    this.subs.add(this.catalogosService.desactivarAsignatura(a.idAsignatura).subscribe({
      next: () => a.activo = !a.activo,
      error: (e: HttpErrorResponse) => alert(e.error?.message || 'Error')
    }));
  }

  abrirModalNuevoRequisito() { this.isEditMode = false; this.formRequisito = { nombreRequisito: '', activo: true, tipoDocumentoPermitido: '' } as any; this.mostrarModal = true; }
  abrirModalEditarRequisito(r: TipoRequisitoPostulacionDTO) { this.isEditMode = true; this.formRequisito = { ...r }; this.mostrarModal = true; }
  guardarRequisito() {
    const p = this.isEditMode && this.formRequisito.idTipoRequisitoPostulacion
      ? this.catalogosService.putTipoRequisito(this.formRequisito.idTipoRequisitoPostulacion, this.formRequisito as TipoRequisitoPostulacionDTO)
      : this.catalogosService.postTipoRequisito(this.formRequisito as TipoRequisitoPostulacionDTO);
    this.procesarGuardadoLegacy(p, 'Requisito');
  }
  toggleEstadoRequisito(r: TipoRequisitoPostulacionDTO) {
    if (!r.idTipoRequisitoPostulacion || !confirm('¿Cambiar estado de este requisito?')) return;
    this.subs.add(this.catalogosService.desactivarTipoRequisito(r.idTipoRequisitoPostulacion).subscribe({
      next: () => r.activo = !r.activo,
      error: (e: HttpErrorResponse) => alert(e.error?.message || 'Error')
    }));
  }

  // =========================================================================
  // CRUD – Catálogos maestros
  // =========================================================================

  // ── Tipos de Sanción ──────────────────────────────────────────────────────
  abrirModalNuevoSancion() { this.isEditMode = false; this.editandoId = null; this.formSancion = { nombreTipoSancion: '', codigo: '' }; this.mostrarModal = true; }
  abrirModalEditarSancion(i: TipoSancionResponse) { this.isEditMode = true; this.editandoId = i.id; this.formSancion = { nombreTipoSancion: i.nombre_tipo_sancion, codigo: i.codigo }; this.mostrarModal = true; }
  guardarSancion() {
    const p = this.isEditMode && this.editandoId
      ? this.tipoSancionSvc.actualizar(this.editandoId, this.formSancion as TipoSancionRequest)
      : this.tipoSancionSvc.crear(this.formSancion as TipoSancionRequest);
    this.procesarGuardadoMaestro(p, 'Tipo de Sanción');
  }
  desactivarSancion(i: TipoSancionResponse) {
    if (!confirm(`¿Desactivar "${i.nombre_tipo_sancion}"?`)) return;
    this.subs.add(this.tipoSancionSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'tipo de sanción')
    }));
  }

  // ── Helper para poblar el formulario compartido al abrir modal ───────────
  private abrirModalEstadoCompartido(id: number | null, datos: { nombreEstado: string; codigo: string; descripcion: string }): void {
    this.isEditMode = id !== null;
    this.editandoId = id;
    this.formEstadoCompartido = { ...datos };
    this.mostrarModal = true;
  }

  // ── Estados de Ayudantía ──────────────────────────────────────────────────
  abrirModalNuevoEstadoAyudantia()                              { this.abrirModalEstadoCompartido(null,  { nombreEstado: '', codigo: '', descripcion: '' }); }
  abrirModalEditarEstadoAyudantia(i: TipoEstadoAyudantiaResponse) { this.abrirModalEstadoCompartido(i.id, { nombreEstado: i.nombre_estado, codigo: i.codigo, descripcion: i.descripcion }); }
  desactivarEstadoAyudantia(i: TipoEstadoAyudantiaResponse) {
    if (!confirm(`¿Desactivar "${i.nombre_estado}"?`)) return;
    this.subs.add(this.tipoEstadoAyudantiaSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estado de ayudantía')
    }));
  }

  // ── Estados de Registro ───────────────────────────────────────────────────
  abrirModalNuevoEstadoRegistro()                               { this.abrirModalEstadoCompartido(null,  { nombreEstado: '', codigo: '', descripcion: '' }); }
  abrirModalEditarEstadoRegistro(i: TipoEstadoRegistroResponse)   { this.abrirModalEstadoCompartido(i.id, { nombreEstado: i.nombre_estado, codigo: i.codigo, descripcion: i.descripcion }); }
  desactivarEstadoRegistro(i: TipoEstadoRegistroResponse) {
    if (!confirm(`¿Desactivar "${i.nombre_estado}"?`)) return;
    this.subs.add(this.tipoEstadoRegistroSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estado de registro')
    }));
  }

  // ── Tipos de Evidencia ────────────────────────────────────────────────────
  abrirModalNuevoEvidencia() { this.isEditMode = false; this.editandoId = null; this.formEvidencia = { nombre: '', extensionPermitida: '', codigo: '' }; this.mostrarModal = true; }
  abrirModalEditarEvidencia(i: TipoEvidenciaResponse) { this.isEditMode = true; this.editandoId = i.id; this.formEvidencia = { nombre: i.nombre, extensionPermitida: i.extension_permitida, codigo: i.codigo }; this.mostrarModal = true; }
  guardarEvidencia() {
    const p = this.isEditMode && this.editandoId
      ? this.tipoEvidenciaSvc.actualizar(this.editandoId, this.formEvidencia as TipoEvidenciaRequest)
      : this.tipoEvidenciaSvc.crear(this.formEvidencia as TipoEvidenciaRequest);
    this.procesarGuardadoMaestro(p, 'Tipo de Evidencia');
  }
  desactivarEvidencia(i: TipoEvidenciaResponse) {
    if (!confirm(`¿Desactivar "${i.nombre}"?`)) return;
    this.subs.add(this.tipoEvidenciaSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'tipo de evidencia')
    }));
  }

  // ── Estados de Evidencia (/api/admin/catalogos-maestros/estados-evidencia) ─
  abrirModalNuevoEstadoEvidencia()                                { this.abrirModalEstadoCompartido(null,  { nombreEstado: '', codigo: '', descripcion: '' }); }
  abrirModalEditarEstadoEvidencia(i: TipoEstadoEvidenciaResponse)  { this.abrirModalEstadoCompartido(i.id, { nombreEstado: i.nombre_estado, codigo: i.codigo, descripcion: i.descripcion }); }
  desactivarEstadoEvidencia(i: TipoEstadoEvidenciaResponse) {
    if (!confirm(`¿Desactivar "${i.nombre_estado}"?`)) return;
    this.subs.add(this.tipoEstadoEvidenciaSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estado de evidencia')
    }));
  }

  // ── Estados de Requisito ──────────────────────────────────────────────────
  abrirModalNuevoEstadoRequisito()                                { this.abrirModalEstadoCompartido(null,  { nombreEstado: '', codigo: '', descripcion: '' }); }
  abrirModalEditarEstadoRequisito(i: TipoEstadoRequisitoResponse)  { this.abrirModalEstadoCompartido(i.id, { nombreEstado: i.nombre_estado, codigo: i.codigo, descripcion: i.descripcion }); }
  desactivarEstadoRequisito(i: TipoEstadoRequisitoResponse) {
    if (!confirm(`¿Desactivar "${i.nombre_estado}"?`)) return;
    this.subs.add(this.tipoEstadoRequisitoSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estado de requisito')
    }));
  }


  guardarEstadoGenerico(): void {
    const payload = this.formEstadoCompartido;
    let p: any;
    let entidad: string;

    if (this.activeTab === 'estadosAyudantia') {
      p       = this.isEditMode && this.editandoId ? this.tipoEstadoAyudantiaSvc.actualizar(this.editandoId, payload as TipoEstadoAyudantiaRequest)   : this.tipoEstadoAyudantiaSvc.crear(payload as TipoEstadoAyudantiaRequest);
      entidad = 'Estado de Ayudantía';
    } else if (this.activeTab === 'estadosRegistro') {
      p       = this.isEditMode && this.editandoId ? this.tipoEstadoRegistroSvc.actualizar(this.editandoId, payload as TipoEstadoRegistroRequest)     : this.tipoEstadoRegistroSvc.crear(payload as TipoEstadoRegistroRequest);
      entidad = 'Estado de Registro';
    } else if (this.activeTab === 'estadosEvidenciaMaestro') {
      p       = this.isEditMode && this.editandoId ? this.tipoEstadoEvidenciaSvc.actualizar(this.editandoId, payload as TipoEstadoEvidenciaRequest)   : this.tipoEstadoEvidenciaSvc.crear(payload as TipoEstadoEvidenciaRequest);
      entidad = 'Estado de Evidencia';
    } else if (this.activeTab === 'estadosRequisito') {
      p       = this.isEditMode && this.editandoId ? this.tipoEstadoRequisitoSvc.actualizar(this.editandoId, payload as TipoEstadoRequisitoRequest)   : this.tipoEstadoRequisitoSvc.crear(payload as TipoEstadoRequisitoRequest);
      entidad = 'Estado de Requisito';
    } else {
      return; // Tab inesperado; no hacer nada
    }

    this.procesarGuardadoMaestro(p, entidad);
  }

  // ── Tipos de Fase ─────────────────────────────────────────────────────────
  abrirModalNuevoFase() { this.isEditMode = false; this.editandoId = null; this.formFase = { codigo: '', nombre: '', descripcion: '', orden: 1 }; this.mostrarModal = true; }
  abrirModalEditarFase(i: TipoFaseResponse) { this.isEditMode = true; this.editandoId = i.id; this.formFase = { codigo: i.codigo, nombre: i.nombre, descripcion: i.descripcion, orden: i.orden }; this.mostrarModal = true; }
  guardarFase() {
    const p = this.isEditMode && this.editandoId
      ? this.tipoFaseSvc.actualizar(this.editandoId, this.formFase as TipoFaseRequest)
      : this.tipoFaseSvc.crear(this.formFase as TipoFaseRequest);
    this.procesarGuardadoMaestro(p, 'Tipo de Fase');
  }
  desactivarFase(i: TipoFaseResponse) {
    if (!confirm(`¿Desactivar la fase "${i.nombre}"?`)) return;
    this.subs.add(this.tipoFaseSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'tipo de fase')
    }));
  }

  // ── Estados de Postulación ────────────────────────────────────────────────
  abrirModalNuevoEstadoPostulacion() { this.isEditMode = false; this.editandoId = null; this.formEstadoPostulacion = { codigo: '', nombre: '', descripcion: '' }; this.mostrarModal = true; }
  abrirModalEditarEstadoPostulacion(i: TipoEstadoPostulacionResponse) { this.isEditMode = true; this.editandoId = i.id; this.formEstadoPostulacion = { codigo: i.codigo, nombre: i.nombre, descripcion: i.descripcion }; this.mostrarModal = true; }
  guardarEstadoPostulacion() {
    const p = this.isEditMode && this.editandoId
      ? this.tipoEstadoPostulacionSvc.actualizar(this.editandoId, this.formEstadoPostulacion as TipoEstadoPostulacionRequest)
      : this.tipoEstadoPostulacionSvc.crear(this.formEstadoPostulacion as TipoEstadoPostulacionRequest);
    this.procesarGuardadoMaestro(p, 'Estado de Postulación');
  }
  desactivarEstadoPostulacion(i: TipoEstadoPostulacionResponse) {
    if (!confirm(`¿Desactivar "${i.nombre}"?`)) return;
    this.subs.add(this.tipoEstadoPostulacionSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'estado de postulación')
    }));
  }

  // ── Privilegios ───────────────────────────────────────────────────────────
  abrirModalNuevoPrivilegio() { this.isEditMode = false; this.editandoId = null; this.formPrivilegio = { nombrePrivilegio: '', codigoInterno: '', descripcion: '' }; this.mostrarModal = true; }
  abrirModalEditarPrivilegio(i: PrivilegioResponse) { this.isEditMode = true; this.editandoId = i.id; this.formPrivilegio = { nombrePrivilegio: i.nombre_privilegio, codigoInterno: i.codigo_interno, descripcion: i.descripcion }; this.mostrarModal = true; }
  guardarPrivilegio() {
    const p = this.isEditMode && this.editandoId
      ? this.privilegioSvc.actualizar(this.editandoId, this.formPrivilegio as PrivilegioRequest)
      : this.privilegioSvc.crear(this.formPrivilegio as PrivilegioRequest);
    this.procesarGuardadoMaestro(p, 'Privilegio');
  }
  desactivarPrivilegio(i: PrivilegioResponse) {
    if (!confirm(`¿Desactivar "${i.nombre_privilegio}"?`)) return;
    this.subs.add(this.privilegioSvc.desactivar(i.id).subscribe({
      next: r => r.exito ? this.cargarDatosActivos() : alert(r.mensaje),
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, 'privilegio')
    }));
  }

  // =========================================================================
  // Helpers privados
  // =========================================================================

  /** Servicios legacy: el error de negocio llega como excepción HTTP */
  private procesarGuardadoLegacy(peticion: any, entidad: string, callback?: () => void): void {
    this.subs.add(peticion.subscribe({
      next: () => {
        alert(`${entidad} ${this.isEditMode ? 'actualizado(a)' : 'registrado(a)'} correctamente.`);
        this.mostrarModal = false;
        this.cargarDatosActivos();
        callback?.();
      },
      error: (e: HttpErrorResponse) => {
        if (e.error?.errors) alert(`Por favor corrige:\n - ${Object.values(e.error.errors).join('\n - ')}`);
        else alert(e.error?.message || 'Ocurrió un error inesperado.');
      }
    }));
  }

  private procesarGuardadoMaestro(peticion: any, entidad: string): void {
    this.subs.add(peticion.subscribe({
      next: (response: StandardModificacionResponse) => {
        if (!response.exito) { alert(`No se pudo guardar: ${response.mensaje}`); return; }
        alert(`${entidad} ${this.isEditMode ? 'actualizado(a)' : 'registrado(a)'} correctamente.`);
        this.mostrarModal = false;
        this.cargarDatosActivos();
      },
      error: (e: HttpErrorResponse) => this.manejarErrorHttp(e, entidad)
    }));
  }

  private manejarErrorHttp(e: HttpErrorResponse, contexto: string): void {
    console.error(`Error HTTP [${contexto}]`, e);
    alert(e.error?.message || `Error de conexión al cargar ${contexto}.`);
    this.loading = false;
  }
}
