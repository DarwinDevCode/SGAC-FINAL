import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth-service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  user = computed(() => this.authService.getUser());

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
}
