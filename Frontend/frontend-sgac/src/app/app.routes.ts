import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { GestionUsuarios } from './features/admin/gestion-usuarios/gestion-usuarios';
import { GestionCatalogosComponent } from './features/admin/gestion-catalogos/gestion-catalogos';
import { GestionPermisosComponent } from './features/admin/gestion-permisos/gestion-permisos';
import { GestionPeriodosComponent } from './features/admin/gestion-periodos/gestion-periodos.component';
import { DashboardComponent as AdminDashboard } from './features/admin/dashboard/dashboard';

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
import { ComisionesDecanoComponent } from './features/decano/comisiones/comisiones.component';
import { AuditoriaComponent as DecanoAuditoria } from './features/decano/auditoria/auditoria';
// DecanoNotificaciones stub removido porque usaremos el global

// Coordinador
import { DashboardComponent as CoordinadorDashboard } from './features/coordinador/dashboard/dashboard.component';
import { CoordinadorConvocatoriasComponent } from './features/coordinador/convocatorias/convocatorias.component';
import { PostulantesVistaComponent as CoordinadorPostulantes } from './features/coordinador/postulantes-vista/postulantes-vista.component';
import { ValidacionesComponent } from './features/coordinador/validaciones/validaciones.component';
import { SeguimientoComponent } from './features/coordinador/seguimiento/seguimiento.component';
import { ResolucionesComponent } from './features/coordinador/resoluciones/resoluciones.component';
import { EvaluacionesComponent } from './features/coordinador/evaluaciones/evaluaciones.component';

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
      { path: 'admin/dashboard', component: AdminDashboard },
      { path: 'admin/usuarios', component: GestionUsuarios },
      { path: 'admin/configuracion', component: GestionCatalogosComponent },
      { path: 'admin/rol-permiso', component: GestionPermisosComponent },
      { path: 'admin/periodos', component: GestionPeriodosComponent },
      { path: 'admin/notifications', component: PostulanteNotificaciones },

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
      { path: 'decano/comisiones', component: ComisionesDecanoComponent },
      { path: 'decano/reportes', component: DecanoAuditoria },
      { path: 'decano/notifications', component: PostulanteNotificaciones },

      // Coordinador
      { path: 'coordinador/dashboard', component: CoordinadorDashboard },
      { path: 'coordinador/convocatorias', component: CoordinadorConvocatoriasComponent },
      { path: 'coordinador/postulantes/:idConvocatoria', component: CoordinadorPostulantes },
      { path: 'coordinador/validaciones', component: ValidacionesComponent },
      { path: 'coordinador/seguimiento', component: SeguimientoComponent },
      { path: 'coordinador/resoluciones', component: ResolucionesComponent },
      { path: 'coordinador/evaluaciones', component: EvaluacionesComponent }, // P13 (ítem 15)
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
