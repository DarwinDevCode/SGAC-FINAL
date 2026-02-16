import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
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

  cargando = signal(false);
  tabActiva = signal<'facultades' | 'carreras' | 'asignaturas' | 'periodos'>('facultades');

  facultades: Facultad[] = [];
  carreras: Carrera[] = [];
  asignaturas: AsignaturaCatalogo[] = [];
  periodos: PeriodoCatalogo[] = [];

  nuevaFacultad = '';
  nuevaCarrera: CarreraCatalogoRequest = { idFacultad: 0, nombreCarrera: '' };
  nuevaAsignatura: AsignaturaCatalogoRequest = { idCarrera: 0, nombreAsignatura: '', semestre: 1 };
  nuevoPeriodo: PeriodoCatalogoRequest = {
    nombrePeriodo: '',
    fechaInicio: '',
    fechaFin: '',
    estado: 'ACTIVO'
  };

  ngOnInit() {
    this.cargarCatalogos();
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }

  setTab(tab: 'facultades' | 'carreras' | 'asignaturas' | 'periodos') {
    this.tabActiva.set(tab);
  }

  cargarCatalogos() {
    this.cargando.set(true);

    this.subs.add(this.usuarioService.listarFacultadesCatalogo().subscribe(data => this.facultades = data || []));
    this.subs.add(this.usuarioService.listarCarrerasCatalogo().subscribe(data => this.carreras = data || []));
    this.subs.add(this.usuarioService.listarAsignaturasCatalogo().subscribe(data => this.asignaturas = data || []));
    this.subs.add(this.usuarioService.listarPeriodosCatalogo().subscribe({
      next: (data) => {
        this.periodos = data || [];
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    }));
  }

  crearFacultad() {
    if (!this.nuevaFacultad.trim()) return;
    const payload: FacultadCatalogoRequest = { nombreFacultad: this.nuevaFacultad.trim() };
    this.subs.add(this.usuarioService.crearFacultadCatalogo(payload).subscribe({
      next: () => {
        this.nuevaFacultad = '';
        this.cargarCatalogos();
      },
      error: () => alert('No se pudo crear la facultad')
    }));
  }

  editarFacultad(facultad: Facultad) {
    const nombre = prompt('Nuevo nombre de facultad:', facultad.nombreFacultad);
    if (!nombre || !nombre.trim()) return;
    this.subs.add(this.usuarioService.actualizarFacultadCatalogo(facultad.idFacultad, { nombreFacultad: nombre.trim() }).subscribe({
      next: () => this.cargarCatalogos(),
      error: () => alert('No se pudo actualizar la facultad')
    }));
  }

  desactivarFacultad(facultad: Facultad) {
    if (!confirm(`¿Desactivar facultad ${facultad.nombreFacultad}?`)) return;
    this.subs.add(this.usuarioService.desactivarFacultadCatalogo(facultad.idFacultad).subscribe(() => this.cargarCatalogos()));
  }

  crearCarrera() {
    if (!this.nuevaCarrera.idFacultad || !this.nuevaCarrera.nombreCarrera.trim()) return;
    this.subs.add(this.usuarioService.crearCarreraCatalogo({
      idFacultad: this.nuevaCarrera.idFacultad,
      nombreCarrera: this.nuevaCarrera.nombreCarrera.trim()
    }).subscribe({
      next: () => {
        this.nuevaCarrera = { idFacultad: 0, nombreCarrera: '' };
        this.cargarCatalogos();
      },
      error: () => alert('No se pudo crear la carrera')
    }));
  }

  editarCarrera(carrera: Carrera) {
    const nombre = prompt('Nuevo nombre de carrera:', carrera.nombreCarrera);
    if (!nombre || !nombre.trim()) return;
    this.subs.add(this.usuarioService.actualizarCarreraCatalogo(carrera.idCarrera, {
      idFacultad: carrera.idFacultad,
      nombreCarrera: nombre.trim()
    }).subscribe({
      next: () => this.cargarCatalogos(),
      error: () => alert('No se pudo actualizar la carrera')
    }));
  }

  desactivarCarrera(carrera: Carrera) {
    if (!confirm(`¿Desactivar carrera ${carrera.nombreCarrera}?`)) return;
    this.subs.add(this.usuarioService.desactivarCarreraCatalogo(carrera.idCarrera).subscribe(() => this.cargarCatalogos()));
  }

  crearAsignatura() {
    if (!this.nuevaAsignatura.idCarrera || !this.nuevaAsignatura.nombreAsignatura.trim()) return;
    this.subs.add(this.usuarioService.crearAsignaturaCatalogo({
      idCarrera: this.nuevaAsignatura.idCarrera,
      nombreAsignatura: this.nuevaAsignatura.nombreAsignatura.trim(),
      semestre: this.nuevaAsignatura.semestre
    }).subscribe({
      next: () => {
        this.nuevaAsignatura = { idCarrera: 0, nombreAsignatura: '', semestre: 1 };
        this.cargarCatalogos();
      },
      error: () => alert('No se pudo crear la asignatura')
    }));
  }

  editarAsignatura(asignatura: AsignaturaCatalogo) {
    const nombre = prompt('Nuevo nombre de asignatura:', asignatura.nombreAsignatura);
    if (!nombre || !nombre.trim()) return;
    this.subs.add(this.usuarioService.actualizarAsignaturaCatalogo(asignatura.idAsignatura, {
      idCarrera: asignatura.idCarrera,
      nombreAsignatura: nombre.trim(),
      semestre: asignatura.semestre
    }).subscribe({
      next: () => this.cargarCatalogos(),
      error: () => alert('No se pudo actualizar la asignatura')
    }));
  }

  desactivarAsignatura(asignatura: AsignaturaCatalogo) {
    if (!confirm(`¿Desactivar asignatura ${asignatura.nombreAsignatura}?`)) return;
    this.subs.add(this.usuarioService.desactivarAsignaturaCatalogo(asignatura.idAsignatura).subscribe(() => this.cargarCatalogos()));
  }

  crearPeriodo() {
    if (!this.nuevoPeriodo.nombrePeriodo.trim() || !this.nuevoPeriodo.fechaInicio || !this.nuevoPeriodo.fechaFin) return;
    this.subs.add(this.usuarioService.crearPeriodoCatalogo({ ...this.nuevoPeriodo, nombrePeriodo: this.nuevoPeriodo.nombrePeriodo.trim() }).subscribe({
      next: () => {
        this.nuevoPeriodo = { nombrePeriodo: '', fechaInicio: '', fechaFin: '', estado: 'ACTIVO' };
        this.cargarCatalogos();
      },
      error: () => alert('No se pudo crear el período académico')
    }));
  }

  editarPeriodo(periodo: PeriodoCatalogo) {
    const nombre = prompt('Nuevo nombre del período:', periodo.nombrePeriodo);
    if (!nombre || !nombre.trim()) return;
    this.subs.add(this.usuarioService.actualizarPeriodoCatalogo(periodo.idPeriodoAcademico, {
      nombrePeriodo: nombre.trim(),
      fechaInicio: periodo.fechaInicio,
      fechaFin: periodo.fechaFin,
      estado: periodo.estado
    }).subscribe({
      next: () => this.cargarCatalogos(),
      error: () => alert('No se pudo actualizar el período')
    }));
  }

  desactivarPeriodo(periodo: PeriodoCatalogo) {
    if (!confirm(`¿Desactivar período ${periodo.nombrePeriodo}?`)) return;
    this.subs.add(this.usuarioService.desactivarPeriodoCatalogo(periodo.idPeriodoAcademico).subscribe(() => this.cargarCatalogos()));
  }
}
