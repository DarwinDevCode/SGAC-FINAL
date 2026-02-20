import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, finalize, forkJoin } from 'rxjs';
import {
  AsignaturaCatalogo,
  AsignaturaCatalogoRequest,
  Carrera,
  CarreraCatalogoRequest,
  Facultad,
  FacultadCatalogoRequest,
  PeriodoCatalogo,
  PeriodoCatalogoRequest,
  UsuarioService
} from '../../../core/services/usuario';

import {AsignaturaDTO} from '../../../core/dto/Asignatura';
import {AsignaturaService} from '../../../core/services/asignatura';


@Component({
  selector: 'app-gestion-catalogos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-catalogos.html',
  styleUrl: './gestion-catalogos.css'
})
export class GestionCatalogosComponent implements OnInit, OnDestroy {
  private usuarioService = inject(UsuarioService);
  private subs = new Subscription();

  loading = signal(false);
  guardando = signal(false);
  busqueda = signal('');
  tabActiva = signal<'facultades' | 'carreras' | 'asignaturas' | 'periodos'>('facultades');

  facultades: Facultad[] = [];
  carreras: Carrera[] = [];
  asignaturas: AsignaturaCatalogo[] = [];
  periodos: PeriodoCatalogo[] = [];

  editandoFacultadId: number | null = null;
  editandoCarreraId: number | null = null;
  editandoAsignaturaId: number | null = null;
  editandoPeriodoId: number | null = null;

  facultadForm: FacultadCatalogoRequest = { nombreFacultad: '' };
  carreraForm: CarreraCatalogoRequest = { idFacultad: 0, nombreCarrera: '' };
  asignaturaForm: AsignaturaCatalogoRequest = { idCarrera: 0, nombreAsignatura: '', semestre: 1 };
  periodoForm: PeriodoCatalogoRequest = { nombrePeriodo: '', fechaInicio: '', fechaFin: '', estado: 'ACTIVO' };

  private accionesEnCurso = new Set<string>();

