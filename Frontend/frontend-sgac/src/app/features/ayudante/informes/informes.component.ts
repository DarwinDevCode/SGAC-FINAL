import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { InformeMensualService } from '../../../core/services/informe-mensual.service';
import { AuthService } from '../../../core/services/auth-service';
import { InformeMensualResponse } from '../../../core/dto/informe-mensual-response';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-ayudante-informes',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule],
  templateUrl: './informes.html',
  styleUrl: './informes.css'
})
export class InformesComponent implements OnInit {
  private informeService = inject(InformeMensualService);
  private authService = inject(AuthService);

  informes: InformeMensualResponse[] = [];
  isLoading = true;
  isGenerating = false;
  isSubmitting = false;
  error = '';
  success = '';
  idUsuario: number | null = null;
  editModes: { [key: number]: boolean } = {};

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.idUsuario = user.idUsuario;
      this.cargarInformes();
    } else {
      this.error = 'Usuario no identificado';
      this.isLoading = false;
    }
  }

  cargarInformes() {
    this.isLoading = true;
    this.error = '';
    this.informeService.listarMisInformes(this.idUsuario!).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (data) => this.informes = data || [],
      error: () => this.error = 'Error al cargar los informes.'
    });
  }

  generarBorrador() {
    this.error = '';
    this.success = '';
    this.isGenerating = true;
    const date = new Date();
    this.informeService.generarBorradorIA(this.idUsuario!, date.getMonth() + 1, date.getFullYear()).pipe(
      finalize(() => this.isGenerating = false)
    ).subscribe({
      next: () => {
        this.success = 'Borrador generado con éxito.';
        this.cargarInformes();
      },
      error: (err) => this.error = err.error?.message || 'Error al generar el borrador.'
    });
  }

  enviarARevision(informe: InformeMensualResponse) {
    this.error = '';
    this.success = '';
    this.isSubmitting = true;
    this.informeService.enviarARevision(informe.idInformeMensual, informe.contenidoBorrador).pipe(
      finalize(() => this.isSubmitting = false)
    ).subscribe({
      next: () => {
        this.success = 'Informe enviado a revisión con éxito.';
        this.editModes[informe.idInformeMensual] = false;
        this.cargarInformes();
      },
      error: (err) => this.error = err.error?.message || 'Error al enviar a revisión.'
    });
  }

  toggleEdit(id: number) {
    this.editModes[id] = !this.editModes[id];
  }

  isEditing(id: number): boolean {
    return !!this.editModes[id];
  }
}
