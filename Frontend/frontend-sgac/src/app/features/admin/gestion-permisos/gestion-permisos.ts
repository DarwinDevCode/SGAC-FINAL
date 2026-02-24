import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { TipoRolService } from '../../../core/services/tipo-rol-service';
import { PermisoService } from '../../../core/services/permiso-service';
import { RolResumenDTO } from '../../../core/dto/rol-resumen-dto';
import { PermisoRolDTO } from '../../../core/dto/permiso-rol-dto';

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

  mostrarModalPermisos = false;
  rolSeleccionado: RolResumenDTO | null = null;

  permisosList: PermisoRolDTO[] = [];
  permisosListFiltrados: PermisoRolDTO[] = [];
  loadingPermisos = false;
  terminoBusqueda: string = '';

  filtros = {
    esquema: 'todo',
    categoria: 'todo',
    privilegio: 'todo'
  };

  ngOnInit(): void {
    this.cargarRoles();
  }

  cargarRoles(): void {
    this.loading = true;
    this.tipoRolService.obtenerRolesParaPermisos().subscribe({
      next: (data) => {
        this.rolesList = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar la lista de roles', err);
        this.loading = false;
      }
    });
  }

  abrirConfiguracionPermisos(rol: RolResumenDTO): void {
    this.rolSeleccionado = rol;
    this.mostrarModalPermisos = true;
    this.terminoBusqueda = '';

    this.filtros = { esquema: 'todo', categoria: 'todo', privilegio: 'todo' };

    this.cargarPermisos();
  }

  cargarPermisos(): void {
    if (!this.rolSeleccionado?.nombreRolBd) return;

    this.loadingPermisos = true;

    const request: PermisoRolDTO = {
      rolBd: this.rolSeleccionado.nombreRolBd,
      esquema: this.filtros.esquema,
      categoria: this.filtros.categoria,
      privilegio: this.filtros.privilegio
    };

    this.permisoService.consultarPermisos(request).subscribe({
      next: (data) => {
        this.permisosList = data;
        this.filtrarPermisosRapido();
        this.loadingPermisos = false;
      },
      error: (err) => {
        console.error('Error al cargar permisos', err);
        this.loadingPermisos = false;
      }
    });
  }

  filtrarPermisosRapido(): void {
    if (!this.terminoBusqueda.trim()) {
      this.permisosListFiltrados = [...this.permisosList];
      return;
    }

    const term = this.terminoBusqueda.toLowerCase();
    this.permisosListFiltrados = this.permisosList.filter(p =>
      (p.elemento?.toLowerCase() || '').includes(term) ||
      (p.esquema?.toLowerCase() || '').includes(term) ||
      (p.categoria?.toLowerCase() || '').includes(term) ||
      (p.privilegio?.toLowerCase() || '').includes(term)
    );
  }

  cerrarModalPermisos(): void {
    this.mostrarModalPermisos = false;
    this.rolSeleccionado = null;
    this.permisosList = [];
    this.permisosListFiltrados = [];
    this.terminoBusqueda = '';
  }

  getPrivilegioColor(privilegio: string | undefined): any {
    switch (privilegio?.toUpperCase()) {
      case 'SELECT': return { 'background-color': '#dbeafe', 'color': '#1e40af', 'border': '1px solid #bfdbfe' };
      case 'INSERT': return { 'background-color': '#dcfce7', 'color': '#166534', 'border': '1px solid #bbf7d0' };
      case 'UPDATE': return { 'background-color': '#fef9c3', 'color': '#9a3412', 'border': '1px solid #fef08a' };
      case 'DELETE': return { 'background-color': '#fee2e2', 'color': '#991b1b', 'border': '1px solid #fecaca' };
      case 'EXECUTE': return { 'background-color': '#f3e8ff', 'color': '#6b21a8', 'border': '1px solid #e9d5ff' };
      default: return { 'background-color': '#f1f5f9', 'color': '#475569', 'border': '1px solid #e2e8f0' };
    }
  }
}