  ngOnInit(): void {
    this.cargarCatalogos();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  setTab(tab: 'facultades' | 'carreras' | 'asignaturas' | 'periodos'): void {
    this.tabActiva.set(tab);
    this.busqueda.set('');
  }

  filtrar(texto: string): void {
    this.busqueda.set(texto);
  }

  cargarCatalogos(): void {
    this.loading.set(true);

    this.subs.add(
      forkJoin({
        facultades: this.usuarioService.listarFacultadesCatalogo(),
        carreras: this.usuarioService.listarCarrerasCatalogo(),
        asignaturas: this.usuarioService.listarAsignaturasCatalogo(),
        periodos: this.usuarioService.listarPeriodosCatalogo()
      }).subscribe({
        next: ({ facultades, carreras, asignaturas, periodos }) => {
          this.facultades = facultades || [];
          this.carreras = carreras || [];
          this.asignaturas = asignaturas || [];
          this.periodos = periodos || [];
          this.loading.set(false);
        },
        error: (error) => {
          this.loading.set(false);
          alert(this.obtenerMensajeError(error, 'No se pudieron cargar los catálogos'));
        }
      })
    );
  }


  private normalizarTexto(valor: string | null | undefined): string {
    return (valor || '').trim().toLowerCase();
  }

  getFacultadesFiltradas(): Facultad[] {
    const term = this.busqueda().toLowerCase().trim();
    if (!term) return this.facultades;
    return this.facultades.filter((f) => f.nombreFacultad.toLowerCase().includes(term));
  }

  getCarrerasFiltradas(): Carrera[] {
    const term = this.busqueda().toLowerCase().trim();
    if (!term) return this.carreras;
    return this.carreras.filter((c) =>
      c.nombreCarrera.toLowerCase().includes(term) ||
      c.nombreFacultad.toLowerCase().includes(term)
    );
  }

  getAsignaturasFiltradas(): AsignaturaCatalogo[] {
    const term = this.busqueda().toLowerCase().trim();
    if (!term) return this.asignaturas;
    return this.asignaturas.filter((a) =>
      a.nombreAsignatura.toLowerCase().includes(term) ||
      a.nombreCarrera.toLowerCase().includes(term)
    );
  }

  getPeriodosFiltrados(): PeriodoCatalogo[] {
    const term = this.busqueda().toLowerCase().trim();
    if (!term) return this.periodos;
    return this.periodos.filter((p) => p.nombrePeriodo.toLowerCase().includes(term) || p.estado.toLowerCase().includes(term));
  }

  guardarFacultad(): void {
    if (this.guardando()) return;

    if (!this.facultadForm.nombreFacultad.trim()) {
      alert('Ingrese el nombre de la facultad.');
      return;
    }

    const payload: FacultadCatalogoRequest = { nombreFacultad: this.facultadForm.nombreFacultad.trim() };

    if (this.editandoFacultadId) {
      const original = this.facultades.find((f) => f.idFacultad === this.editandoFacultadId);
      if (original && this.normalizarTexto(original.nombreFacultad) === this.normalizarTexto(payload.nombreFacultad)) {
        alert('No hay cambios para actualizar en la facultad.');
        return;
      }
    }

    const request$ = this.editandoFacultadId
      ? this.usuarioService.actualizarFacultadCatalogo(this.editandoFacultadId, payload)
      : this.usuarioService.crearFacultadCatalogo(payload);

    this.guardando.set(true);
    this.subs.add(
      request$.subscribe({
        next: (facultad) => {
          if (this.editandoFacultadId) {
            this.facultades = this.facultades.map((f) => (f.idFacultad === facultad.idFacultad ? facultad : f));
          } else {
            this.facultades = [...this.facultades, facultad];
            if (!this.carreraForm.idFacultad) this.carreraForm.idFacultad = facultad.idFacultad;
          }
          this.cancelarEdicionFacultad();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo guardar la facultad (si no cambió el nombre, no es necesario actualizar).'))
      }).add(() => this.guardando.set(false))
    );
  }

  private claveAccion(tipo: string, id: number): string {
    return `${tipo}-${id}`;
  }

  estaProcesando(tipo: 'facultad' | 'carrera' | 'asignatura' | 'periodo', id: number): boolean {
    return this.accionesEnCurso.has(this.claveAccion(tipo, id));
  }

  private iniciarAccion(tipo: 'facultad' | 'carrera' | 'asignatura' | 'periodo', id: number): boolean {
    const key = this.claveAccion(tipo, id);
    if (this.accionesEnCurso.has(key)) return false;
    this.accionesEnCurso.add(key);
    return true;
  }

  private finalizarAccion(tipo: 'facultad' | 'carrera' | 'asignatura' | 'periodo', id: number): void {
    this.accionesEnCurso.delete(this.claveAccion(tipo, id));
  }

  cargarFacultadEnFormulario(facultad: Facultad): void {
    this.editandoFacultadId = facultad.idFacultad;
    this.facultadForm = { nombreFacultad: facultad.nombreFacultad };
  }

  cancelarEdicionFacultad(): void {
    this.editandoFacultadId = null;
    this.facultadForm = { nombreFacultad: '' };
  }

  desactivarFacultad(facultad: Facultad): void {
    if (!confirm(`¿Seguro que desea desactivar la facultad ${facultad.nombreFacultad}?`)) return;
    if (!this.iniciarAccion('facultad', facultad.idFacultad)) return;

    this.subs.add(
      this.usuarioService.desactivarFacultadCatalogo(facultad.idFacultad).pipe(
        finalize(() => this.finalizarAccion('facultad', facultad.idFacultad))
      ).subscribe({
        next: () => {
          this.facultades = this.facultades.filter((f) => f.idFacultad !== facultad.idFacultad);
          if (this.carreraForm.idFacultad === facultad.idFacultad) this.carreraForm.idFacultad = 0;
          if (this.editandoFacultadId === facultad.idFacultad) this.cancelarEdicionFacultad();
          this.cargarCatalogos();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo desactivar la facultad'))
      })
    );
  }

  guardarCarrera(): void {
    if (this.guardando()) return;

    if (!this.carreraForm.idFacultad || !this.carreraForm.nombreCarrera.trim()) {
      alert('Seleccione facultad e ingrese nombre de carrera.');
      return;
    }

    const payload: CarreraCatalogoRequest = {
      idFacultad: this.carreraForm.idFacultad,
      nombreCarrera: this.carreraForm.nombreCarrera.trim()
    };

    const request$ = this.editandoCarreraId
      ? this.usuarioService.actualizarCarreraCatalogo(this.editandoCarreraId, payload)
      : this.usuarioService.crearCarreraCatalogo(payload);

    this.guardando.set(true);
    this.subs.add(
      request$.subscribe({
        next: (carrera) => {
          if (this.editandoCarreraId) {
            this.carreras = this.carreras.map((c) => (c.idCarrera === carrera.idCarrera ? carrera : c));
          } else {
            this.carreras = [...this.carreras, carrera];
            if (!this.asignaturaForm.idCarrera) this.asignaturaForm.idCarrera = carrera.idCarrera;
          }
          this.cancelarEdicionCarrera();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo guardar la carrera'))
      }).add(() => this.guardando.set(false))
    );
  }

  cargarCarreraEnFormulario(carrera: Carrera): void {
    this.editandoCarreraId = carrera.idCarrera;
    this.carreraForm = {
      idFacultad: carrera.idFacultad,
      nombreCarrera: carrera.nombreCarrera
    };
  }

  cancelarEdicionCarrera(): void {
    this.editandoCarreraId = null;
    this.carreraForm = { idFacultad: 0, nombreCarrera: '' };
  }

  desactivarCarrera(carrera: Carrera): void {
    if (!confirm(`¿Seguro que desea desactivar la carrera ${carrera.nombreCarrera}?`)) return;
    if (!this.iniciarAccion('carrera', carrera.idCarrera)) return;

    this.subs.add(
      this.usuarioService.desactivarCarreraCatalogo(carrera.idCarrera).pipe(
        finalize(() => this.finalizarAccion('carrera', carrera.idCarrera))
      ).subscribe({
        next: () => {
          this.carreras = this.carreras.filter((c) => c.idCarrera !== carrera.idCarrera);
          if (this.asignaturaForm.idCarrera === carrera.idCarrera) this.asignaturaForm.idCarrera = 0;
          if (this.editandoCarreraId === carrera.idCarrera) this.cancelarEdicionCarrera();
          this.cargarCatalogos();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo desactivar la carrera'))
      })
    );
  }

  guardarAsignatura(): void {
    if (this.guardando()) return;

    if (!this.asignaturaForm.idCarrera || !this.asignaturaForm.nombreAsignatura.trim() || this.asignaturaForm.semestre < 1) {
      alert('Complete carrera, nombre y semestre válidos para la asignatura.');
      return;
    }

    const payload: AsignaturaCatalogoRequest = {
      idCarrera: this.asignaturaForm.idCarrera,
      nombreAsignatura: this.asignaturaForm.nombreAsignatura.trim(),
      semestre: this.asignaturaForm.semestre
    };

    const request$ = this.editandoAsignaturaId
      ? this.usuarioService.actualizarAsignaturaCatalogo(this.editandoAsignaturaId, payload)
      : this.usuarioService.crearAsignaturaCatalogo(payload);

    this.guardando.set(true);
    this.subs.add(
      request$.subscribe({
        next: (asignatura) => {
          if (this.editandoAsignaturaId) {
            this.asignaturas = this.asignaturas.map((a) => (a.idAsignatura === asignatura.idAsignatura ? asignatura : a));
          } else {
            this.asignaturas = [...this.asignaturas, asignatura];
          }
          this.cancelarEdicionAsignatura();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo guardar la asignatura'))
      }).add(() => this.guardando.set(false))
    );
  }

  cargarAsignaturaEnFormulario(asignatura: AsignaturaCatalogo): void {
    this.editandoAsignaturaId = asignatura.idAsignatura;
    this.asignaturaForm = {
      idCarrera: asignatura.idCarrera,
      nombreAsignatura: asignatura.nombreAsignatura,
      semestre: asignatura.semestre
    };
  }

  cancelarEdicionAsignatura(): void {
    this.editandoAsignaturaId = null;
    this.asignaturaForm = { idCarrera: 0, nombreAsignatura: '', semestre: 1 };
  }

  desactivarAsignatura(asignatura: AsignaturaCatalogo): void {
    if (!confirm(`¿Seguro que desea desactivar la asignatura ${asignatura.nombreAsignatura}?`)) return;
    if (!this.iniciarAccion('asignatura', asignatura.idAsignatura)) return;

    this.subs.add(
      this.usuarioService.desactivarAsignaturaCatalogo(asignatura.idAsignatura).pipe(
        finalize(() => this.finalizarAccion('asignatura', asignatura.idAsignatura))
      ).subscribe({
        next: () => {
          this.asignaturas = this.asignaturas.filter((a) => a.idAsignatura !== asignatura.idAsignatura);
          if (this.editandoAsignaturaId === asignatura.idAsignatura) this.cancelarEdicionAsignatura();
          this.cargarCatalogos();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo desactivar la asignatura'))
      })
    );
  }

  guardarPeriodo(): void {
    if (this.guardando()) return;

    if (!this.periodoForm.nombrePeriodo.trim() || !this.periodoForm.fechaInicio || !this.periodoForm.fechaFin) {
      alert('Complete nombre y fechas del período académico.');
      return;
    }

    const payload: PeriodoCatalogoRequest = {
      ...this.periodoForm,
      nombrePeriodo: this.periodoForm.nombrePeriodo.trim()
    };

    if (this.editandoPeriodoId) {
      const original = this.periodos.find((p) => p.idPeriodoAcademico === this.editandoPeriodoId);
      if (
        original &&
        this.normalizarTexto(original.nombrePeriodo) === this.normalizarTexto(payload.nombrePeriodo) &&
        original.fechaInicio === payload.fechaInicio &&
        original.fechaFin === payload.fechaFin &&
        this.normalizarTexto(original.estado) === this.normalizarTexto(payload.estado)
      ) {
        alert('No hay cambios para actualizar en el período académico.');
        return;
      }
    }

    const request$ = this.editandoPeriodoId
      ? this.usuarioService.actualizarPeriodoCatalogo(this.editandoPeriodoId, payload)
      : this.usuarioService.crearPeriodoCatalogo(payload);

    this.guardando.set(true);
    this.subs.add(
      request$.subscribe({
        next: (periodo) => {
          if (this.editandoPeriodoId) {
            this.periodos = this.periodos.map((p) => (p.idPeriodoAcademico === periodo.idPeriodoAcademico ? periodo : p));
          } else {
            this.periodos = [...this.periodos, periodo];
          }
          this.cancelarEdicionPeriodo();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo guardar el período académico (si no cambió, no es necesario actualizar).'))
      }).add(() => this.guardando.set(false))
    );
  }

  cargarPeriodoEnFormulario(periodo: PeriodoCatalogo): void {
    this.editandoPeriodoId = periodo.idPeriodoAcademico;
    this.periodoForm = {
      nombrePeriodo: periodo.nombrePeriodo,
      fechaInicio: periodo.fechaInicio,
      fechaFin: periodo.fechaFin,
      estado: periodo.estado
    };
  }

  cancelarEdicionPeriodo(): void {
    this.editandoPeriodoId = null;
    this.periodoForm = { nombrePeriodo: '', fechaInicio: '', fechaFin: '', estado: 'ACTIVO' };
  }

  desactivarPeriodo(periodo: PeriodoCatalogo): void {
    if (!confirm(`¿Seguro que desea desactivar el período ${periodo.nombrePeriodo}?`)) return;
    if (!this.iniciarAccion('periodo', periodo.idPeriodoAcademico)) return;

    this.subs.add(
      this.usuarioService.desactivarPeriodoCatalogo(periodo.idPeriodoAcademico).pipe(
        finalize(() => this.finalizarAccion('periodo', periodo.idPeriodoAcademico))
      ).subscribe({
        next: () => {
          this.periodos = this.periodos.filter((p) => p.idPeriodoAcademico !== periodo.idPeriodoAcademico);
          if (this.editandoPeriodoId === periodo.idPeriodoAcademico) this.cancelarEdicionPeriodo();
          this.cargarCatalogos();
        },
        error: (error) => alert(this.obtenerMensajeError(error, 'No se pudo desactivar el período académico'))
      })
    );
  }

  private obtenerMensajeError(error: any, fallback: string): string {
    return error?.error?.message || error?.error?.mensaje || error?.message || fallback;
  }







}
