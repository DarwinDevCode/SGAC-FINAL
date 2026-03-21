// src/app/shared/components/header/header.ts
import { Component, inject, computed, effect, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { NotificationBellComponent } from '../notification-bell/notification-bell';
import { NotificationWSService } from '../../../core/services/notification-ws-service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, NotificationBellComponent],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent implements OnInit {
  private router         = inject(Router);
  private authService    = inject(AuthService);
  private notificationWS = inject(NotificationWSService);

  user = computed(() => this.authService.getUser());

  constructor() {
    effect(() => {
      const u = this.user();
      if (u?.idUsuario) {
        this.notificationWS.conectar(u.idUsuario);
      }
    });
  }

  private urlEvents = toSignal(
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(() => this.router.url)
    )
  );

  // ── Mapa completo segmento → etiqueta ────────────────────────────
  private readonly NAMES: Record<string, string> = {
    // Áreas / roles
    student:      'Estudiante',
    postulante:   'Postulante',
    docente:      'Docente',
    coordinador:  'Coordinación',
    decano:       'Decanato',
    admin:        'Administración',
    ayudante:     'Ayudante',
    comision:     'Comisión',

    // Secciones
    dashboard:              'Inicio',
    consulta:               'Consulta',
    convocatorias:          'Convocatorias',
    validaciones:           'Validaciones',
    oposicion:              'Gestión de Oposición',
    sala:                   'Sala de Evaluación',
    meritos:                'Méritos',
    'evaluacion-meritos':   'Evaluación de Méritos',
    'selector-meritos':     'Evaluación de Méritos',
    resoluciones:           'Resoluciones y Actas',
    reportes:               'Reportes',
    usuarios:               'Gestión de Usuarios',
    periodos:               'Períodos Académicos',
    configuracion:          'Configuración Global',
    'carga-academica':      'Carga Académica',
    'rol-permiso':          'Roles y Permisos',
    cronograma:             'Cronograma',
    notificaciones:         'Notificaciones',
    sesiones:               'Mis Sesiones',
    informes:               'Mis Informes',
    'mis-postulaciones':    'Mis Postulaciones',
    'mis-ayudantes':        'Mis Ayudantes',
    planificacion:          'Planificación de Actividades',
    'aprobar-informes':     'Aprobar Informes',
    'mi-oposicion':         'Mi Oposición',
    'resultados-evaluacion':'Ver Resultados',
  };

  /**
   * Devuelve SOLO el nombre de la página actual (último segmento no-numérico).
   * Esto evita que la cadena completa "Coordinación / Evaluación de Méritos"
   * desborde por debajo del sidebar fijo de 260 px.
   */
  paginaActual = computed((): string => {
    const url = this.urlEvents() ?? this.router.url ?? '';
    const parts = url
      .split('?')[0]          // quitar query-string
      .split('/')
      .filter(p => p && !/^\d+$/.test(p));  // quitar vacíos e IDs numéricos

    if (parts.length === 0) return 'Inicio';

    const ultimo = parts[parts.length - 1];
    return this.NAMES[ultimo]
      ?? (ultimo.charAt(0).toUpperCase() + ultimo.slice(1).replace(/-/g, ' '));
  });

  ngOnInit(): void { /* WS se conecta en el effect */ }
}
