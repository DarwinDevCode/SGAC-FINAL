import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { AdminDashboardComponent } from './features/dashboard/admin/admin';
import { GestionUsuarios } from './features/admin/gestion-usuarios/gestion-usuarios';
import {GestionCatalogosComponent} from './features/admin/gestion-catalogos/gestion-catalogos';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'admin/dashboard', component: AdminDashboardComponent },
      { path: 'admin/usuarios', component: GestionUsuarios },
      { path: 'admin/configuracion', component: GestionCatalogosComponent },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: 'login' }
];
