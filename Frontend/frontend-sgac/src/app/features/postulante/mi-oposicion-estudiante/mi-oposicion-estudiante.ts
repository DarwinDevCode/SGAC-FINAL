// src/app/features/estudiante/mi-oposicion/mi-oposicion-estudiante.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { LucideAngularModule }       from 'lucide-angular';
import { AuthService }               from '../../../core/services/auth-service';
import { EvaluacionOposicionService } from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import { TurnoOposicion }            from '../../../core/models/evaluaciones/EvaluacionOposicion';

@Component({
  selector: 'app-mi-oposicion-estudiante',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './mi-oposicion-estudiante.html',
  styleUrls: ['./mi-oposicion-estudiante.css']
})
export class MiOposicionEstudianteComponent implements OnInit {
  private authSrv = inject(AuthService);
  private svc     = inject(EvaluacionOposicionService);

  loading    = true;
  errorMsg   = '';
  miTurno:   TurnoOposicion | null = null;

  // En producción, idConvocatoria vendría del contexto de postulación del estudiante.
  // Por ahora se asume que hay una convocatoria activa vinculada a su postulación.
  idConvocatoria = 1; // TODO: obtener del contexto

  ngOnInit(): void {
    const user = this.authSrv.getUser();
    if (!user) { this.loading = false; this.errorMsg = 'No hay sesión activa.'; return; }

    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => {
        this.loading = false;
        const turnos = res.cronograma ?? [];
        // Buscar el turno que pertenece al estudiante por nombre
        // En producción esto debería filtrarse por id_postulacion del estudiante
        // que vendría del backend con el cronograma del estudiante autenticado.
        this.miTurno = turnos[0] ?? null;
      },
      error: (err: Error) => {
        this.loading  = false;
        this.errorMsg = err.message;
      }
    });
  }

  formatFecha(f: string | undefined): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  formatHora(h: string | undefined): string { return h ?? '—'; }

  get badgeColor(): string {
    if (!this.miTurno) return 'bg-slate-100 text-slate-500';
    const m: Record<string, string> = {
      PROGRAMADA:  'moe-badge-blue',
      EN_CURSO:    'moe-badge-amber',
      FINALIZADA:  'moe-badge-green',
      NO_PRESENTO: 'moe-badge-red'
    };
    return m[this.miTurno.estado] ?? 'moe-badge-gray';
  }
}
