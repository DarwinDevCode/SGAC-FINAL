import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { InformeMensualService } from '../../../core/services/informe-mensual.service';
import { AuthService } from '../../../core/services/auth-service';
import { InformeMensualResponse } from '../../../core/dto/informe-mensual-response';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-docente-aprobar-informe-mensual',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule],
  templateUrl: './aprobar-informe-mensual.html',
  styleUrl: './aprobar-informe-mensual.css'
})
export class AprobarInformeMensualComponent implements OnInit {
  private informeService = inject(InformeMensualService);
  private authService = inject(AuthService);

  informes: InformeMensualResponse[] = [];
  isLoading = true;
  isProcessing = false;
  error = '';
  success = '';
  idUsuario: number | null = null;
  userRole: string | null = null;
  
  selectedInforme: InformeMensualResponse | null = null;
  observaciones: string = '';

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user && user.idUsuario) {
      this.idUsuario = user.idUsuario;
      this.userRole = this.normalizeRole(user.rolActual);
      this.cargarPendientes();
    } else {
      this.error = 'Usuario no identificado';
      this.isLoading = false;
    }
  }

  private normalizeRole(rawRole?: string | null): string {
    if (!rawRole) return '';
    return rawRole.replace(/^ROLE_/, '').toUpperCase();
  }

  cargarPendientes() {
    this.isLoading = true;
    this.error = '';

    let observable;
    if (this.userRole === 'COORDINADOR') {
      observable = this.informeService.listarPendientesCoordinador();
    } else if (this.userRole === 'DECANO') {
      observable = this.informeService.listarPendientesDecano();
    } else {
      observable = this.informeService.listarPendientesDocente(this.idUsuario!);
    }

    observable.pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (data) => this.informes = data || [],
      error: () => this.error = 'Error al cargar los informes pendientes.'
    });
  }

  verDetalle(informe: InformeMensualResponse) {
    this.selectedInforme = informe;
    this.observaciones = '';
    this.error = '';
    this.success = '';
  }

  cerrarDetalle() {
    this.selectedInforme = null;
  }

  aprobar() {
    if (!this.selectedInforme) return;
    this.isProcessing = true;
    this.informeService.aprobarInforme(this.selectedInforme.idInformeMensual, this.userRole!).pipe(
      finalize(() => this.isProcessing = false)
    ).subscribe({
      next: () => {
        this.success = 'Informe aprobado con éxito.';
        this.selectedInforme = null;
        this.cargarPendientes();
      },
      error: (err) => this.error = err.error?.message || 'Error al aprobar el informe.'
    });
  }

  observar() {
    if (!this.selectedInforme || !this.observaciones.trim()) {
       this.error = 'Debe ingresar una observación.';
       return;
    }
    this.isProcessing = true;
    this.informeService.observarInforme(this.selectedInforme.idInformeMensual, this.observaciones).pipe(
      finalize(() => this.isProcessing = false)
    ).subscribe({
      next: () => {
        this.success = 'Informe observado con éxito.';
        this.selectedInforme = null;
        this.cargarPendientes();
      },
      error: (err) => this.error = err.error?.message || 'Error al observar el informe.'
    });
  }

  rechazar() {
    if (!this.selectedInforme || !this.observaciones.trim()) {
        this.error = 'Debe ingresar el motivo del rechazo.';
        return;
    }
    this.isProcessing = true;
    this.informeService.rechazarInforme(this.selectedInforme.idInformeMensual, this.observaciones).pipe(
      finalize(() => this.isProcessing = false)
    ).subscribe({
      next: () => {
        this.success = 'Informe rechazado.';
        this.selectedInforme = null;
        this.cargarPendientes();
      },
      error: (err) => this.error = err.error?.message || 'Error al rechazar el informe.'
    });
  }
}
