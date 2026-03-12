import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { AuditoriaService, LogAuditoria } from './services/auditoria.service';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.css',
})
export class Auditoria implements OnInit {
  logs: LogAuditoria[] = [];
  totalRecords: number = 0;
  totalPages: number = 0;
  loading: boolean = true;

  // Filtros
  filtros: any = {
    queryParams: '',
    tablaAfectada: '',
    accion: '',
    fechaInicio: null,
    fechaFin: null
  };

  // Paginacion y Orden actual
  currentPage: number = 0;
  pageSize: number = 10;
  currentSort: string = 'fechaHora';
  currentDirection: string = 'desc';

  // Visor de Detalles (Modal)
  verDetalleDialog: boolean = false;
  logSeleccionado: LogAuditoria | null = null;
  camposModificados: { campo: string, anterior: string, nuevo: string }[] = [];

  constructor(private auditoriaService: AuditoriaService) {}

  ngOnInit() {
    this.loadLogs();
  }

  loadLogs() {
    this.loading = true;
    const filtrosLimpios = this.prepararFiltros(this.filtros);

    this.auditoriaService.obtenerLogsPaginados(
      filtrosLimpios, 
      this.currentPage, 
      this.pageSize, 
      this.currentSort, 
      this.currentDirection
    ).subscribe({
      next: (res) => {
        this.logs = res.content;
        this.totalRecords = res.totalElements;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar logs:', err);
        this.loading = false;
      }
    });
  }

  cambiarPagina(pag: number) {
    if (pag >= 0 && pag < this.totalPages) {
      this.currentPage = pag;
      this.loadLogs();
    }
  }

  cambiarDataSize(size: string) {
    this.pageSize = parseInt(size, 10);
    this.currentPage = 0;
    this.loadLogs();
  }

  aplicarFiltros() {
    this.currentPage = 0;
    this.loadLogs();
  }

  limpiarFiltros() {
    this.filtros = {
      queryParams: '',
      tablaAfectada: '',
      accion: '',
      fechaInicio: null,
      fechaFin: null
    };
    this.currentPage = 0;
    this.loadLogs();
  }

  descargarReporte() {
    const filtrosLimpios = this.prepararFiltros(this.filtros);
    this.auditoriaService.descargarReportePdf(filtrosLimpios).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'reporte_auditoria.pdf';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  verDetalle(log: LogAuditoria) {
    this.logSeleccionado = log;
    this.camposModificados = [];

    try {
      if (log.valorAnterior && log.valorAnterior !== 'null' && log.valorNuevo && log.valorNuevo !== 'null') {
        const jsonAnterior = JSON.parse(log.valorAnterior);
        const jsonNuevo = JSON.parse(log.valorNuevo);

        const allKeys = new Set([...Object.keys(jsonAnterior), ...Object.keys(jsonNuevo)]);
        allKeys.forEach(key => {
          const vAnterior = JSON.stringify(jsonAnterior[key]);
          const vNuevo = JSON.stringify(jsonNuevo[key]);

          if (vAnterior !== vNuevo) {
            this.camposModificados.push({
              campo: key,
              anterior: vAnterior !== undefined ? vAnterior : 'N/D',
              nuevo: vNuevo !== undefined ? vNuevo : 'N/D'
            });
          }
        });
      } else if (log.valorAnterior || log.valorNuevo) {
         this.camposModificados.push({
           campo: 'Cuerpo de Registro',
           anterior: log.valorAnterior || 'Vacío',
           nuevo: log.valorNuevo || 'Vacío'
         });
      }
    } catch(e) {
      this.camposModificados.push({
        campo: 'Cuerpo de Registro',
        anterior: log.valorAnterior || 'Vacío',
        nuevo: log.valorNuevo || 'Vacío'
      });
    }

    this.verDetalleDialog = true;
  }

  cerrarDetalle() {
    this.verDetalleDialog = false;
    this.logSeleccionado = null;
  }

  private prepararFiltros(f: any): any {
    const result: any = { ...f };
    if (result.fechaInicio) result.fechaInicio = new Date(result.fechaInicio).toISOString();
    if (result.fechaFin) result.fechaFin = new Date(result.fechaFin).toISOString();
    return result;
  }
}
