import { Routes } from '@angular/router';
import { selectorRolGuard } from './core/guards/selector-rol-guard-guard';
import { salaEvaluacionGuard } from './features/evaluacionOposicion/sala-evaluacion-guard-guard';
import { coordinadorGuard } from './features/coordinador/auth';

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
    path: '',
    loadComponent: () => import('./layouts/main-layout/main-layout').then(m => m.MainLayoutComponent),
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
        loadComponent: () => import('./features/General/gestion-evaluaciones/gestion-evaluaciones').then(m => m.GestionEvaluacionesComponent)
      },
      {
        path: 'comision/sala',
        canDeactivate: [salaEvaluacionGuard],
        loadComponent: () => import('./features/evaluacionOposicion/sala-evaluacion-component/sala-evaluacion-component').then(m => m.SalaEvaluacionComponent)
      },
      {
        path: 'comision/sala/:idConvocatoria',
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
        loadComponent: () => import('./features/General/documento-gestion-component/documento-gestion-component').then(m => m.DocumentoGestionComponent)
      },

      // ── Admin ────────────────────────────────────────────────────────
      {
        path: 'admin/dashboard',
        loadComponent: () => import('./features/admin/dashboard/dashboard').then(m => m.DashboardComponent)
      },
      {
        path: 'admin/usuarios',
        loadComponent: () => import('./features/admin/gestion-usuarios/gestion-usuarios').then(m => m.GestionUsuarios)
      },
      {
        path: 'admin/configuracion',
        loadComponent: () => import('./features/admin/gestion-catalogos/gestion-catalogos').then(m => m.GestionCatalogosComponent)
      },
      {
        path: 'admin/reportes-auditoria',
        loadComponent: () => import('./features/admin/auditoria-component/auditoria-component').then(m => m.AuditoriaComponent)
      },
      {
        path: 'admin/carga-academica',
        loadComponent: () => import('./features/admin/carga-academica/carga-academica').then(m => m.CargaAcademicaComponent)
      },
      {
        path: 'admin/rol-permiso',
        loadComponent: () => import('./features/admin/gestion-permisos/gestion-permisos').then(m => m.GestionPermisosComponent)
      },
      {
        path: 'admin/periodos',
        loadComponent: () => import('./features/admin/gestion-periodos/gestion-periodos.component').then(m => m.GestionPeriodosComponent)
      },

      // ── Postulante / Estudiante ──────────────────────────────────────
      {
        path: 'postulante/dashboard',
        loadComponent: () => import('./features/postulante/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'postulante/convocatorias',
        loadComponent: () => import('./features/postulante/convocatorias/convocatorias.component').then(m => m.ConvocatoriasComponent)
      },
      {
        path: 'postulante/mis-postulaciones',
        loadComponent: () => import('./features/postulante/estado-postulacion/estado-postulacion.component').then(m => m.EstadoPostulacionComponent)
      },
      {
        path: 'postulante/meritos/:id',
        loadComponent: () => import('./features/postulante/meritos/meritos.component').then(m => m.MeritosComponent)
      },
      {
        path: 'postulante/oposicion/:id',
        loadComponent: () => import('./features/postulante/mi-oposicion-estudiante/mi-oposicion-estudiante').then(m => m.MiOposicionEstudianteComponent)
      },
      {
        path: 'postulante/comision',
        loadComponent: () => import('./features/postulante/comision-seleccion/comision-seleccion').then(m => m.ComisionSeleccion)
      },

      // ── Decano ───────────────────────────────────────────────────────
      {
        path: 'decano/dashboard',
        loadComponent: () => import('./features/decano/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'decano/convocatorias',
        loadComponent: () => import('./features/decano/convocatorias-vista/convocatorias-vista.component').then(m => m.ConvocatoriasVistaComponent)
      },
      {
        path: 'decano/postulantes/:idConvocatoria',
        loadComponent: () => import('./features/decano/postulantes-vista/postulantes-vista.component').then(m => m.PostulantesVistaComponent)
      },
      {
        path: 'decano/comisiones',
        loadComponent: () => import('./features/decano/comisiones/comisiones.component').then(m => m.ComisionesDecanoComponent)
      },
      {
        path: 'decano/reportes',
        loadComponent: () => import('./features/decano/auditoria/auditoria').then(m => m.AuditoriaComponent)
      },

      // ── Coordinador ──────────────────────────────────────────────────
      {
        path: 'coordinador/dashboard',
        loadComponent: () => import('./features/coordinador/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'coordinador/convocatorias',
        loadComponent: () => import('./features/coordinador/convocatorias/convocatorias.component').then(m => m.CoordinadorConvocatoriasComponent)
      },
      {
        path: 'coordinador/postulantes/:idConvocatoria',
        loadComponent: () => import('./features/coordinador/postulantes-vista/postulantes-vista.component').then(m => m.PostulantesVistaComponent)
      },
      {
        path: 'coordinador/validaciones',
        loadComponent: () => import('./features/coordinador/validaciones/validaciones.component').then(m => m.ValidacionesComponent)
      },
      {
        path: 'coordinador/evaluacion-meritos',
        canActivate: [coordinadorGuard],
        loadComponent: () => import('./features/coordinador/selector-meritos-component/selector-meritos-component').then(m => m.SelectorMeritosComponent)
      },
      {
        path: 'coordinador/evaluacion-meritos/:idPostulacion',
        canActivate: [coordinadorGuard],
        loadComponent: () => import('./features/coordinador/evaluacion-meritos-component/evaluacion-meritos-component').then(m => m.EvaluacionMeritosComponent)
      },
      {
        path: 'coordinador/oposicion',
        loadComponent: () => import('./features/coordinador/selector-oposicion-component/selector-oposicion-component').then(m => m.SelectorOposicionComponent)
      },
      {
        path: 'coordinador/oposicion/:idConvocatoria',
        loadComponent: () => import('./features/coordinador/gestion-oposicion-component/gestion-oposicion-component').then(m => m.GestionOposicionComponent)
      },
      {
        path: 'coordinador/seguimiento',
        loadComponent: () => import('./features/coordinador/seguimiento/seguimiento.component').then(m => m.SeguimientoComponent)
      },
      {
        path: 'coordinador/resoluciones',
        loadComponent: () => import('./features/coordinador/resoluciones/resoluciones.component').then(m => m.ResolucionesComponent)
      },
      {
        path: 'coordinador/evaluaciones',
        loadComponent: () => import('./features/coordinador/evaluaciones/evaluaciones.component').then(m => m.EvaluacionesComponent)
      },
      {
        path: 'coordinador/reportes',
        loadComponent: () => import('./features/coordinador/reportes/reportes').then(m => m.ReportesComponent)
      },
      {
        path: 'coordinador/notifications',
        loadComponent: () => import('./features/postulante/notificaciones/notificaciones.component').then(m => m.NotificacionesComponent)
      },

      // ── Ayudante ──────────────────────────────────────────────────
      {
        path: 'ayudante/dashboard',
        loadComponent: () => import('./features/ayudante/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'ayudante/actividades',
        loadComponent: () => import('./features/ayudante/actividades/actividades.component').then(m => m.ActividadesComponent)
      },
      // NUEVO: Componente Padrón de Estudiantes
      {
        path: 'ayudante/padron',
        loadComponent: () => import('../app/features/ayudante/padron-estudiantes-component/padron-estudiantes-component').then(m => m.PadronEstudiantesComponent)
      },
      // NUEVO: Flujo Master-Detail (Listado -> Detalle)
      {
        path: 'ayudante/sesiones',
        loadComponent: () => import('../../src/app/features/ayudante/listado-sesiones-component/listado-sesiones-component').then(m => m.ListadoSesionesComponent)
      },
      {
        path: 'ayudante/sesiones/detalle/:id',
        loadComponent: () => import('../../src/app/features/ayudante/detalle-sesion-component/detalle-sesion-component').then(m => m.DetalleSesionComponent)
      },
      // Mantengo la Matriz por si se usa como reporte histórico general
      {
        path: 'ayudante/asistencia/matriz',
        loadComponent: () => import('./features/ayudante/matriz-asistencia-component/matriz-asistencia-component').then(m => m.MatrizAsistenciaComponent)
      },

      // ── Docente ──────────────────────────────────────────────────────
      {
        path: 'docente/dashboard',
        loadComponent: () => import('./features/docente/dashboard/docente-dashboard.component').then(m => m.DocenteDashboardComponent)
      },
      {
        path: 'docente/mis-ayudantes',
        loadComponent: () => import('./features/docente/mis-ayudantes/mis-ayudantes.component').then(m => m.MisAyudantesComponent)
      },
      {
        path: 'docente/aprobar-informes',
        loadComponent: () => import('./features/docente/mis-ayudantes/mis-ayudantes.component').then(m => m.MisAyudantesComponent)
      },
      {
        path: 'docente/mis-ayudantes/:idAyudantia/actividades',
        loadComponent: () => import('./features/docente/actividades-ayudante/actividades-ayudante').then(m => m.ActividadesAyudanteComponent)
      },

      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },

  { path: '**', redirectTo: 'login' },
];
