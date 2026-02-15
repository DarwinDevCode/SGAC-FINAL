import { Component, inject, signal, computed, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { UsuarioService, Usuario, UsuarioRequest, Carrera, Facultad, TipoRol } from '../../../core/services/usuario';

@Component({
  selector: 'app-gestion-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gestion-usuarios.html',
  styleUrl: './gestion-usuarios.css'
})
export class GestionUsuariosComponent implements OnInit, OnDestroy {
  private usuarioService = inject(UsuarioService);
  private subs = new Subscription();

  public todosLosUsuarios: Usuario[] = [];
  public usuariosFiltrados: Usuario[] = [];
  public listaCarreras: Carrera[] = [];
  public listaFacultades: Facultad[] = [];

  loading = signal(true);
  busqueda = signal('');
  mostrarModal = signal(false);

  form: UsuarioRequest = this.initForm();

  getEsEstudiante(): boolean { return this.form.roles[0] === 'ESTUDIANTE'; }
  getEsDecano(): boolean { return this.form.roles[0] === 'DECANO'; }

  ngOnInit() {
    this.cargarDatos();
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }

  cargarDatos() {
    this.loading.set(true);

    this.subs.add(
      this.usuarioService.listarTodos().subscribe({
        next: (data: Usuario[]) => {
          this.todosLosUsuarios = data || [];
          this.usuariosFiltrados = [...this.todosLosUsuarios];
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      })
    );

    this.subs.add(this.usuarioService.listarCarreras().subscribe(data => this.listaCarreras = data || []));
    this.subs.add(this.usuarioService.listarFacultades().subscribe(data => this.listaFacultades = data || []));
  }

  filtrarUsuarios(texto: string) {
    this.busqueda.set(texto);
    const term = texto.toLowerCase();
    this.usuariosFiltrados = this.todosLosUsuarios.filter(u =>
      u.nombres.toLowerCase().includes(term) ||
      u.apellidos.toLowerCase().includes(term) ||
      u.cedula?.includes(term)
    );
  }

  private initForm(): UsuarioRequest {
    return {
      nombres: '', apellidos: '', cedula: '', correo: '', nombreUsuario: '',
      password: '', roles: ['ESTUDIANTE'], idCarrera: undefined, idFacultad: undefined
    };
  }

  abrirModalCrear() {
    this.form = this.initForm();
    this.mostrarModal.set(true);
  }

  guardarUsuario() {
    this.usuarioService.crear(this.form).subscribe({
      next: () => {
        alert("Usuario registrado");
        this.mostrarModal.set(false);
        this.cargarDatos();
      }
    });
  }

  toggleEstado(u: Usuario) {
    this.usuarioService.cambiarEstado(u.idUsuario).subscribe(() => u.activo = !u.activo);
  }

  toggleEstadoRol(u: Usuario, r: TipoRol) {
    if (!u.activo) return;
    this.usuarioService.cambiarEstadoRol(u.idUsuario, r.idTipoRol).subscribe(() => r.activo = !r.activo);
  }
}
