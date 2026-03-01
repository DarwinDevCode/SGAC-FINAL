import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, LucideAngularModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  //userRole = computed(() => this.authService.getUser()?.rolActual || 'ESTUDIANTE');


  private normalizeRole(rawRole?: string | null): string {
    if (!rawRole) return 'ESTUDIANTE';

    return rawRole
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toUpperCase()
      .replace(/^ROLE_/, '')
      .replace(/[\s-]+/g, '_');
  }

  userRole = computed(() => this.normalizeRole(this.authService.getUser()?.rolActual));

  menus: Record<string, any[]> = {
    ESTUDIANTE: [
      { label: 'Inicio', icon: 'LayoutDashboard', route: '/postulante/dashboard' },
      { label: 'Convocatorias', icon: 'FileText', route: '/postulante/convocatorias' },
      { label: 'Mis Postulaciones', icon: 'FolderOpen', route: '/postulante/mis-postulaciones' },
      { label: 'Ver Resultados', icon: 'Award', route: '/postulante/resultados' },
      { label: 'Notificaciones', icon: 'Bell', route: '/postulante/notificaciones' },
    ],
    AYUDANTE_CATEDRA: [
      { label: 'Inicio', icon: 'LayoutDashboard', route: '/ayudante/dashboard' },
      { label: 'Mis sesiones', icon: 'CalendarClock', route: '/ayudante/actividades' },
      { label: 'Mis Informes', icon: 'FileText', route: '/ayudante/informes' },
      { label: 'Notificaciones', icon: 'Bell', route: '/ayudante/notifications' },
    ],
    DOCENTE: [
      { label: 'Inicio', icon: 'LayoutDashboard', route: '/docente/dashboard' },
      { label: 'Mis Ayudantes', icon: 'Users', route: '/docente/mis-ayudantes' },
      { label: 'Planificación Actividades', icon: 'CalendarClock', route: '/docente/planificacion' },
      { label: 'Aprobar Informes', icon: 'CheckSquare', route: '/docente/validar-informes' },
      { label: 'Notificaciones', icon: 'Bell', route: '/docente/notifications' },
    ],
    COORDINADOR: [
      { label: 'Inicio', icon: 'LayoutDashboard', route: '/coordinador/dashboard' },
      { label: 'Gestionar Convocatorias', icon: 'FileText', route: '/coordinador/convocatorias' },
      { label: 'Validar Postulantes', icon: 'CheckSquare', route: '/coordinador/validaciones' },
      { label: 'Seguimiento Mensual', icon: 'BarChart3', route: '/coordinador/seguimiento' },
      { label: 'Resoluciones y Actas', icon: 'FileSignature', route: '/coordinador/resoluciones' },
      { label: 'Notificaciones', icon: 'Bell', route: '/coordinador/notifications' },
    ],
    COMISION_SELECCION: [
      { label: 'Inicio', icon: 'LayoutDashboard', route: '/comision/dashboard' },
      { label: 'Evaluar Méritos', icon: 'FileText', route: '/comision/meritos' },
      { label: 'Evaluar Oposición', icon: 'Users', route: '/comision/oposicion' },
      { label: 'Ranking de Resultados', icon: 'BarChart3', route: '/comision/ranking' },
      { label: 'Notificaciones', icon: 'Bell', route: '/comision/notifications' },
    ],
    DECANO: [
      { label: 'Inicio', icon: 'LayoutDashboard', route: '/decano/dashboard' },
      { label: 'Designar Comisiones', icon: 'Users', route: '/decano/comisiones' },
      { label: 'Firma Electrónica', icon: 'FileSignature', route: '/decano/firmas' },
      { label: 'Auditoría y Reportes', icon: 'BarChart3', route: '/decano/reportes' },
      { label: 'Notificaciones', icon: 'Bell', route: '/decano/notifications' },
    ],
    ADMINISTRADOR: [
      { label: 'Inicio', icon: 'LayoutDashboard', route: '/admin/dashboard' },
      { label: 'Gestión Usuarios', icon: 'Users', route: '/admin/usuarios' },
      { label: 'Periodos Académicos', icon: 'CalendarClock', route: '/admin/periodos' },
      { label: 'Configuración Global', icon: 'Settings', route: '/admin/configuracion' },
      { label: 'Roles y Permisos', icon: 'Settings', route: '/admin/rol-permiso' },
      { label: 'Notificaciones', icon: 'Bell', route: '/admin/notifications' },
    ],
  };

  currentMenu = computed(() => this.menus[this.userRole()] || []);

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
