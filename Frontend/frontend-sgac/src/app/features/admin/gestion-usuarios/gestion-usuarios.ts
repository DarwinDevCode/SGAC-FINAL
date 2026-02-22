import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { UsuarioService } from '../../../core/services/usuario-service';
import { UsuarioDTO } from '../../../core/dto/usuario';
import {CarreraDTO} from '../../../core/dto/carrera';
import {FacultadDTO} from '../../../core/dto/facultad';
import { TipoRolDTO } from '../../../core/dto/tipo-rol';
import { CatalogosService} from '../../../core/services/catalogos-service';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';


@Component({
  selector: 'app-gestion-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gestion-usuarios.html',
  styleUrl: './gestion-usuarios.css',
})
export class GestionUsuarios implements OnInit, OnDestroy {
  catalogoService = inject(CatalogosService);
  usuarioService = inject(UsuarioService);
  private subs = new Subscription(); // Para limpiar las peticiones al destruir el componente

  // Datos
  usuariosList: UsuarioDTO[] = [];
  usuariosFiltrados: UsuarioDTO[] = []; // Usaremos este para el buscador
  listaCarreras: CarreraDTO[] = [];
  listaFacultades: FacultadDTO[] = [];

  // Estados de la UI
  loading = true;
  mostrarModal = false;
  busqueda = '';

  // Formulario
  form: UsuarioDTO = this.initForm();

  ngOnInit(): void {
    this.listarUsuarios();
    this.cargarCatalogos(); // Cargamos facultades y carreras al iniciar
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe(); // Previene memory leaks
  }

  listarUsuarios() {
    this.loading = true;
    this.subs.add(
      this.usuarioService.listarUsuarios().subscribe({
        next: (data) => {
          this.usuariosList = data || [];
          this.aplicarFiltro(this.busqueda); // Mantiene la búsqueda si recargas
          this.loading = false;
        },
        error: () => this.loading = false
      })
    );
  }

  cargarCatalogos() {
    this.subs.add(
      this.catalogoService.getCarreras().subscribe({
        next: (data) => this.listaCarreras = data || [],
        error: (err: HttpErrorResponse) => {
          const msg = err.error?.message || 'Error al cargar carreras';
          alert(msg);
        }
      })
    );

    this.subs.add(
      this.catalogoService.getFacultades().subscribe({
        next: (data) => this.listaFacultades = data || [],
        error: (err: HttpErrorResponse) => {
          const msg = err.error?.message || 'Error al cargar facultades';
          alert(msg);
        }
      })
    );
  }

  filtrarUsuarios(texto: string) {
    this.busqueda = texto;
    this.aplicarFiltro(texto);
  }

  private aplicarFiltro(texto: string) {
    const term = (texto || '').toLowerCase().trim();
    if (!term) {
      this.usuariosFiltrados = [...this.usuariosList];
      return;
    }

    this.usuariosFiltrados = this.usuariosList.filter(u =>
      u.nombres.toLowerCase().includes(term) ||
      u.apellidos.toLowerCase().includes(term) ||
      (u.cedula || '').toLowerCase().includes(term) ||
      (u.nombreUsuario || '').toLowerCase().includes(term)
    );
  }

  abrirModalCrear() {
    this.form = this.initForm();
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
  }

  private initForm(): UsuarioDTO {
    return {
      nombres: '', apellidos: '', cedula: '', correo: '', nombreUsuario: '',
      password: '', rolRegistro: 'ESTUDIANTE',
      idCarrera: undefined, idFacultad: undefined, semestre: 1,
      matricula: '', horasAyudante: 0
    };
  }

  getEsEstudiante(): boolean { return this.form.rolRegistro === 'ESTUDIANTE'; }
  getEsCoordinador(): boolean { return this.form.rolRegistro === 'COORDINADOR'; }
  getEsDecano(): boolean { return this.form.rolRegistro === 'DECANO'; }
  getEsAyudante(): boolean { return this.form.rolRegistro === 'AYUDANTE_CATEDRA'; }


  guardarUsuario() {
    if (this.getEsEstudiante() && !this.form.idCarrera) {
      alert('Seleccione una carrera.'); return;
    }
    if (this.getEsCoordinador() && !this.form.idCarrera) {
      alert('Seleccione la carrera a coordinar.'); return;
    }
    if (this.getEsDecano() && !this.form.idFacultad) {
      alert('Seleccione una facultad.'); return;
    }

    this.subs.add(
      this.usuarioService.crear(this.form).subscribe({
        next: () => {
          alert('Usuario registrado correctamente');
          this.cerrarModal();
          this.listarUsuarios(); // Recargar tabla
        },
        error: (err: HttpErrorResponse) => {
          console.error('Error del backend:', err);

          if (err.error?.errors) {
            const mensajesValidacion = Object.values(err.error.errors).join('\n - ');
            alert(`Por favor, corrige lo siguiente:\n - ${mensajesValidacion}`);
          }
          else if (err.error?.message)
            alert(`Atención: ${err.error.message}`);
          else
            alert("Error de red o servidor no disponible");
        }
      })
    );
  }


  toggleEstado(u: UsuarioDTO) {
    if (!u.idUsuario) return;
    if (!confirm(`¿Cambiar estado global de ${u.nombreUsuario}?`)) return;

    this.subs.add(
      this.usuarioService.cambiarEstado(u.idUsuario).subscribe({
        next: () => {
          u.activo = !u.activo;
          u.roles = (u.roles || []).map(r => ({ ...r, activo: !!u.activo }));
        },
        error: () => alert('Error al cambiar estado global.')
      })
    );
  }

  toggleEstadoRol(u: UsuarioDTO, r: TipoRolDTO) {
    if (!u.idUsuario || !u.activo || !r.idTipoRol) return;

    const accion = r.activo ? 'desactivar' : 'activar';
    if (!confirm(`¿Desea ${accion} el permiso de ${r.nombreTipoRol} para ${u.nombreUsuario}?`)) return;

    this.subs.add(
      this.usuarioService.cambiarEstadoRol(u.idUsuario, r.idTipoRol).subscribe({
        next: () => {
          r.activo = !r.activo;
        },
        error: () => alert('Error al cambiar el estado del rol.')
      })
    );
  }
}
