import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TipoRolService } from '../../../core/services/tipo-rol-service';
import { RolResumenDTO } from '../../../core/dto/rol-resumen-dto';
import {PermisoService} from '../../../core/services/permiso-service';
import { TipoObjetoSeguridadDTO } from '../../../core/dto/tipo-objeto-seguridad-dto';
import { PrivilegioDTO } from '../../../core/dto/privilegio-dto';
import { PermisoRolDTO } from '../../../core/dto/permiso-rol-dto';
import { GestionPermisosRequestDTO } from '../../../core/dto/gestion-permisos-request-dto';


@Component({
  selector: 'app-gestion-permisos',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gestion-permisos.html',
  styleUrls: ['./gestion-permisos.css']
})
export class GestionPermisosComponent implements OnInit {

  private tipoRolService = inject(TipoRolService);
  private permisoService = inject(PermisoService);

  rolesList: RolResumenDTO[] = [];
  loading = false;
  procesandoEnvio = false;

  // Feedback y Notificaciones
  notificacion: { mensaje: string; exito: boolean } | null = null;

  // Estado de Modales
  mostrarModalVer = false;
  mostrarModalOtorgar = false;
  rolSeleccionado: RolResumenDTO | null = null;

  // --- Listas Dinámicas (Alimentadas por BD) ---
  esquemasDisponibles: string[] = [];
  tiposObjetoDisponibles: TipoObjetoSeguridadDTO[] = [];
  elementosBdList: string[] = [];
  privilegiosPorTipo: PrivilegioDTO[] = [];

  // --- Modal 1: Consulta ---
  permisosList: PermisoRolDTO[] = [];
  permisosListFiltrados: PermisoRolDTO[] = [];
  loadingPermisos = false;
  terminoBusqueda: string = '';
  filtros = { esquema: 'todo', categoria: 'todo', privilegio: 'todo' };

  // --- Modal 2: Navegación Jerárquica ---
  esquemaSeleccionado: string | null = null;
  categoriaSeleccionada: TipoObjetoSeguridadDTO | null = null;
  loadingElementos = false;
  seleccionPendiente: GestionPermisosRequestDTO[] = [];

  ngOnInit(): void { this.cargarRoles(); }

