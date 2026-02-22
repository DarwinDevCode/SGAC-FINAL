import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { TipoRolDTO } from '../../../core/dto/tipo-rol';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, Plus, Edit, Power, X } from 'lucide-angular';

@Component({
  selector: 'app-gestion-catalogos',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  providers: [
    {
      provide: LUCIDE_ICONS,
      multi: true,
      // 2. Envolvemos el objeto en un 'new LucideIconProvider'
      useValue: new LucideIconProvider({ Plus, Edit, Power, X })
    }
  ],
  templateUrl: './gestion-catalogos.html',
  styleUrl: './gestion-catalogos.css',
})

export class GestionCatalogosComponent implements OnInit, OnDestroy {
  private catalogosService = inject(CatalogosService);
  private subs = new Subscription();

  // Pestaña activa
  activeTab: 'roles' | 'facultades' | 'carreras' = 'roles';

  // Datos
  rolesList: TipoRolDTO[] = [];
  loading = false;
  mostrarModal = false;

  // Formulario temporal
  formRol: TipoRolDTO = { nombreTipoRol: '', activo: true };
  isEditMode = false;

  ngOnInit(): void {
    this.cargarDatosActivos();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // Cambiar pestaña
  setTab(tab: 'roles' | 'facultades' | 'carreras') {
    this.activeTab = tab;
    this.cargarDatosActivos();
  }

  cargarDatosActivos() {
    this.loading = true;
    if (this.activeTab === 'roles') {
      this.subs.add(
        this.catalogosService.getTiposRol().subscribe({
          next: (data) => {
            this.rolesList = data || [];
            this.loading = false;
          },
          error: () => this.loading = false
        })
      );
    }
  }

  abrirModalNuevoRol() {
    this.isEditMode = false;
    this.formRol = { nombreTipoRol: '', activo: true };
    this.mostrarModal = true;
  }

  abrirModalEditarRol(rol: TipoRolDTO) {
    this.isEditMode = true;
    this.formRol = { ...rol }; // Clonamos el objeto
    this.mostrarModal = true;
  }

  guardarRol() {
    if (!this.formRol.nombreTipoRol) {
      alert('El nombre es obligatorio');
      return;
    }

    const peticion = this.isEditMode && this.formRol.idTipoRol
      ? this.catalogosService.putTipoRol(this.formRol.idTipoRol, this.formRol)
      : this.catalogosService.postTipoRol(this.formRol);

    this.subs.add(
      peticion.subscribe({
        next: () => {
          alert(`Rol ${this.isEditMode ? 'actualizado' : 'creado'} correctamente`);
          this.mostrarModal = false;
          this.cargarDatosActivos();
        },
        error: () => alert('Error al guardar el rol')
      })
    );
  }

  toggleEstadoRol(rol: TipoRolDTO) {
    if (!rol.idTipoRol) return;
    if (!confirm(`¿Desea cambiar el estado de ${rol.nombreTipoRol}?`)) return;

    this.subs.add(
      this.catalogosService.desactivarTipoRol(rol.idTipoRol).subscribe({
        next: () => {
          rol.activo = !rol.activo; // Actualización optimista
        },
        error: () => alert('Error al cambiar el estado')
      })
    );
  }
}
