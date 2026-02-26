import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { GestionUsuarios } from './features/admin/gestion-usuarios/gestion-usuarios';
import { GestionCatalogosComponent } from './features/admin/gestion-catalogos/gestion-catalogos';
import { GestionPermisosComponent } from './features/admin/gestion-permisos/gestion-permisos';

// Postulante
import { DashboardComponent as PostulanteDashboard } from './features/postulante/dashboard/dashboard.component';
import { ConvocatoriasComponent } from './features/postulante/convocatorias/convocatorias.component';
import { EstadoPostulacionComponent } from './features/postulante/estado-postulacion/estado-postulacion.component';
import { ResultadosComponent } from './features/postulante/resultados/resultados.component';
import { MeritosComponent } from './features/postulante/meritos/meritos.component';
import { OposicionComponent } from './features/postulante/oposicion/oposicion.component';
import { NotificacionesComponent as PostulanteNotificaciones } from './features/postulante/notificaciones/notificaciones.component';

// Decano
import { DashboardComponent as DecanoDashboard } from './features/decano/dashboard/dashboard.component';
import { ConvocatoriasVistaComponent as DecanoConvocatorias } from './features/decano/convocatorias-vista/convocatorias-vista.component';
import { PostulantesVistaComponent as DecanoPostulantes } from './features/decano/postulantes-vista/postulantes-vista.component';

// Coordinador
import { DashboardComponent as CoordinadorDashboard } from './features/coordinador/dashboard/dashboard.component';
import { ConvocatoriasVistaComponent as CoordinadorConvocatoriasVista } from './features/coordinador/convocatorias-vista/convocatorias-vista.component';
import { CoordinadorConvocatoriasComponent } from './features/coordinador/convocatorias/convocatorias.component';
import { PostulantesVistaComponent as CoordinadorPostulantes } from './features/coordinador/postulantes-vista/postulantes-vista.component';
import { ValidacionesComponent } from './features/coordinador/validaciones/validaciones.component';
import { SeguimientoComponent } from './features/coordinador/seguimiento/seguimiento.component';
import { ResolucionesComponent } from './features/coordinador/resoluciones/resoluciones.component';

// Ayudante
import { DashboardComponent as AyudanteDashboard } from './features/ayudante/dashboard/dashboard.component';
import { ActividadesComponent as AyudanteActividades } from './features/ayudante/actividades/actividades.component';
import { InformesComponent as AyudanteInformes } from './features/ayudante/informes/informes.component';
import { AyudanteNotificacionesComponent } from './features/ayudante/notificaciones/notificaciones.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: MainLayoutComponent,
    children: [
      // Admin
      { path: 'admin/dashboard', redirectTo: 'admin/usuarios', pathMatch: 'full' },
      { path: 'admin/usuarios', component: GestionUsuarios },
      { path: 'admin/configuracion', component: GestionCatalogosComponent },
      { path: 'admin/rol-permiso', component: GestionPermisosComponent },

      // Postulante / Estudiante (routes for ESTUDIANTE role sidebar)
      { path: 'postulante/dashboard', component: PostulanteDashboard },
      { path: 'postulante/convocatorias', component: ConvocatoriasComponent },
      { path: 'postulante/mis-postulaciones', component: EstadoPostulacionComponent },
      { path: 'postulante/resultados', component: ResultadosComponent },
      { path: 'postulante/meritos/:id', component: MeritosComponent },
      { path: 'postulante/oposicion/:id', component: OposicionComponent },
      { path: 'postulante/notificaciones', component: PostulanteNotificaciones },

      // Decano
      { path: 'decano/dashboard', component: DecanoDashboard },
      { path: 'decano/convocatorias', component: DecanoConvocatorias },
      { path: 'decano/postulantes/:idConvocatoria', component: DecanoPostulantes },
      // Sidbar links for decano: comisiones, firmas, reportes, notifications
      { path: 'decano/comisiones', component: DecanoDashboard },  // stub → dashboard
      { path: 'decano/firmas', component: DecanoDashboard },      // stub → dashboard
      { path: 'decano/reportes', component: DecanoDashboard },    // stub → dashboard
      { path: 'decano/notifications', component: DecanoDashboard },

      // Coordinador
      { path: 'coordinador/dashboard', component: CoordinadorDashboard },
      { path: 'coordinador/convocatorias', component: CoordinadorConvocatoriasComponent },
      { path: 'coordinador/convocatorias-vista', component: CoordinadorConvocatoriasVista },
      { path: 'coordinador/postulantes/:idConvocatoria', component: CoordinadorPostulantes },
      { path: 'coordinador/validaciones', component: ValidacionesComponent },
      { path: 'coordinador/seguimiento', component: SeguimientoComponent },
      { path: 'coordinador/resoluciones', component: ResolucionesComponent },
      { path: 'coordinador/notifications', component: PostulanteNotificaciones },

      // Ayudante
      { path: 'ayudante/dashboard', component: AyudanteDashboard },
      { path: 'ayudante/actividades', component: AyudanteActividades },
      { path: 'ayudante/informes', component: AyudanteInformes },
      { path: 'ayudante/notifications', component: AyudanteNotificacionesComponent },

      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: 'login' }
];
