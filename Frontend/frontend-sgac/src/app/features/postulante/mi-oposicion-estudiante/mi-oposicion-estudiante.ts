import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { LucideAngularModule }       from 'lucide-angular';
import { ActivatedRoute }            from '@angular/router';  // ← FIX: leer idConvocatoria de la URL

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
  private route = inject(ActivatedRoute);
  private svc   = inject(EvaluacionOposicionService);

  loading    = true;
  errorMsg   = '';

  // El turno del postulante autenticado.
  // La función SQL fn_obtener_mi_turno filtra por id_usuario extraído
  // del JWT en el backend, por lo que este componente nunca accede
  // ni muestra datos de otros postulantes.
  miTurno: TurnoOposicion | null = null;

  ngOnInit(): void {
    // La ruta es /postulante/oposicion/:id, donde :id es el idConvocatoria.
    // El nombre del parámetro en app.routes.ts es 'id' (no 'idConvocatoria'),
    // por eso usamos 'id' aquí.
    const paramId = this.route.snapshot.paramMap.get('id');
    const idConvocatoria = paramId ? Number(paramId) : 0;

    if (!idConvocatoria) {
      this.loading  = false;
      this.errorMsg = 'No se pudo identificar la convocatoria. Verifica la URL.';
      return;
    }

    // ── FIX: llamar al endpoint específico del postulante ─────────────
    // Antes se llamaba a obtenerCronograma() (que devuelve TODOS los
    // turnos) y se tomaba turnos[0] como el turno del estudiante.
    // Esto era un problema de privacidad: el estudiante podía ver los
    // temas de los demás postulantes en las DevTools del navegador.
    //
    // Ahora llamamos a obtenerMiTurno() que delega a fn_obtener_mi_turno,
    // una función SQL que filtra por el id_usuario del JWT.
    // El backend devuelve exactamente un objeto "turno" (o un error si
    // el estudiante no tiene turno en esta convocatoria).
    //
    this.svc.obtenerMiTurno(idConvocatoria).subscribe({
      next: res => {
        this.loading = false;
        // El backend retorna { exito: true, turno: {...} }
        // La propiedad 'turno' es un único objeto, no un array.
        // La mapeamos a la interfaz TurnoOposicion que ya tenemos.
        this.miTurno = (res as any).turno ?? null;
      },
      error: (err: Error) => {
        this.loading  = false;
        this.errorMsg = err.message;
      }
    });
  }

  // ── Helpers de formateo ───────────────────────────────────────────

  formatFecha(f?: string): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  formatHora(h?: string): string { return h ?? '—'; }

  // Devuelve la clase CSS del badge según el estado del turno.
  // Usamos las mismas clases definidas en el CSS del componente (moe-badge-*).
  get badgeColor(): string {
    if (!this.miTurno) return 'moe-badge-gray';
    return ({
      PROGRAMADA:  'moe-badge-blue',
      EN_CURSO:    'moe-badge-amber',
      FINALIZADA:  'moe-badge-green',
      NO_PRESENTO: 'moe-badge-red'
    } as Record<string, string>)[this.miTurno.estado] ?? 'moe-badge-gray';
  }
}
