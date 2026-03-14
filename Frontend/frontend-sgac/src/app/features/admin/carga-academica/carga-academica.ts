import {
  Component, OnInit, OnDestroy, inject, computed, signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';

import { CargaAcademicaService } from '../../../core/services/configuracion/carga-academica-service'
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
  styleUrl: './carga-academica.css',
})
export class CargaAcademicaComponent implements OnInit, OnDestroy {

  private svc  = inject(CargaAcademicaService);
  private subs = new Subscription();

  // ── Vista activa ─────────────────────────────────────────────
  vista: Vista = 'seleccion';

  // ── Datos maestros ───────────────────────────────────────────
  docentes:    DocenteActivoDTO[]       = [];
  asignaturas: AsignaturaJerarquiaDTO[] = [];

  // ── Selección ────────────────────────────────────────────────
  docenteSeleccionado: DocenteActivoDTO | null = null;

  /** Carga en edición (lado izquierdo del panel) */
  cargaActual: AsignaturaJerarquiaDTO[] = [];

  // ── Feedback ─────────────────────────────────────────────────
  cargandoDocentes    = true;
  cargandoAsignaturas = true;
  guardando           = false;
  resultado: SincronizarCargaResponse | null = null;

  // ── Buscadores ───────────────────────────────────────────────
  /** Búsqueda en la tabla de docentes */
  busquedaDocente = '';

  /** Filtros del catálogo de asignaturas (lado derecho) */
  filtroFacultad   = 0;
  filtroCarrera    = 0;
  filtroAsignatura = '';

  // ── Catálogos derivados ──────────────────────────────────────
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
      const porFacultad  = !this.filtroFacultad   || a.idFacultad === this.filtroFacultad;
      const porCarrera   = !this.filtroCarrera    || a.idCarrera  === this.filtroCarrera;
      const porNombre    = !this.filtroAsignatura ||
        a.nombreAsignatura.toLowerCase().includes(this.filtroAsignatura.toLowerCase().trim());
      const noEnCarga    = !this.cargaActual.some(c => c.idAsignatura === a.idAsignatura);
      return porFacultad && porCarrera && porNombre && noEnCarga;
    });
  }

  get hayPendientes(): boolean {
    // Compara IDs de la carga actual con los que vienen del servidor
    return true; // siempre habilitar guardar si hay docente seleccionado
  }

  // ── Ciclo de vida ────────────────────────────────────────────
  ngOnInit() {
    this.cargarDocentes();
    this.cargarAsignaturas();
  }

  ngOnDestroy() { this.subs.unsubscribe(); }

  // ── Carga de datos ───────────────────────────────────────────
  private cargarDocentes() {
    this.cargandoDocentes = true;
    this.subs.add(
      this.svc.getDocentes().subscribe({
        next: (data) => { this.docentes = data; this.cargandoDocentes = false; },
        error: () => { this.cargandoDocentes = false; alert('Error al cargar docentes.'); },
      })
    );
  }

  private cargarAsignaturas() {
    this.cargandoAsignaturas = true;
    this.subs.add(
      this.svc.getAsignaturas().subscribe({
        next: (data) => { this.asignaturas = data; this.cargandoAsignaturas = false; },
        error: () => { this.cargandoAsignaturas = false; alert('Error al cargar asignaturas.'); },
      })
    );
  }

  // ── Selección de docente ─────────────────────────────────────
  seleccionarDocente(docente: DocenteActivoDTO) {
    this.docenteSeleccionado = docente;
    this.resultado           = null;
    // Inicialmente la carga actual vacía; el usuario la construye
    // desde el catálogo. En una mejora futura se puede precargar
    // desde un endpoint GET /carga-academica/docentes/:id/asignaturas
    this.cargaActual = [];
    this.resetFiltros();
    this.vista = 'gestion';
  }

  volverASeleccion() {
    this.docenteSeleccionado = null;
    this.vista = 'seleccion';
  }

  // ── Gestión de carga ─────────────────────────────────────────
  agregarAsignatura(a: AsignaturaJerarquiaDTO) {
    if (this.cargaActual.some(c => c.idAsignatura === a.idAsignatura)) return; // guard
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

  onFacultadChange() {
    this.filtroCarrera = 0; // Resetear carrera al cambiar facultad
  }

  // ── Guardar cambios ──────────────────────────────────────────
  guardarCambios() {
    if (!this.docenteSeleccionado) return;
    if (!confirm(`¿Confirmar los cambios en la carga de ${this.docenteSeleccionado.nombres} ${this.docenteSeleccionado.apellidos}?`)) return;

    this.guardando = true;
    this.resultado = null;

    const payload = {
      idDocente:      this.docenteSeleccionado.idDocente,
      asignaturasIds: this.cargaActual.map(a => a.idAsignatura),
    };

    this.subs.add(
      this.svc.sincronizar(payload).subscribe({
        next: (res) => {
          this.guardando = false;
          this.resultado = res;
          // Actualizar conteo en la tabla de docentes
          if (this.docenteSeleccionado) {
            this.docenteSeleccionado.totalAsignaturas = this.cargaActual.length;
            const idx = this.docentes.findIndex(d => d.idDocente === this.docenteSeleccionado!.idDocente);
            if (idx !== -1) this.docentes[idx] = { ...this.docentes[idx], totalAsignaturas: this.cargaActual.length };
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
