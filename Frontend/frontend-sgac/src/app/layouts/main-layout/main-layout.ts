import { Component, inject, signal, OnInit } from '@angular/core';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar';
import { HeaderComponent } from '../../shared/components/header/header';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth-service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterOutlet],
  templateUrl: './main-layout.html'
})
export class MainLayoutComponent implements OnInit {
  private router = inject(Router);
  private auth = inject(AuthService);
  authService = inject(AuthService);

  breadcrumbs = signal<string[]>(['Inicio']);

  ngOnInit() {

    console.log(this.authService.getUser()?.rolActual);

    this.buildBreadcrumbs(this.router.url);

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.buildBreadcrumbs(event.urlAfterRedirects);
      });
  }

  private buildBreadcrumbs(url: string) {
    const parts = url.split('/').filter(p => p);
    const crumbs = parts.map(p =>
      p.charAt(0).toUpperCase() + p.replace('-', ' ').slice(1)
    );

    this.breadcrumbs.set(['Inicio', ...crumbs.slice(1)]);
  }
}
