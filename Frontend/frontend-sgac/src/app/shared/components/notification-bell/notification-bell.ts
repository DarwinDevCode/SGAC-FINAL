import { CommonModule } from '@angular/common';
import { Component, HostListener, inject, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Notificacion } from '../../../core/dto/notificacion';
import { NotificationWSService } from '../../../core/services/notification-ws-service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.css'
})
export class NotificationBellComponent {
  private readonly notificacionService = inject(NotificationWSService);
  private readonly router = inject(Router);

  readonly panelOpen = signal(false);

  readonly notificaciones = toSignal(this.notificacionService.notificaciones$, { initialValue: [] });
  readonly unreadCount = toSignal(this.notificacionService.unreadCount$, { initialValue: 0 });

  readonly unreadCountText = computed(() => {
    const c = this.unreadCount();
    return c > 9 ? '9+' : String(c);
  });

  togglePanel(): void {
    this.panelOpen.update((v) => !v);
  }

  closePanel(): void {
    this.panelOpen.set(false);
  }

  marcarLeida(notif: Notificacion, event?: Event): void {
    event?.stopPropagation();
    if (notif.leido) {
      return;
    }
    this.notificacionService.marcarComoLeida(notif.idNotificacion).subscribe();
  }

  onClickNotificacion(notif: Notificacion): void {
    this.marcarLeida(notif);

    // Navegación según tipo
    if (notif.tipo === 'OBSERVACION' && notif.idReferencia != null) {
      // Ajusta la ruta real de tu feature. Dejo una ruta base razonable.
      this.router.navigate(['/ayudante/sesiones'], {
        queryParams: { idRegistroActividad: notif.idReferencia }
      });
      this.closePanel();
      return;
    }

    // Si no hay navegación, solo cerrar panel.
    this.closePanel();
  }

  getItemClass(notif: Notificacion): string {
    if (notif.tipo === 'OBSERVACION') return 'notif-item notif-observacion';
    if (notif.tipo === 'APROBACION') return 'notif-item notif-aprobacion';
    return 'notif-item';
  }

  getIconName(notif: Notificacion): string {
    if (notif.tipo === 'OBSERVACION') return 'AlertTriangle';
    if (notif.tipo === 'APROBACION') return 'CircleCheck';
    return 'Bell';
  }

  formatFecha(iso: string): string {
    // iso puede venir como Instant
    try {
      const d = new Date(iso);
      return d.toLocaleString();
    } catch {
      return iso;
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (!target) return;

    // Cierra si click fuera del componente
    if (!target.closest('.notif-wrapper')) {
      this.closePanel();
    }
  }
}