  cargarRoles(): void {
    this.loading = true;
    this.tipoRolService.obtenerRolesParaPermisos().subscribe({
      next: (data) => { this.rolesList = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  // --- Gestión de Modales ---
  abrirModalVerPermisos(rol: RolResumenDTO): void {
    this.rolSeleccionado = rol;
    this.mostrarModalVer = true;
    this.cargarListasIniciales();
    this.cargarPermisos();
  }

  abrirModalOtorgarPermisos(rol: RolResumenDTO): void {
    this.rolSeleccionado = rol;
    this.mostrarModalOtorgar = true;
    this.esquemaSeleccionado = null;
    this.categoriaSeleccionada = null;
    this.seleccionPendiente = [];
    this.cargarListasIniciales();
  }

  private cargarListasIniciales(): void {
    // Paso 1 y 2: Esquemas y Categorías de la BD
    this.permisoService.listarEsquemas().subscribe(data => this.esquemasDisponibles = data);
    this.permisoService.listarTiposObjeto().subscribe(data => this.tiposObjetoDisponibles = data);
  }

  cerrarModales(): void {
    this.mostrarModalVer = false;
    this.mostrarModalOtorgar = false;
    this.notificacion = null;
    this.seleccionPendiente = [];
  }

  // --- Navegación del Menú ---
  toggleEsquema(esc: string) {
    this.esquemaSeleccionado = (this.esquemaSeleccionado === esc) ? null : esc;
    this.categoriaSeleccionada = null;
    this.elementosBdList = [];
  }

  toggleCategoria(tipo: TipoObjetoSeguridadDTO) {
    if (this.categoriaSeleccionada?.idTipoObjetoSeguridad === tipo.idTipoObjetoSeguridad) {
      this.categoriaSeleccionada = null;
    } else {
      this.categoriaSeleccionada = tipo;
      this.cargarNivelElementosYPrivilegios();
    }
  }

  private cargarNivelElementosYPrivilegios() {
    if (!this.esquemaSeleccionado || !this.categoriaSeleccionada) return;
    this.loadingElementos = true;

    forkJoin({
      elementos: this.permisoService.listarElementos(this.esquemaSeleccionado, this.categoriaSeleccionada.nombreTipoObjeto),
      privilegios: this.permisoService.listarPrivilegios(this.categoriaSeleccionada.idTipoObjetoSeguridad)
    }).subscribe({
      next: (res) => {
        this.elementosBdList = res.elementos;
        this.privilegiosPorTipo = res.privilegios;
        this.loadingElementos = false;
      },
      error: () => this.loadingElementos = false
    });
  }

  gestionarCheckBatch(elemento: string, privilegio: string, event: any) {
    const isChecked = event.target.checked;
    const request: GestionPermisosRequestDTO = {
      rolBd: this.rolSeleccionado!.nombreRolBd!,
      esquema: this.esquemaSeleccionado!,
      elemento: elemento,
      categoria: this.categoriaSeleccionada!.nombreTipoObjeto,
      privilegio: privilegio,
      otorgar: true
    };

    if (isChecked) {
      this.seleccionPendiente.push(request);
    } else {
      this.seleccionPendiente = this.seleccionPendiente.filter(p =>
        !(p.elemento === elemento && p.privilegio === privilegio && p.esquema === this.esquemaSeleccionado)
      );
    }
  }

  isCheckSelected(elemento: string, privilegio: string): boolean {
    return this.seleccionPendiente.some(p =>
      p.elemento === elemento && p.privilegio === privilegio && p.esquema === this.esquemaSeleccionado
    );
  }

  otorgarPermisosMasivo() {
    if (this.seleccionPendiente.length === 0) return;
    if (!confirm(`¿Confirmar el otorgamiento de ${this.seleccionPendiente.length} permisos?`)) return;

    this.procesandoEnvio = true;
    const peticiones = this.seleccionPendiente.map(p =>
      this.permisoService.gestionarPermiso(p).pipe(
        catchError(err => of({ mensaje: `Error en ${p.elemento}: ${err.error?.mensaje}`, exito: false }))
      )
    );

    forkJoin(peticiones).subscribe(resultados => {
      this.procesandoEnvio = false;
      const fallidos = resultados.filter(r => !r.exito);
      if (fallidos.length === 0) {
        this.mostrarFeedback(`Éxito: ${resultados.length} permisos otorgados.`, true);
        this.seleccionPendiente = [];
      } else {
        this.mostrarFeedback(`Completado con ${fallidos.length} errores.`, false);
      }
    });
  }

  confirmarRevocarPermiso(p: PermisoRolDTO): void {
    if (!confirm(`¿Revocar '${p.privilegio}' del objeto '${p.elemento}'?`)) return;

    const request: GestionPermisosRequestDTO = {
      rolBd: this.rolSeleccionado!.nombreRolBd!,
      esquema: p.esquema!,
      elemento: p.elemento!,
      categoria: p.categoria!,
      privilegio: p.privilegio!,
      otorgar: false
    };

    this.permisoService.gestionarPermiso(request).subscribe({
      next: (res) => {
        this.mostrarFeedback(res.mensaje, res.exito);
        if (res.exito) this.cargarPermisos();
      },
      error: (err) => this.mostrarFeedback(err.error?.mensaje || 'Error al revocar', false)
    });
  }

  private mostrarFeedback(mensaje: string, exito: boolean) {
    this.notificacion = { mensaje, exito };
    setTimeout(() => this.notificacion = null, 6000);
  }

  cargarPermisos(): void {
    if (!this.rolSeleccionado?.nombreRolBd) return;
    this.loadingPermisos = true;
    this.permisoService.consultarPermisos({
      rolBd: this.rolSeleccionado.nombreRolBd,
      esquema: this.filtros.esquema === 'todo' ? undefined : this.filtros.esquema,
      categoria: this.filtros.categoria === 'todo' ? undefined : this.filtros.categoria,
      privilegio: this.filtros.privilegio === 'todo' ? undefined : this.filtros.privilegio
    }).subscribe({
      next: (data) => {
        this.permisosList = data;
        this.filtrarPermisosRapido();
        this.loadingPermisos = false;
      },
      error: () => this.loadingPermisos = false
    });
  }

  filtrarPermisosRapido(): void {
    const term = this.terminoBusqueda.toLowerCase();
    this.permisosListFiltrados = this.permisosList.filter(p => (p.elemento?.toLowerCase() || '').includes(term));
  }

  getPrivilegioColor(privilegio: string | undefined): any {
    switch (privilegio?.toUpperCase()) {
      case 'SELECT': return { 'background-color': '#dbeafe', 'color': '#1e40af' };
      case 'INSERT': return { 'background-color': '#dcfce7', 'color': '#166534' };
      case 'UPDATE': return { 'background-color': '#fef9c3', 'color': '#9a3412' };
      case 'DELETE': return { 'background-color': '#fee2e2', 'color': '#991b1b' };
      case 'EXECUTE': return { 'background-color': '#f3e8ff', 'color': '#6b21a8' };
      case 'USAGE': return { 'background-color': '#e0f2fe', 'color': '#0369a1' };
      default: return { 'background-color': '#f1f5f9', 'color': '#475569' };
    }
  }
}
