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
import { NotificacionesComponent as PostulanteNotificaciones } from './features/postulante/notificaciones/notificaciones.component';

// Decano
import { DashboardComponent as DecanoDashboard } from './features/decano/dashboard/dashboard.component';
import { ConvocatoriasVistaComponent as DecanoConvocatorias } from './features/decano/convocatorias-vista/convocatorias-vista.component';
import { PostulantesVistaComponent as DecanoPostulantes } from './features/decano/postulantes-vista/postulantes-vista.component';
import { ComisionesDecanoComponent } from './features/decano/comisiones/comisiones.component';
import { AuditoriaComponent as DecanoAuditoria } from './features/decano/auditoria/auditoria';

// Coordinador
import { DashboardComponent as CoordinadorDashboard } from './features/coordinador/dashboard/dashboard.component';
import { CoordinadorConvocatoriasComponent } from './features/coordinador/convocatorias/convocatorias.component';
import { PostulantesVistaComponent as CoordinadorPostulantes } from './features/coordinador/postulantes-vista/postulantes-vista.component';
import { ValidacionesComponent } from './features/coordinador/validaciones/validaciones.component';
import { SeguimientoComponent } from './features/coordinador/seguimiento/seguimiento.component';
import { ResolucionesComponent } from './features/coordinador/resoluciones/resoluciones.component';
import { EvaluacionesComponent } from './features/coordinador/evaluaciones/evaluaciones.component';
import { ReportesComponent as CoordinadorReportes } from './features/coordinador/reportes/reportes';
import { SelectorOposicionComponent } from './features/coordinador/selector-oposicion-component/selector-oposicion-component';
import { GestionOposicionComponent }  from './features/coordinador/gestion-oposicion-component/gestion-oposicion-component';

// Ayudante
import { DashboardComponent as AyudanteDashboard } from './features/ayudante/dashboard/dashboard.component';
import { ActividadesComponent as AyudanteActividades } from './features/ayudante/actividades/actividades.component';
import { InformesComponent as AyudanteInformes } from './features/ayudante/informes/informes.component';
import { SesionesComponent } from './features/ayudante/sesiones/sesiones';

// General
import { NotificacionesPageComponent } from './features/notificaciones/notificaciones-page.component';
import { DocenteDashboardComponent } from './features/docente/dashboard/docente-dashboard.component';
import { MisAyudantesComponent } from './features/docente/mis-ayudantes/mis-ayudantes.component';
import { ActividadesAyudanteComponent } from './features/docente/actividades-ayudante/actividades-ayudante';
import { CronogramaActivoComponent } from './features/General/cronograma-activo/cronograma-activo.component';
import { ComisionSeleccion } from './features/postulante/comision-seleccion/comision-seleccion';
import { GestionEvaluacionesComponent } from './features/General/gestion-evaluaciones/gestion-evaluaciones';
import { MiOposicionEstudianteComponent } from './features/postulante/mi-oposicion-estudiante/mi-oposicion-estudiante';
import { SalaEvaluacionComponent } from './features/evaluacionOposicion/sala-evaluacion-component/sala-evaluacion-component';
import {CargaAcademicaComponent} from './features/admin/carga-academica/carga-academica';
import { salaEvaluacionGuard } from './features/evaluacionOposicion/sala-evaluacion-guard-guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: MainLayoutComponent,
    children: [

      // ── Global ─────────────────────────────────────────────
      { path: 'notificaciones',                component: NotificacionesPageComponent },
      { path: 'cronograma',                    component: CronogramaActivoComponent },
      { path: 'comision',                      component: GestionEvaluacionesComponent },
      { path: 'comision/sala', component: SalaEvaluacionComponent, canDeactivate: [salaEvaluacionGuard] },
      { path: 'comision/sala/:idConvocatoria', component: SalaEvaluacionComponent, canDeactivate: [salaEvaluacionGuard] },


      // ── Admin ───────────────────────────────────────────────
      { path: 'admin/consulta',      component: AdminDashboard },
      { path: 'admin/usuarios',      component: GestionUsuarios },
      { path: 'admin/configuracion', component: GestionCatalogosComponent },
      { path: 'admin/carga-academica', component: CargaAcademicaComponent},
      { path: 'admin/rol-permiso',   component: GestionPermisosComponent },
      { path: 'admin/periodos',      component: GestionPeriodosComponent },

      // ── Postulante / Estudiante ─────────────────────────────
      { path: 'postulante/dashboard',         component: PostulanteDashboard },
      { path: 'postulante/convocatorias',     component: ConvocatoriasComponent },
      { path: 'postulante/mis-postulaciones', component: EstadoPostulacionComponent },
      { path: 'postulante/resultados',        component: ResultadosComponent },
      { path: 'postulante/meritos/:id',       component: MeritosComponent },
      { path: 'postulante/oposicion/:id',     component: MiOposicionEstudianteComponent },
      { path: 'postulante/comision',          component: ComisionSeleccion },

      // ── Decano ──────────────────────────────────────────────
      { path: 'decano/dashboard',                   component: DecanoDashboard },
      { path: 'decano/convocatorias',               component: DecanoConvocatorias },
      { path: 'decano/postulantes/:idConvocatoria', component: DecanoPostulantes },
      { path: 'decano/comisiones',                  component: ComisionesDecanoComponent },
      { path: 'decano/reportes',                    component: DecanoAuditoria },

      // ── Coordinador ─────────────────────────────────────────
      { path: 'coordinador/dashboard',                   component: CoordinadorDashboard },
      { path: 'coordinador/convocatorias',               component: CoordinadorConvocatoriasComponent },
      { path: 'coordinador/postulantes/:idConvocatoria', component: CoordinadorPostulantes },
      { path: 'coordinador/validaciones',                component: ValidacionesComponent },
      { path: 'coordinador/oposicion',                   component: SelectorOposicionComponent },
      { path: 'coordinador/oposicion/:idConvocatoria',   component: GestionOposicionComponent },
      { path: 'coordinador/seguimiento',   component: SeguimientoComponent },
      { path: 'coordinador/resoluciones',  component: ResolucionesComponent },
      { path: 'coordinador/evaluaciones',  component: EvaluacionesComponent },
      { path: 'coordinador/reportes',      component: CoordinadorReportes },
      { path: 'coordinador/notifications', component: PostulanteNotificaciones },

      // ── Ayudante ────────────────────────────────────────────
      { path: 'ayudante/dashboard',   component: AyudanteDashboard },
      { path: 'ayudante/actividades', component: AyudanteActividades },
      { path: 'ayudante/informes',    component: AyudanteInformes },
      { path: 'ayudante/sesiones',    component: SesionesComponent },

      // ── Docente ─────────────────────────────────────────────
      { path: 'docente/dashboard',                              component: DocenteDashboardComponent },
      { path: 'docente/mis-ayudantes',                          component: MisAyudantesComponent },
      { path: 'docente/aprobar-informes',                       component: MisAyudantesComponent },
      { path: 'docente/mis-ayudantes/:idAyudantia/actividades', component: ActividadesAyudanteComponent },

      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },

  { path: '**', redirectTo: 'login' },
];
