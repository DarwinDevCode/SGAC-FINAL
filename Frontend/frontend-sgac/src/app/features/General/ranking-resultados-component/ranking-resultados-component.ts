import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormsModule }         from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';

import { RankingService }                 from '../../../core/services/resultados/ranking-service';
import { ResultadoRanking, EstadoRanking } from '../../../core/models/resultados/Ranking';

type OrdenDir = 'asc' | 'desc';

@Component({
  selector:    'app-ranking-resultados',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './ranking-resultados-component.html',
  styleUrls:   ['./ranking-resultados-component.css'],
})
export class RankingResultadosComponent implements OnInit, OnDestroy {

  private svc = inject(RankingService);

  // ── Estado de carga ───────────────────────────────────────────────
  loading        = true;
  error          = '';
  faseNoPublicada = false;
  mensajeFase    = '';

  // ── Datos ─────────────────────────────────────────────────────────
  private todos:    ResultadoRanking[] = [];
  filtrados:        ResultadoRanking[] = [];

  // ── Filtros y orden ───────────────────────────────────────────────
  busqueda        = '';
  filtroAsignatura = '';
  filtroCarrera   = '';
  ordenDir: OrdenDir = 'desc';

  asignaturas: string[] = [];
  carreras:    string[] = [];

  // ── Estadísticas ──────────────────────────────────────────────────
  get totalSeleccionados(): number { return this.todos.filter(r => r.estado === 'SELECCIONADO').length; }
  get totalElegibles():     number { return this.todos.filter(r => r.estado === 'ELEGIBLE').length; }
  get totalResultados():    number { return this.todos.length; }

  private toastTimer: any;
  toastMsg  = '';
  toastTipo = 'ok';

  // ═══════════════════════════════════════════════════════════════════
  ngOnInit(): void { this.cargar(); }
  ngOnDestroy(): void { clearTimeout(this.toastTimer); }

  cargar(): void {
    this.loading         = true;
    this.error           = '';
    this.faseNoPublicada = false;

    this.svc.obtenerResultados().subscribe({
      next: res => {
        this.loading = false;

        if (!res.exito) {
          if (res.faseNoPublicada) {
            this.faseNoPublicada = true;
            this.mensajeFase     = res.mensaje ?? 'Los resultados están siendo procesados.';
          } else {
            this.error = res.mensaje ?? 'No se pudieron cargar los resultados.';
          }
          return;
        }

        this.todos = res.resultados ?? [];
        this.construirFiltros();
        this.aplicarFiltros();
      },
      error: (err: Error) => {
        this.loading = false;
        this.error   = err.message;
      }
    });
  }

  // ── Filtros ───────────────────────────────────────────────────────

  private construirFiltros(): void {
    this.asignaturas = [...new Set(this.todos.map(r => r.asignatura))].sort();
    this.carreras    = [...new Set(this.todos.map(r => r.carrera))].sort();
  }

  aplicarFiltros(): void {
    const busq = this.busqueda.toLowerCase().trim();

    this.filtrados = this.todos.filter(r => {
      const coincideBusq = !busq || r.postulante.toLowerCase().includes(busq);
      const coincideAsig = !this.filtroAsignatura || r.asignatura === this.filtroAsignatura;
      const coincideCarr = !this.filtroCarrera    || r.carrera    === this.filtroCarrera;
      return coincideBusq && coincideAsig && coincideCarr;
    });

    this.ordenar();
  }

  limpiarFiltros(): void {
    this.busqueda         = '';
    this.filtroAsignatura = '';
    this.filtroCarrera    = '';
    this.aplicarFiltros();
  }

  toggleOrden(): void {
    this.ordenDir = this.ordenDir === 'desc' ? 'asc' : 'desc';
    this.ordenar();
  }

  private ordenar(): void {
    this.filtrados = [...this.filtrados].sort((a, b) => {
      const diff = a.total - b.total;
      return this.ordenDir === 'desc' ? -diff : diff;
    });
  }

  // ── Helpers de vista ─────────────────────────────────────────────

  badgeEstado(estado: EstadoRanking): string {
    return {
      SELECCIONADO:    'badge-green',
      ELEGIBLE:        'badge-blue',
      NO_SELECCIONADO: 'badge-gray',
    }[estado] ?? 'badge-gray';
  }

  labelEstado(estado: EstadoRanking): string {
    return {
      SELECCIONADO:    'Seleccionado',
      ELEGIBLE:        'Elegible',
      NO_SELECCIONADO: 'No seleccionado',
    }[estado] ?? estado;
  }

  iconoEstado(estado: EstadoRanking): string {
    return {
      SELECCIONADO:    'award',
      ELEGIBLE:        'info',
      NO_SELECCIONADO: 'x-circle',
    }[estado] ?? 'circle';
  }

  claseFilaTabla(estado: EstadoRanking): string {
    return {
      SELECCIONADO:    'rk-fila-seleccionado',
      ELEGIBLE:        'rk-fila-elegible',
      NO_SELECCIONADO: '',
    }[estado] ?? '';
  }

  inicialesPostulante(nombre: string): string {
    return nombre
      .split(' ')
      .filter(p => p.length > 0)
      .slice(0, 2)
      .map(p => p[0].toUpperCase())
      .join('');
  }

  formatPuntaje(n: number): string {
    return n.toFixed(2);
  }

  private toast(msg: string, tipo: 'ok' | 'err' | 'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg  = msg;
    this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', 4500);
  }
}
