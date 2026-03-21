import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription, forkJoin } from 'rxjs';

import { CargaAcademicaService } from '../../../core/services/configuracion/carga-academica-service';
import {
  AsignaturaJerarquiaDTO,
  DocenteActivoDTO,
  SincronizarCargaResponse,
} from '../../../core/models/configuracion/CargaAcademica';

type Vista = 'seleccion' | 'gestion';

@Component({
  selector: 'app-carga-academica',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './carga-academica.html',
  styleUrls: ['./carga-academica.css', '../admin-shared.css'],
})
export class CargaAcademicaComponent implements OnInit, OnDestroy {

  private svc  = inject(CargaAcademicaService);
  private subs = new Subscription();

  vista: Vista = 'seleccion';

  docentes:    DocenteActivoDTO[]       = [];
  asignaturas: AsignaturaJerarquiaDTO[] = [];

  docenteSeleccionado: DocenteActivoDTO | null = null;
  cargaActual: AsignaturaJerarquiaDTO[] = [];

  cargandoDocentes    = true;
  cargandoPanel       = false;
  cargandoAsignaturas = true;
  guardando           = false;
  resultado: SincronizarCargaResponse | null = null;

  busquedaDocente  = '';
  filtroFacultad   = 0;
  filtroCarrera    = 0;
  filtroAsignatura = '';

  get facultades(): { id: number; nombre: string }[] {
    const mapa = new Map<number, string>();
    this.asignaturas.forEach(a => mapa.set(a.idFacultad, a.nombreFacultad));
    return [...mapa.entries()]
      .map(([id, nombre]) => ({ id, nombre }))
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  get carreras(): { id: number; nombre: string }[] {
    const fuente = this.filtroFacultad
      ? this.asignaturas.filter(a => a.idFacultad === this.filtroFacultad)
      : this.asignaturas;
    const mapa = new Map<number, string>();
    fuente.forEach(a => mapa.set(a.idCarrera, a.nombreCarrera));
    return [...mapa.entries()]
      .map(([id, nombre]) => ({ id, nombre }))
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  get docentesFiltrados(): DocenteActivoDTO[] {
    const t = this.busquedaDocente.toLowerCase().trim();
    if (!t) return this.docentes;
    return this.docentes.filter(d =>
      d.nombres.toLowerCase().includes(t) ||
      d.apellidos.toLowerCase().includes(t) ||
      d.cedula.includes(t)
    );
  }

  get asignaturasFiltradas(): AsignaturaJerarquiaDTO[] {
    return this.asignaturas.filter(a => {
      const porFacultad = !this.filtroFacultad  || a.idFacultad === this.filtroFacultad;
      const porCarrera  = !this.filtroCarrera   || a.idCarrera  === this.filtroCarrera;
      const porNombre   = !this.filtroAsignatura ||
        a.nombreAsignatura.toLowerCase().includes(this.filtroAsignatura.toLowerCase().trim());
      const noEnCarga   = !this.cargaActual.some(c => c.idAsignatura === a.idAsignatura);
      return porFacultad && porCarrera && porNombre && noEnCarga;
    });
  }

  ngOnInit() {
    this.subs.add(
      forkJoin({
        docentes:    this.svc.getDocentes(),
        asignaturas: this.svc.getAsignaturas(),
      }).subscribe({
        next: ({ docentes, asignaturas }) => {
          this.docentes            = docentes    ?? [];
          this.asignaturas         = asignaturas ?? [];
          this.cargandoDocentes    = false;
          this.cargandoAsignaturas = false;
        },
        error: () => {
          this.cargandoDocentes    = false;
          this.cargandoAsignaturas = false;
        },
      })
    );
  }

  ngOnDestroy() { this.subs.unsubscribe(); }

  seleccionarDocente(docente: DocenteActivoDTO) {
    this.docenteSeleccionado = docente;
    this.cargaActual         = [];
    this.resultado           = null;
    this.resetFiltros();
    this.vista               = 'gestion';
    this.cargandoPanel       = true;

    this.subs.add(
      this.svc.getAsignaturasDocente(docente.idDocente).subscribe({
        next:  (data) => { this.cargaActual = data ?? []; this.cargandoPanel = false; },
        error: ()     => { this.cargandoPanel = false; },
      })
    );
  }

  volverASeleccion() {
    this.docenteSeleccionado = null;
    this.vista = 'seleccion';
  }

  agregarAsignatura(a: AsignaturaJerarquiaDTO) {
    if (this.cargaActual.some(c => c.idAsignatura === a.idAsignatura)) return;
    this.cargaActual = [...this.cargaActual, a];
  }

  revocarAsignatura(a: AsignaturaJerarquiaDTO) {
    this.cargaActual = this.cargaActual.filter(c => c.idAsignatura !== a.idAsignatura);
  }

  resetFiltros() {
    this.filtroFacultad   = 0;
    this.filtroCarrera    = 0;
    this.filtroAsignatura = '';
  }

  onFacultadChange() { this.filtroCarrera = 0; }

  guardarCambios() {
    if (!this.docenteSeleccionado) return;
    const nombre = `${this.docenteSeleccionado.nombres} ${this.docenteSeleccionado.apellidos}`;
    if (!confirm(`¿Confirmar cambios en la carga de ${nombre}?`)) return;

    this.guardando = true;
    this.resultado = null;

    this.subs.add(
      this.svc.sincronizar({
        idDocente:      this.docenteSeleccionado.idDocente,
        asignaturasIds: this.cargaActual.map(a => a.idAsignatura),
      }).subscribe({
        next: (res) => {
          this.guardando = false;
          this.resultado = res;
          const idx = this.docentes.findIndex(d => d.idDocente === this.docenteSeleccionado!.idDocente);
          if (idx !== -1) {
            this.docentes[idx] = { ...this.docentes[idx], totalAsignaturas: this.cargaActual.length };
            this.docenteSeleccionado = { ...this.docenteSeleccionado!, totalAsignaturas: this.cargaActual.length };
          }
        },
        error: (err: HttpErrorResponse) => {
          this.guardando = false;
          alert(`Error: ${err.error?.message ?? 'No se pudo sincronizar.'}`);
        },
      })
    );
  }
}
