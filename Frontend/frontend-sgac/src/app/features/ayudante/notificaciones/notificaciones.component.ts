import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { PostulanteService } from '../../../core/services/postulante-service';
import { NotificacionResponseDTO } from '../../../core/dto/notificacion';

@Component({
  selector: 'app-ayudante-notificaciones',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="gestion-container">
      <div class="section-header">
        <div class="title-container">
          <h2 class="title">Notificaciones</h2>
          <p class="subtitle">Alertas y avisos del sistema SGAC.</p>
        </div>
      </div>

      @if (loading) {
        <div class="info-message">
          <lucide-icon name="loader-2" class="animate-spin" size="32"></lucide-icon>
          <p style="margin-top:.75rem">Cargando...</p>
        </div>
      } @else if (notificaciones.length === 0) {
        <div class="info-message" style="border:2px dashed hsl(var(--border));border-radius:var(--radius)">
          <lucide-icon name="bell" size="40" style="opacity:.3"></lucide-icon>
          <p style="margin-top:.75rem">No tienes notificaciones nuevas.</p>
        </div>
      } @else {
        <div style="display:flex;flex-direction:column;gap:.625rem">
          @for (n of notificaciones; track n.idNotificacion) {
            <div [class]="n.leida ? 'notif-item' : 'notif-item unread'">
              <div [class]="n.leida ? 'notif-dot' : 'notif-dot unread'">
                <lucide-icon name="bell" size="16"></lucide-icon>
              </div>
              <div style="flex:1;min-width:0">
                <p style="font-size:.875rem;margin:0" [style.fontWeight]="n.leida ? '400' : '600'">{{ n.mensaje }}</p>
                <p class="subtitle" style="margin:.25rem 0 0 0;font-size:.75rem">{{ n.fechaEnvio | date:'dd/MM/yyyy HH:mm' }}</p>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: []
})
export class AyudanteNotificacionesComponent implements OnInit {
  private authService = inject(AuthService);
  private postulanteService = inject(PostulanteService);

  notificaciones: NotificacionResponseDTO[] = [];
  loading = true;

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (!user) { this.loading = false; return; }
    this.postulanteService.listarNotificaciones(user.idUsuario).subscribe({
      next: (data) => { this.notificaciones = data || []; this.loading = false; },
      error: () => this.loading = false
    });
  }
}
