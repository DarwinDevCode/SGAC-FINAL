import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { toSignal } from '@angular/core/rxjs-interop';
import { NotificationWSService } from '../../core/services/notification-ws-service';
import { Notificacion } from '../../core/dto/notificacion';

@Component({
  selector: 'app-notificaciones-page',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './notificaciones-page.html',
  styleUrl: './notificaciones-page.css'
})
export class NotificacionesPageComponent {
  private readonly notifications = inject(NotificationWSService);

  readonly notificaciones = toSignal(this.notifications.notificaciones$, { initialValue: [] });
  readonly unreadCount = toSignal(this.notifications.unreadCount$, { initialValue: 0 });

  refrescar(): void {
    this.notifications.obtenerNotificaciones().subscribe();
  }

  marcarLeida(n: Notificacion): void {
    if (n.leido) return;
    this.notifications.marcarComoLeida(n.idNotificacion).subscribe();
  }

  marcarTodasLeidas(): void {
    const list = this.notificaciones();
    list.filter((n) => !n.leido).forEach((n) => this.marcarLeida(n));
  }

  trackById = (_: number, n: Notificacion) => n.idNotificacion;
}

