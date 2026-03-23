import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import {BackupService} from '../../../core/services/configuracion/backup-service';

interface ToastMessage {
  id: number; severity: 'success'|'error'|'warn'|'info'; summary: string; detail: string;
}

@Component({
  selector: 'app-respaldos',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './respaldos-component.html',
  styleUrl: './respaldos-component.css'
})
export class RespaldosComponent implements OnInit {

  private backupService = inject(BackupService);

  archivosRespaldo: string[] = [];
  cargandoLista = false;
  generando = false;
  restaurando = false;

  toasts: ToastMessage[] = [];
  private toastIdCtr = 0;

  modalRestaurarAbierto = false;
  respaldoSeleccionado: string | null = null;

  ngOnInit(): void {
    this.cargarRespaldos();
  }

  addToast(s: ToastMessage['severity'], sum: string, det: string, life = 5000): void {
    const id = ++this.toastIdCtr;
    this.toasts.push({ id, severity: s, summary: sum, detail: det });
    setTimeout(() => this.removeToast(id), life);
  }
  removeToast(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }
  private toastOk(d: string) { this.addToast('success', 'Éxito', d, 4000); }
  private toastErr(d: string) { this.addToast('error', 'Error', d, 6000); }

  cargarRespaldos(): void {
    this.cargandoLista = true;
    this.backupService.listarRespaldos().subscribe({
      next: (data) => {
        this.archivosRespaldo = data;
        this.cargandoLista = false;
      },
      error: () => {
        this.toastErr('No se pudo cargar la lista de respaldos desde el servidor.');
        this.cargandoLista = false;
      }
    });
  }

  generarRespaldo(): void {
    this.generando = true;
    this.backupService.generarRespaldo().subscribe({
      next: (res) => {
        this.generando = false;
        this.toastOk(res.mensaje);
        this.cargarRespaldos(); // Recargamos la tabla para ver el nuevo archivo
      },
      error: (err) => {
        this.generando = false;
        this.toastErr(err.error?.mensaje || 'Ocurrió un error al generar el respaldo.');
      }
    });
  }

  abrirModalRestaurar(nombreArchivo: string): void {
    this.respaldoSeleccionado = nombreArchivo;
    this.modalRestaurarAbierto = true;
  }

  cerrarModalRestaurar(): void {
    this.respaldoSeleccionado = null;
    this.modalRestaurarAbierto = false;
  }

  confirmarRestauracion(): void {
    if (!this.respaldoSeleccionado) return;

    this.restaurando = true;
    this.backupService.restaurarRespaldo(this.respaldoSeleccionado).subscribe({
      next: (res) => {
        this.restaurando = false;
        this.cerrarModalRestaurar();
        this.addToast('success', '¡Restauración Completada!', res.mensaje, 6000);
      },
      error: (err) => {
        this.restaurando = false;
        this.cerrarModalRestaurar();
        this.toastErr(err.error?.mensaje || 'Error crítico al intentar restaurar la base de datos.');
      }
    });
  }

  formatearFechaDesdeNombre(nombreArchivo: string): string {
    try {
      const parts = nombreArchivo.replace('sgac_backup_', '').replace('.dump', '').split('_');
      if (parts.length === 2) {
        const d = parts[0];
        const t = parts[1];
        return `${d.slice(6,8)}/${d.slice(4,6)}/${d.slice(0,4)} — ${t.slice(0,2)}:${t.slice(2,4)}:${t.slice(4,6)}`;
      }
    } catch (e) {}
    return 'Fecha desconocida';
  }
}
