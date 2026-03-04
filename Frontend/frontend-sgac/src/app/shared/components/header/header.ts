import { Component, inject, computed, effect, signal, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { NotificacionService } from '../../../core/services/notificacion-service';
import { NotificacionResponseDTO } from '../../../core/dto/notificacion';
import { toSignal } from '@angular/core/rxjs-interop';
import { NotificationBellComponent } from '../notification-bell/notification-bell';

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
  private notificacionService = inject(NotificacionService);

  user = computed(() => this.authService.getUser());

  constructor() {
    // Conectar WS en cuanto tengamos usuario logeado.
    effect(() => {
      const u = this.user();
      if (u?.idUsuario) {
        this.notificacionService.conectar(u.idUsuario);
      }
    });
  }

  private notifService = inject(NotificacionService);

  // Notificaciones
  notificaciones = signal<NotificacionResponseDTO[]>([]);
  dropdownAbierto = signal(false);
  cargando = signal(false);

  get noLeidas(): number {
    return this.notificaciones().filter(n => !n.leido).length;
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
    this.cargarNotificaciones();
  }

  cargarNotificaciones(): void {
    const user = this.authService.getUser();
    if (!user?.idUsuario) return;
    this.cargando.set(true);
    this.notifService.obtenerNotificaciones().subscribe({
      next: (data: NotificacionResponseDTO[]) => { this.notificaciones.set(data); this.cargando.set(false); },
      error: () => { this.cargando.set(false); }
    });
  }

  toggleDropdown(): void {
    this.dropdownAbierto.update(v => !v);
    if (this.dropdownAbierto()) this.cargarNotificaciones();
  }

  marcarLeida(notif: NotificacionResponseDTO, event: Event): void {
    event.stopPropagation();
    if (notif.leido) return;
    this.notifService.marcarComoLeida(notif.idNotificacion).subscribe(() => {
      this.notificaciones.update(list =>
        list.map(n => n.idNotificacion === notif.idNotificacion ? { ...n, leido: true } : n)
      );
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.notif-wrapper')) {
      this.dropdownAbierto.set(false);
    }
  }

  truncar(msg: string, max = 65): string {
    return msg.length > max ? msg.slice(0, max) + '...' : msg;
  }
}
