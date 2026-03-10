import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { PostulanteService } from '../../../core/services/postulante-service';
import { NotificacionResponseDTO } from '../../../core/dto/notificacion';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './notificaciones.html',
  styleUrl: './notificaciones.css',
})
export class NotificacionesComponent implements OnInit {
  private authService = inject(AuthService);
  private postulanteService = inject(PostulanteService);

  todasNotificaciones: NotificacionResponseDTO[] = [];
  loading = true;
  errorMensaje = '';

  /** Filtro activo: 'todas' | 'no_leidas' | 'leidas' */
  filtroActivo: 'todas' | 'no_leidas' | 'leidas' = 'todas';

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (!user) { this.loading = false; return; }
    this.postulanteService.listarNotificaciones(user.idUsuario).subscribe({
      next: (data) => { this.todasNotificaciones = data || []; this.loading = false; },
      error: () => { this.errorMensaje = 'No se cargaron notificaciones.'; this.loading = false; }
    });
  }

  get notificaciones(): NotificacionResponseDTO[] {
    if (this.filtroActivo === 'no_leidas') return this.todasNotificaciones.filter(n => !n.leido);
    if (this.filtroActivo === 'leidas') return this.todasNotificaciones.filter(n => n.leido);
    return this.todasNotificaciones;
  }

  get countNoLeidas(): number { return this.todasNotificaciones.filter(n => !n.leido).length; }
  get countLeidas(): number { return this.todasNotificaciones.filter(n => n.leido).length; }

  setFiltro(f: 'todas' | 'no_leidas' | 'leidas') { this.filtroActivo = f; }

  marcarLeida(id: number) {
    this.postulanteService.marcarNotificacionLeida(id).subscribe({
      next: () => {
        const n = this.notificaciones.find(n => n.idNotificacion === id);
        if (n) n.leido = true;
      }
    });
  }

  marcarTodasLeidas() {
    const noLeidas = this.todasNotificaciones.filter(n => !n.leido);
    noLeidas.forEach(n => this.marcarLeida(n.idNotificacion));
  }
}
