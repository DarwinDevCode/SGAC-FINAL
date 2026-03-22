import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InformeMensualService } from '../../../core/services/informe-mensual.service';
import { InformeMensualResponse } from '../../../core/dto/informe-mensual-response';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-aprobaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './aprobaciones.html',
  styleUrl: './aprobaciones.css'
})
export class AprobacionesComponent implements OnInit {
  private informeService = inject(InformeMensualService);

  informes: InformeMensualResponse[] = [];
  loading = true;
  errorMensaje = '';

  modalVisible = false;
  modalEstado: 'ACEPTADO' | 'OBSERVADO' | 'RECHAZADO' = 'ACEPTADO';
  modalObservacion = '';
  modalIdInforme = 0;
  guardando = false;

  ngOnInit() {
    this.cargarInformes();
  }

  cargarInformes() {
    this.loading = true;
    this.informeService.listarPendientesDecano().subscribe({
      next: (data) => {
        this.informes = data || [];
        this.loading = false;
      },
      error: () => {
        this.errorMensaje = 'Error al cargar informes pendientes del Decanato.';
        this.loading = false;
      }
    });
  }

  iniciarRevisar(idInformeMensual: number, estado: 'ACEPTADO' | 'OBSERVADO' | 'RECHAZADO'): void {
    this.modalIdInforme = idInformeMensual;
    this.modalEstado = estado;
    this.modalObservacion = '';
    this.modalVisible = true;
  }

  confirmarAccion(): void {
    if (this.modalEstado !== 'ACEPTADO' && !this.modalObservacion.trim()) {
      alert('Debes ingresar una observación o motivo para continuar.');
      return;
    }

    this.guardando = true;
    let obs$: any;
    if (this.modalEstado === 'ACEPTADO') {
      obs$ = this.informeService.aprobarInforme(this.modalIdInforme, 'DECANO');
    } else if (this.modalEstado === 'OBSERVADO') {
      obs$ = this.informeService.observarInforme(this.modalIdInforme, this.modalObservacion);
    } else {
      obs$ = this.informeService.rechazarInforme(this.modalIdInforme, this.modalObservacion);
    }

    obs$.pipe(finalize(() => this.guardando = false)).subscribe({
      next: () => {
        this.cerrarModal();
        this.cargarInformes();
      },
      error: (err: any) => alert(err?.error?.message || 'Error al procesar el informe.')
    });
  }

  cerrarModal() {
    this.modalVisible = false;
  }
}
