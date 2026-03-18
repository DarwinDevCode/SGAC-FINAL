import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth-service';
import { PostulanteService } from '../../../core/services/postulaciones/postulante-service';
import { TribunalEvaluacionResponse } from '../../../core/models/postulaciones/postulacion';

@Component({
  selector: 'app-comision-seleccion',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './comision-seleccion.html',
  styleUrls: ['./comision-seleccion.css']
})
export class ComisionSeleccion implements OnInit, OnDestroy {
  private authService      = inject(AuthService);
  private postulanteService = inject(PostulanteService);
  private subs             = new Subscription();

  isLoading    = true;
  errorMessage = '';
  datosTribunal: TribunalEvaluacionResponse | null = null;

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(){
    const user = this.authService.getUser();
    if (!user) {
      this.isLoading    = false;
      this.errorMessage = 'No hay sesión activa.';
      return;
    }

    this.subs.add(
      this.postulanteService.obtenerTribunalEvaluacion(user.idUsuario).subscribe({
        next: (datos) => {
          this.datosTribunal = datos;
          this.isLoading     = false;
        },
        error: (err: Error) => {
          this.errorMessage = err.message;
          this.isLoading    = false;
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    try {
      return new Date(fecha + 'T00:00:00').toLocaleDateString('es-EC', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
      });
    } catch {
      return fecha;
    }
  }

  formatearHora(hora: string): string {
    if (!hora) return '—';
    const [h, m] = hora.split(':');
    const hNum   = parseInt(h, 10);
    const ampm   = hNum >= 12 ? 'PM' : 'AM';
    const h12    = hNum % 12 || 12;
    return `${h12}:${m} ${ampm}`;
  }

  rolColor(rol: string): string {
    const r = rol?.toLowerCase() ?? '';
    if (r.includes('presid')) return 'rol-presidente';
    if (r.includes('vocal'))  return 'rol-vocal';
    if (r.includes('secret')) return 'rol-secretario';
    return 'rol-default';
  }

  rolIcon(rol: string): string {
    const r = rol?.toLowerCase() ?? '';
    if (r.includes('presid')) return 'shield-check';
    if (r.includes('vocal'))  return 'user-check';
    if (r.includes('secret')) return 'clipboard';
    return 'user';
  }

  iniciales(nombre: string): string {
    if (!nombre) return '?';
    return nombre.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }
}
