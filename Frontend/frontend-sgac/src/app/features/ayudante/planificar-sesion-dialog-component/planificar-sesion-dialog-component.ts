import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { AyudantiaService } from '../../../core/services/ayudantia/ayudantia-service';
import { PlanificarSesionRequestDTO } from '../../../core/models/ayudantia/asistencia';

@Component({
  selector: 'app-planificar-sesion-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './planificar-sesion-dialog-component.html',
  styleUrl: './planificar-sesion-dialog-component.css',
})
export class PlanificarSesionDialogComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private ayudantiaService = inject(AyudantiaService);
  private dialogRef = inject(MatDialogRef<PlanificarSesionDialogComponent>);
  private destroy$ = new Subject<void>();

  form!: FormGroup;
  enviando = signal(false);
  cargandoDatos = signal(false);
  errorMsg = signal<string | null>(null);

  ngOnInit(): void {
    this.inicializarFormulario();
  }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }


  inicializarFormulario(): void {
    this.form = this.fb.group({
      fecha: ['', [Validators.required]],
      horaInicio: ['', [Validators.required]],
      horaFin: ['', [Validators.required]],
      lugar: ['', [Validators.required]],
      tema: ['', [Validators.required, Validators.minLength(3)]],
    }, { validators: this.validarHorarios });
  }

  validarHorarios(group: AbstractControl): ValidationErrors | null {
    const horaInicio = group.get('horaInicio')?.value;
    const horaFin = group.get('horaFin')?.value;
    if (!horaInicio || !horaFin) return null;
    const [hiH, hiM] = horaInicio.split(':').map(Number);
    const [hfH, hfM] = horaFin.split(':').map(Number);
    if ((hfH * 60 + hfM) <= (hiH * 60 + hiM)) return { horariosInvalidos: true };
    return null;
  }

  enviar(): void {
    if (this.form.invalid) { this.marcarCamposComoTocados(); return; }
    this.errorMsg.set(null);
    this.enviando.set(true);

    const v = this.form.value;

    const request: PlanificarSesionRequestDTO = { 
      fecha: v.fecha, 
      horaInicio: v.horaInicio, 
      horaFin: v.horaFin, 
      lugar: v.lugar, 
      tema: v.tema 
    };

    this.ayudantiaService.planificarSesion(request)
      .pipe(takeUntil(this.destroy$), finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (res: any) => { if (res.valido) this.dialogRef.close(true); else this.errorMsg.set(res.mensaje ?? 'No se pudo planificar.'); },
        error: (err: HttpErrorResponse) => this.errorMsg.set(err?.error?.message ?? 'Error al planificar la sesión.'),
      });
  }

  marcarCamposComoTocados(): void { Object.keys(this.form.controls).forEach(key => this.form.get(key)?.markAsTouched()); }
  cerrar(): void { this.dialogRef.close(false); }

  obtenerErrorCampo(nombreCampo: string): string | null {
    const control = this.form.get(nombreCampo);
    if (!control || !control.errors || !control.touched) return null;
    if (control.errors['required']) return 'Este campo es requerido';
    if (control.errors['minlength']) return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
    return 'Valor inválido';
  }

  obtenerErrorFormulario(): string | null {
    return (this.form.hasError('horariosInvalidos') && this.form.touched) ? 'La hora de fin debe ser posterior a la de inicio' : null;
  }

  get hoy(): string { return new Date().toISOString().split('T')[0]; }

  calcularDuracion(): string {
    const horaInicio = this.form.get('horaInicio')?.value;
    const horaFin = this.form.get('horaFin')?.value;
    if (!horaInicio || !horaFin) return '—';
    const [hiH, hiM] = horaInicio.split(':').map(Number);
    const [hfH, hfM] = horaFin.split(':').map(Number);
    const minInicio = hiH * 60 + hiM;
    const minFin = hfH * 60 + hfM;
    if (minFin <= minInicio) return '—';
    const duracion = minFin - minInicio;
    const horas = Math.floor(duracion / 60);
    const minutos = duracion % 60;
    return minutos === 0 ? `${horas}h` : `${horas}h ${minutos}m`;
  }
}
