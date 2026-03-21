import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { AyudantiaService } from '../../../core/services/ayudantia/ayudantia-service';
import { ParticipanteRequestDTO } from '../../../core/models/general/respuesta-operacion';

@Component({
  selector: 'app-gestionar-participante-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './gestionar-participante-dialog-component.html',
  styleUrl: 'gestionar-participante-dialog-component.css'
})
export class GestionarParticipanteDialogComponent implements OnInit {

  private fb = inject(FormBuilder);
  private ayudantiaService = inject(AyudantiaService);
  private dialogRef = inject(MatDialogRef<GestionarParticipanteDialogComponent>);
  public data = inject(MAT_DIALOG_DATA); // Recibe el estudiante si es edición
  private destroy$ = new Subject<void>();

  form!: FormGroup;
  enviando = signal(false);
  errorMsg = signal<string | null>(null);
  esEdicion = signal(false);

  ngOnInit(): void {
    this.esEdicion.set(!!this.data);

    this.form = this.fb.group({
      nombre: [this.data?.nombreCompleto || '', [Validators.required, Validators.minLength(3)]],
      curso: [this.data?.curso || '', [Validators.required]],
      paralelo: [this.data?.paralelo || '']
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMsg.set(null);
    this.enviando.set(true);

    const valores = this.form.value;
    const request: ParticipanteRequestDTO = {
      accion: this.esEdicion() ? 'UPD' : 'INS',
      nombre: valores.nombre.trim(),
      curso: valores.curso.trim(),
      paralelo: valores.paralelo?.trim() || '',
      idParticipante: this.data?.idParticipanteAyudantia
    };

    this.ayudantiaService.gestionarParticipante(request)
      .pipe(takeUntil(this.destroy$), finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (res) => {
          if (res.valido) this.dialogRef.close(true);
          else this.errorMsg.set(res.mensaje ?? 'Error al guardar el estudiante.');
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(err?.error?.message ?? 'Error de conexión.');
        }
      });
  }

  cerrar(): void {
    this.dialogRef.close(false);
  }

  obtenerErrorCampo(campo: string): string | null {
    const control = this.form.get(campo);
    if (!control || !control.errors || !control.touched) return null;
    if (control.errors['required']) return 'Este campo es requerido';
    if (control.errors['minlength']) return 'Mínimo 3 caracteres';
    return 'Dato inválido';
  }
}
