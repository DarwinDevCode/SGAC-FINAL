import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
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
  getEsCoordinador(): boolean { return this.form.roles[0] === 'COORDINADOR'; }
  getEsDecano(): boolean { return this.form.roles[0] === 'DECANO'; }
  getEsAyudante(): boolean { return this.form.roles[0] === 'AYUDANTE_CATEDRA'; }

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
          this.aplicarFiltro(this.busqueda());
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
    this.aplicarFiltro(texto);
  }

  private aplicarFiltro(texto: string) {
    const term = (texto || '').toLowerCase().trim();
    if (!term) {
      this.usuariosFiltrados = [...this.todosLosUsuarios];
      return;
    }

    this.usuariosFiltrados = this.todosLosUsuarios.filter(u =>
      u.nombres.toLowerCase().includes(term) ||
      u.apellidos.toLowerCase().includes(term) ||
      (u.cedula || '').toLowerCase().includes(term)
    );
  }

  private initForm(): UsuarioRequest {
    return {
      nombres: '',
      apellidos: '',
      cedula: '',
      correo: '',
      nombreUsuario: '',
      password: '',
      roles: ['ESTUDIANTE'],
      idCarrera: undefined,
      idFacultad: undefined,
      semestre: 1,
      matricula: '',
      horasAyudante: 0
    };
  }

  abrirModalCrear() {
    this.form = this.initForm();
    this.mostrarModal.set(true);
  }

  guardarUsuario() {
    if (this.getEsEstudiante() && !this.form.idCarrera) {
      alert('Seleccione una carrera');
      return;
    }

    if (this.getEsCoordinador() && !this.form.idCarrera) {
      alert('Seleccione la carrera a coordinar');
      return;
    }

    if (this.getEsDecano() && !this.form.idFacultad) {
      alert('Seleccione una facultad');
      return;
    }

    this.subs.add(
      this.usuarioService.crear(this.form).subscribe({
        next: () => {
          alert('Usuario registrado correctamente');
          this.mostrarModal.set(false);
          this.cargarDatos();
        },
        error: (error: any) => {
          const msg = error?.error?.mensaje || '';
          if (msg.toLowerCase().includes('duplicate')) {
            alert('Error: Ya existe un usuario con esa cédula, correo o matrícula.');
            return;
          }
          alert('Error al registrar: verifique los datos.');
        }
      })
    );
  }

  toggleEstado(u: Usuario) {
    if (!confirm(`¿Cambiar estado global de ${u.nombreUsuario}?`)) return;
    this.subs.add(this.usuarioService.cambiarEstado(u.idUsuario).subscribe(() => u.activo = !u.activo));
  }

  toggleEstadoRol(u: Usuario, r: TipoRol) {
    if (!u.activo) return;

    const accion = r.activo ? 'desactivar' : 'activar';
    if (!confirm(`¿Desea ${accion} el permiso de ${r.nombreTipoRol} para ${u.nombreUsuario}?`)) return;

    this.subs.add(this.usuarioService.cambiarEstadoRol(u.idUsuario, r.idTipoRol).subscribe(() => r.activo = !r.activo));
  }
}
