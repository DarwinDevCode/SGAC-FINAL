import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { PostulanteService } from '../../../core/services/postulante-service';
import { NotificacionResponseDTO } from '../../../core/dto/notificacion';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './notificaciones.html',
  styleUrl: './notificaciones.css',
})
export class NotificacionesComponent implements OnInit {
  private authService = inject(AuthService);
  private postulanteService = inject(PostulanteService);

  notificaciones: NotificacionResponseDTO[] = [];
  loading = true;
  errorMensaje = '';

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (!user) { this.loading = false; return; }
    this.postulanteService.listarNotificaciones(user.idUsuario).subscribe({
      next: (data) => { this.notificaciones = data || []; this.loading = false; },
      error: () => { this.errorMensaje = 'No se cargaron notificaciones.'; this.loading = false; }
    });
  }

  marcarLeida(id: number) {
    this.postulanteService.marcarNotificacionLeida(id).subscribe({
      next: () => {
        const n = this.notificaciones.find(n => n.idNotificacion === id);
        if (n) n.leida = true;
      }
    });
  }
}
