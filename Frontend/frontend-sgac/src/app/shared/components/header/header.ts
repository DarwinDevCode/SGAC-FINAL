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
  private router = inject(Router);
  private authService = inject(AuthService);
  private notificationWS = inject(NotificationWSService);

  user = computed(() => this.authService.getUser());

  constructor() {
    // Conectar WS en cuanto tengamos usuario logeado.
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

  breadcrumbs = computed(() => {
    const url = this.urlEvents() || '';
    const parts = url.split('/').filter(p => p);
    const names: Record<string, string> = {
      student: 'Estudiante',
      docente: 'Docente',
      coordinador: 'Coordinación',
      dashboard: 'Inicio',
      admin: 'Administrador'
    };
    return parts.map((part, index) => ({
      label: names[part] || part.charAt(0).toUpperCase() + part.slice(1),
      path: '/' + parts.slice(0, index + 1).join('/')
    }));
  });

  ngOnInit(): void {
    // no-op: la campana maneja su propio estado; el WS se conecta en el effect.
    //this.cargarNotificaciones();
  }


}
