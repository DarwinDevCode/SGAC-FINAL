import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import {forkJoin, of, Subscription} from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { TipoRolService } from '../../../core/services/tipo-rol-service';
import { RolResumenDTO } from '../../../core/dto/rol-resumen-dto';
import { PermisoService } from '../../../core/services/permiso-service';
import { TipoObjetoSeguridadDTO } from '../../../core/dto/tipo-objeto-seguridad-dto';
import { PrivilegioDTO } from '../../../core/dto/privilegio-dto';
import { PermisoRolDTO } from '../../../core/dto/permiso-rol-dto';
import { GestionPermisosRequestDTO } from '../../../core/dto/gestion-permisos-request-dto';
import { GestionPermisosMasivoRequestDTO } from '../../../core/dto/gestion-permisos-masivo-request-dto';
import {CatalogosService} from '../../../core/services/catalogos-service';


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
  private catalogosService = inject(CatalogosService);
  private subs = new Subscription();


  rolesList: RolResumenDTO[] = [];
  loading = false;
  procesandoEnvio = false;

  notificacion: { mensaje: string; exito: boolean } | null = null;
  resultadoOperacion: any = null;

  mostrarModalVer = false;
  mostrarModalOtorgar = false;
  rolSeleccionado: RolResumenDTO | null = null;

  esquemasDisponibles: string[] = [];
  tiposObjetoDisponibles: TipoObjetoSeguridadDTO[] = [];
  elementosBdList: string[] = [];
  privilegiosPorTipo: PrivilegioDTO[] = [];

  permisosList: PermisoRolDTO[] = [];
  permisosListFiltrados: PermisoRolDTO[] = [];
  loadingPermisos = false;
  terminoBusqueda: string = '';
  filtros = { esquema: 'todo', categoria: 'todo', privilegio: 'todo' };

  esquemaSeleccionado: string | null = null;
  categoriaSeleccionada: TipoObjetoSeguridadDTO | null = null;
  loadingElementos = false;
  seleccionPendiente: GestionPermisosRequestDTO[] = [];

  ngOnInit(): void {
    this.cargarRoles();

    this.subs.add(
      this.catalogosService.rolActualizado$.subscribe(() => {
        this.cargarRoles();
      })
    );
  }

  cargarRoles(): void {
    this.loading = true;
    this.tipoRolService.obtenerRolesParaPermisos().subscribe({
      next: (data) => {
        this.rolesList = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  abrirModalVerPermisos(rol: RolResumenDTO): void {
    this.rolSeleccionado = rol;
    this.mostrarModalVer = true;
    this.terminoBusqueda = '';
    this.filtros = { esquema: 'todo', categoria: 'todo', privilegio: 'todo' };

    this.cargarCatalogosIniciales();
    this.cargarPermisos();
  }

  abrirModalOtorgarPermisos(rol: RolResumenDTO): void {
    this.rolSeleccionado = rol;
    this.mostrarModalOtorgar = true;
    this.esquemaSeleccionado = null;
    this.categoriaSeleccionada = null;
    this.elementosBdList = [];
    this.seleccionPendiente = [];

    this.cargarCatalogosIniciales();
  }

  private cargarCatalogosIniciales(): void {
    forkJoin({
      esquemas: this.permisoService.listarEsquemas().pipe(catchError(() => of([]))),
      tipos: this.permisoService.listarTiposObjeto().pipe(catchError(() => of([])))
    }).subscribe(res => {
      this.esquemasDisponibles = res.esquemas;
      this.tiposObjetoDisponibles = res.tipos;
    });
  }

  cerrarModales(): void {
    this.mostrarModalVer = false;
    this.mostrarModalOtorgar = false;
    this.rolSeleccionado = null;
    this.notificacion = null;
    this.resultadoOperacion = null;
    this.seleccionPendiente = [];
  }

  toggleEsquema(esc: string) {
    this.esquemaSeleccionado = (this.esquemaSeleccionado === esc) ? null : esc;
    this.categoriaSeleccionada = null;
    this.elementosBdList = [];
    this.privilegiosPorTipo = [];
  }

  toggleCategoria(tipo: TipoObjetoSeguridadDTO) {
    if (this.categoriaSeleccionada?.idTipoObjetoSeguridad === tipo.idTipoObjetoSeguridad) {
      this.categoriaSeleccionada = null;
      this.elementosBdList = [];
      this.privilegiosPorTipo = [];
    } else {
      this.categoriaSeleccionada = tipo;
      this.cargarNivelElementosYPrivilegios();
    }
  }

  private cargarNivelElementosYPrivilegios() {
    if (!this.esquemaSeleccionado || !this.categoriaSeleccionada) return;

    this.loadingElementos = true;
    this.elementosBdList = [];

    forkJoin({
      elementos: this.permisoService.listarElementos(this.esquemaSeleccionado, this.categoriaSeleccionada.nombreTipoObjeto).pipe(catchError(() => of([]))),
      privilegios: this.permisoService.listarPrivilegios(this.categoriaSeleccionada.idTipoObjetoSeguridad).pipe(catchError(() => of([])))
    }).pipe(
      finalize(() => this.loadingElementos = false)
    ).subscribe(res => {
      this.elementosBdList = res.elementos;
      this.privilegiosPorTipo = res.privilegios;
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

    if (!confirm(`¿Desea aplicar estos ${this.seleccionPendiente.length} permisos de forma atómica?`)) return;

    this.procesandoEnvio = true;
    this.resultadoOperacion = null;

    const requestMasivo: GestionPermisosMasivoRequestDTO = {
      permisos: [...this.seleccionPendiente]
    };

    this.permisoService.gestionarPermisosMasivo(requestMasivo).subscribe({
      next: (res) => {
        this.procesandoEnvio = false;
        this.resultadoOperacion = res;
        if (res.exito) {
          this.mostrarFeedback(res.mensaje, true);
          this.seleccionPendiente = [];
        }
      },
      error: (err) => {
        this.procesandoEnvio = false;
        this.resultadoOperacion = err.error;
        this.mostrarFeedback(err.error?.mensaje || 'Error en la transacción.', false);
      }
    });
  }

  cargarPermisos(): void {
    if (!this.rolSeleccionado?.nombreRolBd) return;

    this.loadingPermisos = true;
    const params: PermisoRolDTO = {
      rolBd: this.rolSeleccionado.nombreRolBd,
      esquema: this.filtros.esquema === 'todo' ? undefined : this.filtros.esquema,
      categoria: this.filtros.categoria === 'todo' ? undefined : this.filtros.categoria,
      privilegio: this.filtros.privilegio === 'todo' ? undefined : this.filtros.privilegio
    };

    this.permisoService.consultarPermisos(params).pipe(
      finalize(() => this.loadingPermisos = false)
    ).subscribe({
      next: (data) => {
        this.permisosList = data;
        this.filtrarPermisosRapido();
      },
      error: (err) => {
        console.error(err);
        this.permisosList = [];
        this.permisosListFiltrados = [];
      }
    });
  }

  filtrarPermisosRapido(): void {
    const term = this.terminoBusqueda.toLowerCase().trim();
    if (!term) {
      this.permisosListFiltrados = [...this.permisosList];
    } else {
      this.permisosListFiltrados = this.permisosList.filter(p =>
        (p.elemento?.toLowerCase().includes(term)) ||
        (p.privilegio?.toLowerCase().includes(term))
      );
    }
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

  getPrivilegioColor(privilegio: string | undefined): any {
    const p = privilegio?.toUpperCase();
    if (p === 'SELECT') return { 'background-color': '#dbeafe', 'color': '#1e40af' };
    if (p === 'INSERT') return { 'background-color': '#dcfce7', 'color': '#166534' };
    if (p === 'UPDATE') return { 'background-color': '#fef9c3', 'color': '#9a3412' };
    if (p === 'DELETE') return { 'background-color': '#fee2e2', 'color': '#991b1b' };
    if (p === 'EXECUTE') return { 'background-color': '#f3e8ff', 'color': '#6b21a8' };
    if (p === 'USAGE') return { 'background-color': '#e0f2fe', 'color': '#0369a1' };
    return { 'background-color': '#f1f5f9', 'color': '#475569' };
  }
}
