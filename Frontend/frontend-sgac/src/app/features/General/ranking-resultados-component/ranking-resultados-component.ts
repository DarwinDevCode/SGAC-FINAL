import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormsModule }         from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';

import { RankingService }                  from '../../../core/services/resultados/ranking-service';
import { AuthService }                     from '../../../core/services/auth-service';
import { ResultadoRanking, EstadoRanking } from '../../../core/models/resultados/Ranking';

type OrdenDir = 'asc' | 'desc';

// Roles que pueden descargar reportes
const ROLES_REPORTE = ['DECANO', 'COORDINADOR', 'ADMINISTRADOR'];

@Component({
  selector:    'app-ranking-resultados',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './ranking-resultados-component.html',
  styleUrls:   ['./ranking-resultados-component.css'],
})
export class RankingResultadosComponent implements OnInit, OnDestroy {

  private svc     = inject(RankingService);
  private authSvc = inject(AuthService);

  // ── Estado de carga ───────────────────────────────────────────────
  loading         = true;
  error           = '';
  faseNoPublicada = false;
  mensajeFase     = '';

  // ── Datos ─────────────────────────────────────────────────────────
  private todos: ResultadoRanking[] = [];
  filtrados:     ResultadoRanking[] = [];

  // ── Filtros y orden ───────────────────────────────────────────────
  busqueda         = '';
  filtroAsignatura = '';
  filtroCarrera    = '';
  ordenDir: OrdenDir = 'desc';

  asignaturas: string[] = [];
  carreras:    string[] = [];

  // ── Descarga ──────────────────────────────────────────────────────
  descargandoExcel = false;
  descargandoPdf   = false;

  // ── Estadísticas ──────────────────────────────────────────────────
  get totalSeleccionados(): number {
    return this.todos.filter(r => r.estado === 'SELECCIONADO').length;
  }
  get totalElegibles(): number {
    return this.todos.filter(r => r.estado === 'ELEGIBLE').length;
  }
  get totalResultados(): number { return this.todos.length; }

  // Toast
  private toastTimer: any;
  toastMsg  = '';
  toastTipo = 'ok';

  // ═══════════════════════════════════════════════════════════════════
  ngOnInit(): void  { this.cargar(); }
  ngOnDestroy(): void { clearTimeout(this.toastTimer); }

  // ── Seguridad de UI ───────────────────────────────────────────────

  /**
   * Devuelve true si el rol activo del usuario permite ver y descargar
   * reportes. Se usa en *ngIf de los botones de exportación.
   */
  esRolAutorizadoParaReportes(): boolean {
    const rol = this.authSvc.getUser()?.rolActual?.toUpperCase() ?? '';
    return ROLES_REPORTE.includes(rol);
  }

  // ── Carga de datos ─────────────────────────────────────────────────

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
            this.mensajeFase = res.mensaje ?? 'Los resultados están siendo procesados.';
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

  // ── Filtros ────────────────────────────────────────────────────────

  private construirFiltros(): void {
    this.asignaturas = [...new Set(this.todos.map(r => r.asignatura))].sort();
    this.carreras    = [...new Set(this.todos.map(r => r.carrera))].sort();
  }

  aplicarFiltros(): void {
    const busq = this.busqueda.toLowerCase().trim();
    this.filtrados = this.todos.filter(r => {
      const ok1 = !busq || r.postulante.toLowerCase().includes(busq);
      const ok2 = !this.filtroAsignatura || r.asignatura === this.filtroAsignatura;
      const ok3 = !this.filtroCarrera    || r.carrera    === this.filtroCarrera;
      return ok1 && ok2 && ok3;
    });
    this.ordenar();
  }

  limpiarFiltros(): void {
    this.busqueda = this.filtroAsignatura = this.filtroCarrera = '';
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

  // ── Exportación ────────────────────────────────────────────────────

  generarReporteExcel(): void {
    if (this.descargandoExcel) return;
    this.descargandoExcel = true;
    this.svc.exportarExcel().subscribe({
      next: blob => {
        this.descargandoExcel = false;
        this.descargarBlob(blob, this.nombreArchivo('xlsx'), 'xlsx');
        this.toast('Archivo Excel descargado correctamente.', 'ok');
      },
      error: (err: Error) => {
        this.descargandoExcel = false;
        this.toast(err.message, 'err');
      }
    });
  }

  generarReportePdf(): void {
    if (this.descargandoPdf) return;
    this.descargandoPdf = true;
    this.svc.exportarPdf().subscribe({
      next: blob => {
        this.descargandoPdf = false;
        this.descargarBlob(blob, this.nombreArchivo('pdf'), 'pdf');
        this.toast('Archivo PDF descargado correctamente.', 'ok');
      },
      error: (err: Error) => {
        this.descargandoPdf = false;
        this.toast(err.message, 'err');
      }
    });
  }

  private descargarBlob(blob: Blob, nombre: string, tipo: 'xlsx' | 'pdf'): void {
    const mime = tipo === 'xlsx'
      ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      : 'application/pdf';
    const url = window.URL.createObjectURL(new Blob([blob], { type: mime }));
    const a   = document.createElement('a');
    a.href    = url;
    a.download = nombre;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private nombreArchivo(ext: string): string {
    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm   = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd   = String(hoy.getDate()).padStart(2, '0');
    return `Ranking_Final_${yyyy}${mm}${dd}.${ext}`;
  }

  // ── Helpers de vista ──────────────────────────────────────────────

  badgeEstado(estado: EstadoRanking): string {
    return ({ SELECCIONADO: 'badge-green', ELEGIBLE: 'badge-blue',
      NO_SELECCIONADO: 'badge-gray' } as any)[estado] ?? 'badge-gray';
  }
  labelEstado(estado: EstadoRanking): string {
    return ({ SELECCIONADO: 'Seleccionado', ELEGIBLE: 'Elegible',
      NO_SELECCIONADO: 'No seleccionado' } as any)[estado] ?? estado;
  }
  iconoEstado(estado: EstadoRanking): string {
    return ({ SELECCIONADO: 'award', ELEGIBLE: 'info',
      NO_SELECCIONADO: 'x-circle' } as any)[estado] ?? 'circle';
  }
  claseFilaTabla(estado: EstadoRanking): string {
    return ({ SELECCIONADO: 'rk-fila-seleccionado', ELEGIBLE: 'rk-fila-elegible',
      NO_SELECCIONADO: '' } as any)[estado] ?? '';
  }

  inicialesPostulante(nombre: string): string {
    return nombre.split(' ').filter(p => p.length > 0)
      .slice(0, 2).map(p => p[0].toUpperCase()).join('');
  }

  formatPuntaje(n: number): string { return n.toFixed(2); }

  private toast(msg: string, tipo: 'ok' | 'err' | 'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg  = msg;
    this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', tipo === 'err' ? 8000 : 4500);
  }
}
