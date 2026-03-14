import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { UsuarioService } from '../../../core/services/usuario-service';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { UsuarioDTO, RegistroUsuarioGlobalDTO } from '../../../core/dto/usuario';
import { TipoRolDTO } from '../../../core/dto/tipo-rol';
import { CarreraDTO } from '../../../core/dto/carrera';
import { FacultadDTO } from '../../../core/dto/facultad';

/** Rol con su estado de selección para el formulario */
interface RolCheckbox extends TipoRolDTO {
  seleccionado: boolean;
}

/** Formulario de registro (sin campo contraseña) */
interface FormRegistro {
  nombres:      string;
  apellidos:    string;
  cedula:       string;
  correo:       string;
  username:     string;
  // Campos condicionales
  idCarrera?:   number;
  matricula?:   string;
  semestre?:    number;
  idFacultad?:  number;
  horasAyudante?: number;
}

@Component({
  selector: 'app-gestion-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gestion-usuarios.html',
  styleUrl: './gestion-usuarios.css',
})
export class GestionUsuarios implements OnInit, OnDestroy {

  private usuarioService  = inject(UsuarioService);
  private catalogoService = inject(CatalogosService);
  private subs = new Subscription();

  // ── Datos ─────────────────────────────────────────────────────
  usuariosList:      UsuarioDTO[]    = [];
  usuariosFiltrados: UsuarioDTO[]    = [];
  rolesCheckbox:     RolCheckbox[]   = [];
  listaCarreras:     CarreraDTO[]    = [];
  listaFacultades:   FacultadDTO[]   = [];

  // ── UI ────────────────────────────────────────────────────────
  loading       = true;
  guardando     = false;
  mostrarModal  = false;
  busqueda      = '';

  // ── Formulario ────────────────────────────────────────────────
  form: FormRegistro = this.initForm();

  // ── Helpers de visibilidad ────────────────────────────────────
  get tieneRol()       { return (nombre: string) => this.rolesCheckbox.some(r => r.nombreTipoRol === nombre && r.seleccionado); }
  get necesitaCarrera()  { return this.tieneRol('ESTUDIANTE') || this.tieneRol('COORDINADOR'); }
  get necesitaEstudiante() { return this.tieneRol('ESTUDIANTE'); }
  get necesitaFacultad() { return this.tieneRol('DECANO'); }
  get necesitaHoras()    { return this.tieneRol('AYUDANTE_CATEDRA'); }

  get rolesSeleccionados(): RolCheckbox[] {
    return this.rolesCheckbox.filter(r => r.seleccionado);
  }

