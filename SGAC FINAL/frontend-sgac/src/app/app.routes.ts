import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { AdminComponent } from './features/dashboard/admin/admin';
import { StudentComponent } from './features/dashboard/student/student';
import { DocenteComponent } from './features/dashboard/docente/docente';
import { CoordinadorComponent } from './features/dashboard/coordinador/coordinador';
import { DecanoComponent } from './features/dashboard/decano/decano';
import { AyudanteComponent } from './features/dashboard/ayudante/ayudante';
import {GestionUsuariosComponent} from './features/admin/gestion-usuarios/gestion-usuarios';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'admin/dashboard', component: AdminComponent },
      { path: 'student/dashboard', component: StudentComponent },
      { path: 'docente/dashboard', component: DocenteComponent },
      { path: 'coordinador/dashboard', component: CoordinadorComponent },
      { path: 'decano/dashboard', component: DecanoComponent },
      { path: 'ayudante/dashboard', component: AyudanteComponent },
      { path: 'admin/usuarios', component: GestionUsuariosComponent},
    ]
  },

  { path: '**', redirectTo: 'login' }
];
