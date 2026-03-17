import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService, PreAuthData } from '../../../core/services/auth-service';

interface RolConfig {
  icono: string;
  etiqueta: string;
  descripcion: string;
  claseColor: string;
}

const ROLES_CONFIG: Record<string, RolConfig> = {
  ESTUDIANTE: {
    icono: 'user',
    etiqueta: 'Estudiante',
    descripcion: 'Mis postulaciones y carrera',
    claseColor: 'rol-estudiante',
  },
  COORDINADOR: {
    icono: 'settings',
    etiqueta: 'Coordinador',
    descripcion: 'Gestión de convocatorias',
    claseColor: 'rol-coordinador',
  },
  ADMINISTRADOR: {
    icono: 'shield-check',
    etiqueta: 'Administrador',
    descripcion: 'Control total del sistema',
    claseColor: 'rol-admin',
  },
  DECANO: {
    icono: 'landmark',
    etiqueta: 'Decano',
    descripcion: 'Supervisión de facultad',
    claseColor: 'rol-decano',
  },
  DOCENTE: {
    icono: 'book-open',
    etiqueta: 'Docente',
    descripcion: 'Evaluación de postulantes',
    claseColor: 'rol-docente',
  },
  AYUDANTE_CATEDRA: {
    icono: 'clipboard-list',
    etiqueta: 'Ayudante',
    descripcion: 'Asistencia y actividades',
    claseColor: 'rol-ayudante',
  },
};

const ROL_RUTAS: Record<string, string> = {
  ADMINISTRADOR: '/admin/dashboard',
  ESTUDIANTE: '/postulante/dashboard',
  DOCENTE: '/docente/dashboard',
  COORDINADOR: '/coordinador/dashboard',
  DECANO: '/decano/dashboard',
  AYUDANTE_CATEDRA: '/ayudante/dashboard',
};

@Component({
  selector: 'app-selector-rol',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './selector-rol-component.html',
  styleUrl: './selector-rol-component.css',
})
export class SelectorRolComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  preAuthData: PreAuthData | null = null;
  cargandoRol: string | null = null;
  errorMsg = '';

  ngOnInit(): void {
    this.preAuthData = this.authService.getPreAuthData();
    if (!this.preAuthData) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.preAuthData.roles.length === 1) {
      this.seleccionar(this.preAuthData.roles[0].nombreTipoRol);
    }
  }

  getConfig(nombreRol: string): RolConfig {
    return ROLES_CONFIG[nombreRol] ?? {
      icono: 'circle-user',
      etiqueta: nombreRol,
      descripcion: 'Acceder al sistema',
      claseColor: 'rol-default',
    };
  }

  seleccionar(nombreRol: string): void {
    if (this.cargandoRol) return;
    this.cargandoRol = nombreRol;

    this.authService.seleccionarRol(nombreRol).subscribe({
      next: () => {
        const ruta = ROL_RUTAS[nombreRol] ?? '/dashboard';
        this.router.navigate([ruta]);
      },
      error: (err) => {
        this.cargandoRol = null;
        this.errorMsg = err.error?.mensaje || 'Error al activar el rol.';
      },
    });
  }

  volverAlLogin(): void {
    this.authService.logout();
  }
}
