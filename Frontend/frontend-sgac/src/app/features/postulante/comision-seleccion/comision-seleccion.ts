import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { ComisionService } from '../../../core/services/convocatorias/comision-service';
import { MiembroComision, ComisionEstudiante } from '../../../core/models/convocatoria/comision';

@Component({
  selector: 'app-mi-comision-estudiante',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './comision-seleccion.html',
  styleUrls: ['./comision-seleccion.css']
})

export class ComisionSeleccion implements OnInit {
  private authSrv    = inject(AuthService);
  private comisionSrv = inject(ComisionService);

  loading    = true;
  errorMsg   = '';
  comisiones: ComisionEstudiante[] = [];

  formatFecha(f: string | null | undefined): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  iconForCargo(cargo: string): string {
    const map: Record<string, string> = {
      DECANO:      'shield-check',
      COORDINADOR: 'badge-check',
      DOCENTE:     'book-open'
    };
    return map[cargo?.toUpperCase()] ?? 'user';
  }

  labelForCargo(cargo: string): string {
    const map: Record<string, string> = {
      DECANO:      'Decano de Facultad',
      COORDINADOR: 'Coordinador de Carrera',
      DOCENTE:     'Docente Especialista'
    };
    return map[cargo?.toUpperCase()] ?? cargo;
  }

  ngOnInit(): void {
    const user = this.authSrv.getUser();
    if (!user) {
      this.errorMsg = 'No hay sesión activa.';
      this.loading  = false;
      return;
    }

    this.comisionSrv.obtenerDetalle(user.idUsuario, 'ESTUDIANTE').subscribe({
      next: res => {
        this.loading = false;
        if (!res.exito) {
          this.errorMsg = res.mensaje ?? 'No se encontró comisión asignada.';
          return;
        }
        this.comisiones = res.comisiones ?? [];
      },
      error: (err: Error) => {
        this.loading  = false;
        this.errorMsg = err.message;
      }
    });
  }

  ordenadosMiembros(miembros: MiembroComision[]): MiembroComision[] {
    const orden = ['DECANO', 'COORDINADOR', 'DOCENTE'];
    return [...miembros].sort(
      (a, b) => orden.indexOf(a.cargo?.toUpperCase()) - orden.indexOf(b.cargo?.toUpperCase())
    );
  }
}
