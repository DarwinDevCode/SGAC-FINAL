import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule }       from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { ConfiguracionService } from '../../../core/services/configuracion/Configuracion';
import { CronogramaActivoResponse, PeriodoInfo, FaseInfo } from '../../../core/models/Cronograma';

export interface GanttCol {
  label:    string;
  subLabel: string;
  leftPct:  number;
  widthPct: number;
}

@Component({
  selector:    'app-cronograma-activo',
  standalone:  true,
  imports:     [CommonModule, LucideAngularModule],
  templateUrl: './cronograma-activo.component.html',
  styleUrl:    './cronograma-activo.component.css',
})
export class CronogramaActivoComponent implements OnInit, OnDestroy {

  private svc = inject(ConfiguracionService);

  cargando   = true;
  error      = '';
  periodo:  PeriodoInfo | null  = null;
  fases:    FaseInfo[]          = [];

  // Refresco automático cada 5 minutos
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  // Colores — misma paleta que gestion-periodos
  readonly FASE_COLORS = [
    '#3b82f6','#22c55e','#f59e0b','#a855f7','#ef4444',
    '#eab308','#06b6d4','#ec4899','#6366f1','#14b8a6',
  ];

  // ═══════════════════════════════════════════════════════════════════════════
  ngOnInit(): void {
    this.cargar();
    this.refreshInterval = setInterval(() => this.cargar(), 5 * 60 * 1000);
  }
  ngOnDestroy(): void {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  // ── Carga ─────────────────────────────────────────────────────────────────
  cargar(): void {
    this.cargando = true;
    this.svc.obtenerCronogramaActual().subscribe({
      next: (res: CronogramaActivoResponse) => {
        this.cargando = false;
        if (res.exito && res.periodo && res.fases) {
          this.periodo = res.periodo;
          this.fases   = res.fases;
          this.error   = '';
        } else {
          this.error   = res.mensaje ?? 'No hay un período activo en este momento.';
          this.periodo = null;
          this.fases   = [];
        }
      },
      error: () => {
        this.cargando = false;
        this.error   = 'No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.';
      },
    });
  }

  // ── Helpers de color ──────────────────────────────────────────────────────
  getFaseColor(i: number): string { return this.FASE_COLORS[i % this.FASE_COLORS.length]; }

  // ── Helpers de fecha ──────────────────────────────────────────────────────
  fmt(f: string): string {
    if (!f || f.length < 10) return f;
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }
  fmtLong(f: string): string {
    if (!f) return '';
    const MESES = ['enero','febrero','marzo','abril','mayo','junio',
      'julio','agosto','septiembre','octubre','noviembre','diciembre'];
    const [y, m, d] = f.split('-');
    return `${parseInt(d)} de ${MESES[parseInt(m) - 1]} de ${y}`;
  }

  // ── Posición de la barra Gantt ────────────────────────────────────────────
  getFaseBarStyle(fase: FaseInfo): { left: string; width: string } | null {
    if (!this.periodo) return null;
    const pI = new Date(this.periodo.fechaInicio + 'T00:00:00').getTime();
    const pF = new Date(this.periodo.fechaFin    + 'T00:00:00').getTime();
    const fI = new Date(fase.fechaInicio         + 'T00:00:00').getTime();
    const fF = new Date(fase.fechaFin            + 'T00:00:00').getTime();
    const total = pF - pI;
    const left  = Math.max(0, ((fI - pI) / total) * 100);
    const width = Math.max(0.5, ((fF - fI + 86400000) / total) * 100);
    return { left: `${left}%`, width: `${Math.min(width, 100 - left)}%` };
  }

  /**
   * Posición de la línea "hoy" en el Gantt (porcentaje).
   * Retorna null si la fecha actual está fuera del rango del periodo.
   */
  get hoyCursoPct(): number | null {
    if (!this.periodo) return null;
    const pI    = new Date(this.periodo.fechaInicio + 'T00:00:00').getTime();
    const pF    = new Date(this.periodo.fechaFin    + 'T00:00:00').getTime();
    const hoy   = new Date(); hoy.setHours(0, 0, 0, 0);
    const hoyMs = hoy.getTime();
    if (hoyMs < pI || hoyMs > pF) return null;
    return ((hoyMs - pI) / (pF - pI)) * 100;
  }

  // ── Columnas del Gantt (adaptativas: semanal / quincenal / mensual) ────────
  get ganttColumns(): GanttCol[] {
    if (!this.periodo) return [];
    const inicio  = new Date(this.periodo.fechaInicio + 'T00:00:00');
    const fin     = new Date(this.periodo.fechaFin    + 'T00:00:00');
    const totalMs = fin.getTime() - inicio.getTime();
    const totalDias = totalMs / 86400000;
    const cols: GanttCol[] = [];
    const MESES = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];

    if (totalDias <= 90) {
      // Semanal
      let cur = new Date(inicio); let n = 1;
      while (cur <= fin) {
        const sS = cur.getTime();
        const sE = Math.min(new Date(cur.getTime() + 6 * 86400000).getTime(), fin.getTime());
        cols.push({
          label:    `Sem ${n}`,
          subLabel: `${String(cur.getDate()).padStart(2,'0')}/${String(cur.getMonth()+1).padStart(2,'0')}`,
          leftPct:  ((sS - inicio.getTime()) / totalMs) * 100,
          widthPct: ((sE - sS + 86400000)   / totalMs) * 100,
        });
        cur = new Date(cur.getTime() + 7 * 86400000); n++;
      }
    } else if (totalDias <= 180) {
      // Quincenal
      let cur = new Date(inicio.getFullYear(), inicio.getMonth(), 1);
      while (cur <= fin) {
        for (const sd of [1, 16]) {
          const sS = new Date(cur.getFullYear(), cur.getMonth(), sd);
          if (sS > fin) break;
          const sE = sd === 1
            ? new Date(cur.getFullYear(), cur.getMonth(), 15)
            : new Date(cur.getFullYear(), cur.getMonth() + 1, 0);
          const sMs = Math.max(sS.getTime(), inicio.getTime());
          const eMs = Math.min(sE.getTime(), fin.getTime());
          if (eMs >= sMs) cols.push({
            label:    MESES[sS.getMonth()],
            subLabel: `${String(sS.getDate()).padStart(2,'0')}/${String(sS.getMonth()+1).padStart(2,'0')}`,
            leftPct:  ((sMs - inicio.getTime()) / totalMs) * 100,
            widthPct: ((eMs - sMs + 86400000)   / totalMs) * 100,
          });
        }
        cur = new Date(cur.getFullYear(), cur.getMonth() + 1, 1);
      }
    } else {
      // Mensual
      let cur = new Date(inicio.getFullYear(), inicio.getMonth(), 1);
      while (cur <= fin) {
        const mS  = Math.max(cur.getTime(), inicio.getTime());
        const nM  = new Date(cur.getFullYear(), cur.getMonth() + 1, 1);
        const mE  = Math.min(nM.getTime() - 1, fin.getTime());
        if (mE >= mS) cols.push({
          label:    MESES[cur.getMonth()],
          subLabel: `${String(new Date(mS).getDate()).padStart(2,'0')}/${String(cur.getMonth()+1).padStart(2,'0')}`,
          leftPct:  ((mS - inicio.getTime()) / totalMs) * 100,
          widthPct: ((mE - mS + 86400000)   / totalMs) * 100,
        });
        cur = nM;
      }
    }
    return cols;
  }

  // ── Fase actual (para la tarjeta de resumen) ──────────────────────────────
  get faseActual(): FaseInfo | null {
    return this.fases.find(f => f.esActual) ?? null;
  }
  get indiceFaseActual(): number {
    return this.fases.findIndex(f => f.esActual);
  }

  isFasePasada(fase: FaseInfo): boolean {
    const hoy = new Date(); hoy.setHours(0, 0, 0, 0);
    const fin = new Date(fase.fechaFin + 'T00:00:00');
    return fin < hoy;
  }
}
