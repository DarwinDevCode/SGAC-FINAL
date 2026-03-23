import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { PostulanteService } from '../../../core/services/postulaciones/postulante-service';
import { NotificationWSService } from '../../../core/services/notification-ws-service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, LucideAngularModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class SidebarComponent {
  private authService    = inject(AuthService);
  private router         = inject(Router);
  private notificationWS = inject(NotificationWSService);
  public  postulanteService = inject(PostulanteService);

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

  menus: Record<string, { label: string; icon: string; route: string; exact?: boolean }[]> = {
    ESTUDIANTE: [
      { label: 'Inicio',            icon: 'LayoutDashboard', route: '/postulante/dashboard' },
      { label: 'Cronograma',        icon: 'CalendarDays',    route: '/cronograma' },
      { label: 'Convocatorias',     icon: 'FileText',        route: '/postulante/convocatorias' },
      { label: 'Documentos',        icon: 'Library',         route: '/documentos-visor' },
      { label: 'Mis Postulaciones', icon: 'FolderOpen',      route: '/postulante/mis-postulaciones' },
      { label: 'Tribunal',          icon: 'Gavel',           route: '/postulante/comision' },
      { label: 'Ver Resultados',    icon: 'Trophy',          route: '/resultados-evaluacion' },
      { label: 'Notificaciones',    icon: 'Bell',            route: '/notificaciones' },
    ],
    AYUDANTE_CATEDRA: [
      { label: 'Inicio',             icon: 'LayoutDashboard', route: '/ayudante/dashboard' },
      { label: 'Cronograma',         icon: 'CalendarDays',    route: '/cronograma' },
      { label: 'Documentos',         icon: 'Library',         route: '/documentos-visor' },
      { label: 'Mi Padrón',          icon: 'Users',           route: '/ayudante/padron' },
      { label: 'Mis Sesiones',       icon: 'CalendarClock',   route: '/ayudante/sesiones' },
      { label: 'Matriz de Asistencia', icon: 'Table2',        route: '/ayudante/asistencia/matriz' },
      { label: 'Mis Informes',       icon: 'FileText',        route: '/ayudante/informes' },
      { label: 'Notificaciones',     icon: 'Bell',            route: '/notificaciones' },
    ],
    DOCENTE: [
      { label: 'Inicio',                   icon: 'LayoutDashboard', route: '/docente/dashboard' },
      { label: 'Cronograma',               icon: 'CalendarDays',    route: '/cronograma' },
      { label: 'Documentos',               icon: 'Library',         route: '/documentos-visor' },
      { label: 'Tribunal',                 icon: 'Gavel',           route: '/comision',      exact: true },
      { label: 'Sala de Evaluación',       icon: 'MicVocal',        route: '/comision/sala' },
      { label: 'Ver Resultados',           icon: 'Trophy',          route: '/resultados-evaluacion' },
      { label: 'Mis Ayudantes',            icon: 'Users',           route: '/docente/mis-ayudantes' },

      { label: 'Aprobar Informes',         icon: 'CheckSquare',     route: '/docente/aprobar-informes' },
      { label: 'Informe Final',            icon: 'FileCheck',      route: '/docente/aprobar-informes-mensuales' },
      { label: 'Notificaciones',           icon: 'Bell',            route: '/notificaciones' },
    ],
    COORDINADOR: [
      { label: 'Inicio',                 icon: 'LayoutDashboard', route: '/coordinador/dashboard' },
      { label: 'Cronograma',             icon: 'CalendarDays',    route: '/cronograma' },
      { label: 'Gestión Documentos',     icon: 'Files',           route: '/documentos-gestion' },
      { label: 'Gestionar Convocatorias',icon: 'FileText',        route: '/coordinador/convocatorias' },
      { label: 'Validar Postulantes',    icon: 'CheckSquare',     route: '/coordinador/validaciones' },
      { label: 'Tribunal',               icon: 'Gavel',           route: '/comision',      exact: true },
      { label: 'Evaluación de Méritos',  icon: 'Star',            route: '/coordinador/evaluacion-meritos' },
      { label: 'Gestionar Oposición',    icon: 'ScrollText',      route: '/coordinador/oposicion' },
      { label: 'Sala de Evaluación',     icon: 'MicVocal',        route: '/comision/sala' },
      { label: 'Ver Resultados',         icon: 'Trophy',          route: '/resultados-evaluacion' },
      { label: 'Resoluciones y Actas',   icon: 'FileSignature',   route: '/coordinador/resoluciones' },
      { label: 'Reportes y Consultas',   icon: 'ClipboardList',   route: '/coordinador/reportes' },
      { label: 'Informe Final',          icon: 'FileCheck',      route: '/docente/aprobar-informes-mensuales' },
      { label: 'Notificaciones',         icon: 'Bell',            route: '/notificaciones' },
    ],
    COMISION_SELECCION: [
      { label: 'Inicio',                icon: 'LayoutDashboard', route: '/comision/dashboard' },
      { label: 'Cronograma',            icon: 'CalendarDays',    route: '/cronograma' },
      { label: 'Evaluar Méritos',       icon: 'FileText',        route: '/comision/meritos' },
      { label: 'Evaluar Oposición',     icon: 'Users',           route: '/comision/oposicion' },
      { label: 'Ranking de Resultados', icon: 'BarChart3',       route: '/comision/ranking' },
      { label: 'Notificaciones',        icon: 'Bell',            route: '/notificaciones' },
    ],
    DECANO: [
      { label: 'Inicio',               icon: 'LayoutDashboard', route: '/decano/dashboard' },
      { label: 'Cronograma',           icon: 'CalendarDays',    route: '/cronograma' },
      { label: 'Documentos',           icon: 'Library',         route: '/documentos-visor' },
      { label: 'Tribunal',             icon: 'Gavel',           route: '/comision',      exact: true },
      { label: 'Sala de Evaluación',   icon: 'MicVocal',        route: '/comision/sala' },
      { label: 'Ver Resultados',       icon: 'Trophy',          route: '/resultados-evaluacion' },
      { label: 'Auditoría y Reportes', icon: 'BarChart3',       route: '/decano/reportes' },
      { label: 'Informe Final',        icon: 'FileCheck',      route: '/docente/aprobar-informes-mensuales' },
      { label: 'Notificaciones',       icon: 'Bell',            route: '/notificaciones' },
    ],
    ADMINISTRADOR: [
      { label: 'Inicio',              icon: 'LayoutDashboard', route: '/admin/dashboard' },
      { label: 'Gestión Usuarios',    icon: 'Users',           route: '/admin/usuarios' },
      { label: 'Carga Académica',     icon: 'Briefcase',       route: '/admin/carga-academica' },
      { label: 'Gestión Documentos',  icon: 'Files',           route: '/documentos-gestion' },
      { label: 'Periodos Académicos', icon: 'CalendarClock',   route: '/admin/periodos' },
      { label: 'Cronograma',          icon: 'CalendarDays',    route: '/cronograma' },
      { label: 'Configuración Global',icon: 'Settings',        route: '/admin/configuracion' },
      { label: 'Roles y Permisos',    icon: 'Settings',        route: '/admin/rol-permiso' },
      { label: 'Notificaciones',      icon: 'Bell',            route: '/notificaciones' },
    ],
  };

  currentMenu = computed(() => this.menus[this.userRole()] || []);

  logout() {
    this.notificationWS.desconectar();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
