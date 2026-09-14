import { Routes } from '@angular/router';
import { selectorRolGuard } from './core/guards/selector-rol-guard-guard';
import { salaEvaluacionGuard } from './features/evaluacionOposicion/sala-evaluacion-guard-guard';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'seleccionar-rol',
    canActivate: [selectorRolGuard],
    loadComponent: () => import('./features/auth/selector-rol-component/selector-rol-component').then(m => m.SelectorRolComponent)
  },
  {
    path: 'forbidden',
    loadComponent: () => import('./features/General/forbidden/forbidden.component').then(m => m.ForbiddenComponent)
  },

  {
    path: '',
    loadComponent: () => import('./layouts/main-layout/main-layout').then(m => m.MainLayoutComponent),
    canActivateChild: [authGuard],
    children: [

      // ── Global ───────────────────────────────────────────────────────
      {
        path: 'notificaciones',
        loadComponent: () => import('./features/notificaciones/notificaciones-page.component').then(m => m.NotificacionesPageComponent)
      },
      {
        path: 'cronograma',
        loadComponent: () => import('./features/General/cronograma-activo/cronograma-activo.component').then(m => m.CronogramaActivoComponent)
      },
      {
        path: 'comision',
        canActivate: [roleGuard],
        data: { roles: ['COORDINADOR', 'DOCENTE'] },
        loadComponent: () => import('./features/General/gestion-evaluaciones/gestion-evaluaciones').then(m => m.GestionEvaluacionesComponent)
      },
      {
        path: 'comision/sala',
        canActivate: [roleGuard],
        data: { roles: ['COORDINADOR', 'DOCENTE'] },
        canDeactivate: [salaEvaluacionGuard],
        loadComponent: () => import('./features/evaluacionOposicion/sala-evaluacion-component/sala-evaluacion-component').then(m => m.SalaEvaluacionComponent)
      },
      {
        path: 'comision/sala/:idConvocatoria',
        canActivate: [roleGuard],
        data: { roles: ['COORDINADOR', 'DOCENTE'] },
        canDeactivate: [salaEvaluacionGuard],
        loadComponent: () => import('./features/evaluacionOposicion/sala-evaluacion-component/sala-evaluacion-component').then(m => m.SalaEvaluacionComponent)
      },
      {
        path: 'resultados-evaluacion',
        loadComponent: () => import('./features/General/ranking-resultados-component/ranking-resultados-component').then(m => m.RankingResultadosComponent)
      },
      {
        path: 'documentos-visor',
        loadComponent: () => import('./features/General/documento-visor-component/documento-visor-component').then(m => m.DocumentoVisorComponent)
      },
      {
        path: 'documentos-gestion',
        canActivate: [roleGuard],
        data: { roles: ['COORDINADOR', 'ADMINISTRADOR'] },
        loadComponent: () => import('./features/General/documento-gestion-component/documento-gestion-component').then(m => m.DocumentoGestionComponent)
      },

      // ── Admin ────────────────────────────────────────────────────────
      {
        path: 'admin',
        canActivateChild: [roleGuard],
        data: { roles: ['ADMINISTRADOR'] },
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/admin/dashboard/dashboard').then(m => m.DashboardComponent)
          },
          {
            path: 'usuarios',
            loadComponent: () => import('./features/admin/gestion-usuarios/gestion-usuarios').then(m => m.GestionUsuarios)
          },
          {
            path: 'configuracion',
            loadComponent: () => import('./features/admin/gestion-catalogos/gestion-catalogos').then(m => m.GestionCatalogosComponent)
          },
          {
            path: 'reportes-auditoria',
            loadComponent: () => import('./features/admin/auditoria-component/auditoria-component').then(m => m.AuditoriaComponent)
          },
          {
            path: 'carga-academica',
            loadComponent: () => import('./features/admin/carga-academica/carga-academica').then(m => m.CargaAcademicaComponent)
          },
          {
            path: 'rol-permiso',
            loadComponent: () => import('./features/admin/gestion-permisos/gestion-permisos').then(m => m.GestionPermisosComponent)
          },
          {
            path: 'periodos',
            loadComponent: () => import('./features/admin/gestion-periodos/gestion-periodos.component').then(m => m.GestionPeriodosComponent)
          },
          {
            path: 'respaldos',
            loadComponent: () => import('./features/admin/respaldos-component/respaldos-component').then(m => m.RespaldosComponent)
          }
        ]
      },

      // ── Postulante / Estudiante ──────────────────────────────────────
      {
        path: 'postulante',
        canActivateChild: [roleGuard],
        data: { roles: ['ESTUDIANTE'] },
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/postulante/dashboard/dashboard.component').then(m => m.DashboardComponent)
          },
          {
            path: 'convocatorias',
            loadComponent: () => import('./features/postulante/convocatorias/convocatorias.component').then(m => m.ConvocatoriasComponent)
          },
          {
            path: 'mis-postulaciones',
            loadComponent: () => import('./features/postulante/estado-postulacion/estado-postulacion.component').then(m => m.EstadoPostulacionComponent)
          },
          {
            path: 'meritos/:id',
            loadComponent: () => import('./features/postulante/meritos/meritos.component').then(m => m.MeritosComponent)
          },
          {
            path: 'oposicion/:id',
            loadComponent: () => import('./features/postulante/mi-oposicion-estudiante/mi-oposicion-estudiante').then(m => m.MiOposicionEstudianteComponent)
          },
          {
            path: 'comision',
            loadComponent: () => import('./features/postulante/comision-seleccion/comision-seleccion').then(m => m.ComisionSeleccion)
          }
        ]
      },

      // ── Decano ───────────────────────────────────────────────────────
      {
        path: 'decano',
        canActivateChild: [roleGuard],
        data: { roles: ['DECANO'] },
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/decano/dashboard/dashboard.component').then(m => m.DashboardComponent)
          },
          {
            path: 'convocatorias',
            loadComponent: () => import('./features/decano/convocatorias-vista/convocatorias-vista.component').then(m => m.ConvocatoriasVistaComponent)
          },
          {
            path: 'postulantes/:idConvocatoria',
            loadComponent: () => import('./features/decano/postulantes-vista/postulantes-vista.component').then(m => m.PostulantesVistaComponent)
          },
          {
            path: 'comisiones',
            loadComponent: () => import('./features/decano/comisiones/comisiones.component').then(m => m.ComisionesDecanoComponent)
          },
          {
            path: 'reportes',
            loadComponent: () => import('./features/decano/auditoria/auditoria').then(m => m.AuditoriaComponent)
          }
        ]
      },

      // ── Coordinador ──────────────────────────────────────────────────
      {
        path: 'coordinador',
        canActivateChild: [roleGuard],
        data: { roles: ['COORDINADOR'] },
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/coordinador/dashboard/dashboard.component').then(m => m.DashboardComponent)
          },
          {
            path: 'convocatorias',
            loadComponent: () => import('./features/coordinador/convocatorias/convocatorias.component').then(m => m.CoordinadorConvocatoriasComponent)
          },
          {
            path: 'postulantes/:idConvocatoria',
            loadComponent: () => import('./features/coordinador/postulantes-vista/postulantes-vista.component').then(m => m.PostulantesVistaComponent)
          },
          {
            path: 'validaciones',
            loadComponent: () => import('./features/coordinador/validaciones/validaciones.component').then(m => m.ValidacionesComponent)
          },
          {
            path: 'evaluacion-meritos',
            loadComponent: () => import('./features/coordinador/selector-meritos-component/selector-meritos-component').then(m => m.SelectorMeritosComponent)
          },
          {
            path: 'evaluacion-meritos/:idPostulacion',
            loadComponent: () => import('./features/coordinador/evaluacion-meritos-component/evaluacion-meritos-component').then(m => m.EvaluacionMeritosComponent)
          },
          {
            path: 'oposicion',
            loadComponent: () => import('./features/coordinador/selector-oposicion-component/selector-oposicion-component').then(m => m.SelectorOposicionComponent)
          },
          {
            path: 'oposicion/:idConvocatoria',
            loadComponent: () => import('./features/coordinador/gestion-oposicion-component/gestion-oposicion-component').then(m => m.GestionOposicionComponent)
          },
          {
            path: 'seguimiento',
            loadComponent: () => import('./features/coordinador/seguimiento/seguimiento.component').then(m => m.SeguimientoComponent)
          },
          {
            path: 'resoluciones',
            loadComponent: () => import('./features/coordinador/resoluciones/resoluciones.component').then(m => m.ResolucionesComponent)
          },
          {
            path: 'evaluaciones',
            loadComponent: () => import('./features/coordinador/evaluaciones/evaluaciones.component').then(m => m.EvaluacionesComponent)
          },
          {
            path: 'reportes',
            loadComponent: () => import('./features/coordinador/reportes/reportes').then(m => m.ReportesComponent)
          },
          {
            path: 'notifications',
            loadComponent: () => import('./features/postulante/notificaciones/notificaciones.component').then(m => m.NotificacionesComponent)
          }
        ]
      },

      // ── Ayudante ──────────────────────────────────────────────────
      {
        path: 'ayudante',
        canActivateChild: [roleGuard],
        data: { roles: ['AYUDANTE_CATEDRA'] },
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/ayudante/dashboard/dashboard.component').then(m => m.DashboardComponent)
          },
          {
            path: 'actividades',
            loadComponent: () => import('./features/ayudante/actividades/actividades.component').then(m => m.ActividadesComponent)
          },
          {
            path: 'padron',
            loadComponent: () => import('../app/features/ayudante/padron-estudiantes-component/padron-estudiantes-component').then(m => m.PadronEstudiantesComponent)
          },
          {
            path: 'sesiones',
            loadComponent: () => import('../../src/app/features/ayudante/listado-sesiones-component/listado-sesiones-component').then(m => m.ListadoSesionesComponent)
          },
          {
            path: 'sesiones/detalle/:id',
            loadComponent: () => import('../../src/app/features/ayudante/detalle-sesion-component/detalle-sesion-component').then(m => m.DetalleSesionComponent)
          },
          {
            path: 'asistencia/matriz',
            loadComponent: () => import('./features/ayudante/matriz-asistencia-component/matriz-asistencia-component').then(m => m.MatrizAsistenciaComponent)
          }
        ]
      },

      // ── Docente ──────────────────────────────────────────────────────
      {
        path: 'docente',
        canActivateChild: [roleGuard],
        data: { roles: ['DOCENTE'] },
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/docente/dashboard/docente-dashboard.component').then(m => m.DocenteDashboardComponent)
          },
          {
            path: 'mis-ayudantes',
            loadComponent: () => import('./features/docente/mis-ayudantes/mis-ayudantes.component').then(m => m.MisAyudantesComponent)
          },
          {
            path: 'aprobar-informes',
            loadComponent: () => import('./features/docente/mis-ayudantes/mis-ayudantes.component').then(m => m.MisAyudantesComponent)
          },
          {
            path: 'mis-ayudantes/:idAyudantia/actividades',
            loadComponent: () => import('./features/docente/actividades-ayudante/actividades-ayudante').then(m => m.ActividadesAyudanteComponent)
          }
        ]
      },

      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },

  { path: '**', redirectTo: 'login' },
];