  // ── Ciclo de vida ─────────────────────────────────────────────
  ngOnInit(): void {
    this.listarUsuarios();
    this.cargarCatalogos();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // ── Usuarios ──────────────────────────────────────────────────
  listarUsuarios() {
    this.loading = true;
    this.subs.add(
      this.usuarioService.listarUsuarios().subscribe({
        next: (data) => {
          this.usuariosList = data ?? [];
          this.aplicarFiltro(this.busqueda);
          this.loading = false;
        },
        error: () => this.loading = false,
      })
    );
  }

  filtrarUsuarios(texto: string) {
    this.busqueda = texto;
    this.aplicarFiltro(texto);
  }

  private aplicarFiltro(texto: string) {
    const term = texto.toLowerCase().trim();
    this.usuariosFiltrados = !term
      ? [...this.usuariosList]
      : this.usuariosList.filter(u =>
        u.nombres.toLowerCase().includes(term) ||
        u.apellidos.toLowerCase().includes(term) ||
        (u.cedula       ?? '').toLowerCase().includes(term) ||
        (u.nombreUsuario ?? '').toLowerCase().includes(term)
      );
  }

  // ── Catálogos ─────────────────────────────────────────────────
  private cargarCatalogos() {
    // Roles desde la BD
    this.subs.add(
      this.usuarioService.getRolesActivos().subscribe({
        next: (roles) => {
          this.rolesCheckbox = roles.map(r => ({ ...r, seleccionado: false }));
        },
        error: (err: HttpErrorResponse) =>
          alert(err.error?.message ?? 'Error al cargar roles'),
      })
    );

    // Carreras
    this.subs.add(
      this.catalogoService.getCarreras().subscribe({
        next: (data) => this.listaCarreras = data ?? [],
        error: (err: HttpErrorResponse) =>
          alert(err.error?.message ?? 'Error al cargar carreras'),
      })
    );

    // Facultades
    this.subs.add(
      this.catalogoService.getFacultades().subscribe({
        next: (data) => this.listaFacultades = data ?? [],
        error: (err: HttpErrorResponse) =>
          alert(err.error?.message ?? 'Error al cargar facultades'),
      })
    );
  }

  // ── Modal ─────────────────────────────────────────────────────
  abrirModalCrear() {
    this.form = this.initForm();
    // Desmarcar todos los checkboxes
    this.rolesCheckbox.forEach(r => r.seleccionado = false);
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
  }

  private initForm(): FormRegistro {
    return {
      nombres: '', apellidos: '', cedula: '', correo: '', username: '',
      idCarrera: undefined, matricula: '', semestre: 1,
      idFacultad: undefined, horasAyudante: 0,
    };
  }

  // ── Guardar ───────────────────────────────────────────────────
  guardarUsuario() {
    // Validar selección de roles
    const rolesIds = this.rolesSeleccionados.map(r => r.idTipoRol!);
    if (rolesIds.length === 0) {
      alert('Seleccione al menos un rol para el usuario.'); return;
    }

    // Validaciones condicionales
    if (this.necesitaCarrera && !this.form.idCarrera) {
      alert('Seleccione la carrera requerida por el rol.'); return;
    }
    if (this.necesitaEstudiante && !this.form.matricula?.trim()) {
      alert('Ingrese la matrícula del estudiante.'); return;
    }
    if (this.necesitaFacultad && !this.form.idFacultad) {
      alert('Seleccione la facultad para el decano.'); return;
    }
    if (this.necesitaHoras && (!this.form.horasAyudante || this.form.horasAyudante <= 0)) {
      alert('Ingrese las horas asignadas al ayudante.'); return;
    }

    const payload: RegistroUsuarioGlobalDTO = {
      nombres:   this.form.nombres.trim(),
      apellidos: this.form.apellidos.trim(),
      cedula:    this.form.cedula.trim(),
      correo:    this.form.correo.trim().toLowerCase(),
      username:  this.form.username.trim().toLowerCase(),
      rolesIds,
      ...(this.necesitaCarrera    && { idCarrera:    this.form.idCarrera }),
      ...(this.necesitaEstudiante && { matricula:    this.form.matricula, semestre: this.form.semestre }),
      ...(this.necesitaFacultad   && { idFacultad:   this.form.idFacultad }),
      ...(this.necesitaHoras      && { horasAyudante: this.form.horasAyudante }),
    };

    this.guardando = true;
    this.subs.add(
      this.usuarioService.registrarGlobal(payload).subscribe({
        next: (res) => {
          alert(res.mensaje ?? 'Usuario registrado. Credenciales enviadas al correo.');
          this.guardando = false;
          this.cerrarModal();
          this.listarUsuarios();
        },
        error: (err: HttpErrorResponse) => {
          this.guardando = false;
          const msg = err.error?.message ?? 'Error de red o servidor no disponible';
          alert(`Error: ${msg}`);
        },
      })
    );
  }

  // ── Cambio de estado ──────────────────────────────────────────
  toggleEstado(u: UsuarioDTO) {
    if (!u.idUsuario) return;
    if (!confirm(`¿Cambiar estado global de "${u.nombreUsuario}"?`)) return;
    this.subs.add(
      this.usuarioService.cambiarEstado(u.idUsuario).subscribe({
        next: () => {
          u.activo = !u.activo;
          u.roles = (u.roles ?? []).map(r => ({ ...r, activo: !!u.activo }));
        },
        error: () => alert('Error al cambiar estado global.'),
      })
    );
  }

  toggleEstadoRol(u: UsuarioDTO, r: TipoRolDTO) {
    if (!u.idUsuario || !u.activo || !r.idTipoRol) return;
    const accion = r.activo ? 'desactivar' : 'activar';
    if (!confirm(`¿Desea ${accion} el permiso de ${r.nombreTipoRol} para "${u.nombreUsuario}"?`)) return;
    this.subs.add(
      this.usuarioService.cambiarEstadoRol(u.idUsuario, r.idTipoRol).subscribe({
        next: () => r.activo = !r.activo,
        error: () => alert('Error al cambiar estado del rol.'),
      })
    );
  }
}
