import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  inject,
  signal,
  computed,
  effect,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription, forkJoin } from 'rxjs';

import { AuthService } from '../../../core/services/auth-service';
import { DocumentoService } from '../../../core/services/documentos/documento-academico-service';
import {
  Facultad,
  Carrera,
  TipoDocumento,
  DocumentoVisor,
  NivelDocumento,
  DocumentoCrearRequest,
  DocumentoActualizarRequest,
} from '../../../core/models/documentos/documento-academico';

interface Toast {
  msg: string;
  tipo: 'ok' | 'err';
}

@Component({
  selector: 'app-documento-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './documento-gestion-component.html',
  styleUrls: ['./documento-gestion-component.css'],
})
export class DocumentoGestionComponent implements OnInit, OnDestroy {
  private readonly docService = inject(DocumentoService);
  private readonly authService = inject(AuthService);
  private readonly subs = new Subscription();

  protected readonly NivelDocumento = NivelDocumento;

  @Input() soloLectura = false;

  // Signals de Estado
  facultades = signal<Facultad[]>([]);
  carreras = signal<Carrera[]>([]);
  tiposDocumento = signal<TipoDocumento[]>([]);
  documentos = signal<DocumentoVisor[]>([]);

  // Estado UI
  loading = signal(true);
  guardando = signal(false);
  cargandoCarreras = signal(false);
  modalAbierto = signal(false);
  confirmacion = signal<number | null>(null);
  toast = signal<Toast | null>(null);
  filtroNombre = signal('');

  // Formulario
  formNombre = signal('');
  formIdTipo = signal<number | null>(null);
  formNivel = signal<NivelDocumento>(NivelDocumento.GLOBAL);
  formIdFacultad = signal<number | null>(null);
  formIdCarrera = signal<number | null>(null);
  archivoSeleccionado = signal<File | null>(null);
  arrastrandoArchivo = signal(false);
  modoEdicion = signal(false);
  documentoEditando = signal<number | null>(null);

  // Computed
  esCoordinador = computed(() =>
    this.authService.getUser()?.rolActual?.toUpperCase() === 'COORDINADOR'
  );

  documentosFiltrados = computed(() => {
    const docs = this.documentos();
    const filtro = this.filtroNombre().toLowerCase().trim();
    return filtro
      ? docs.filter(d =>
        d.nombreMostrar.toLowerCase().includes(filtro) ||
        d.tipoDocumento.toLowerCase().includes(filtro))
      : docs;
  });

  mostrarSelectCarreras = computed(() =>
    !this.esCoordinador() &&
    this.formNivel() === NivelDocumento.CARRERA &&
    this.formIdFacultad() !== null
  );

  constructor() {
    effect(() => {
      const nivel = this.formNivel();
      if (!this.esCoordinador()) {
        if (nivel === NivelDocumento.GLOBAL) {
          this.formIdFacultad.set(null);
          this.formIdCarrera.set(null);
        } else if (nivel === NivelDocumento.FACULTAD) {
          this.formIdCarrera.set(null);
        }
      }
    }, { allowSignalWrites: true });

    effect(() => {
      const idFac = this.formIdFacultad();
      if (!this.esCoordinador() && this.formNivel() === NivelDocumento.CARRERA && idFac) {
        this.cargarCarreras(idFac);
      }
    }, { allowSignalWrites: true });
  }

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  private cargarDatosIniciales(): void {
    const user = this.authService.getUser();
    if (!user?.idUsuario) return;

    this.loading.set(true);
    this.subs.add(
      forkJoin({
        facultades: this.docService.obtenerFacultades(),
        tipos: this.docService.obtenerTiposDocumento(),
        documentos: this.docService.listarDocumentos(user.idUsuario, user.rolActual || 'ESTUDIANTE')
      }).subscribe({
        next: (res) => {
          this.facultades.set(res.facultades);
          this.tiposDocumento.set(res.tipos);
          this.documentos.set(res.documentos);
          this.loading.set(false);
        },
        error: (err) => {
          this.mostrarToast(err.message, 'err');
          this.loading.set(false);
        }
      })
    );
  }

  private cargarCarreras(idFacultad: number): void {
    this.cargandoCarreras.set(true);
    this.subs.add(
      this.docService.obtenerCarreras(idFacultad).subscribe({
        next: (res) => {
          this.carreras.set(res);
          this.cargandoCarreras.set(false);
        },
        error: () => this.cargandoCarreras.set(false)
      })
    );
  }

  abrirModalCrear(): void {
    this.modoEdicion.set(false);
    this.resetearFormulario();
    if (this.esCoordinador()) this.formNivel.set(NivelDocumento.CARRERA);
    this.modalAbierto.set(true);
  }

  abrirModalEditar(doc: DocumentoVisor): void {
    this.modoEdicion.set(true);
    this.documentoEditando.set(doc.idDocumento);
    this.formNombre.set(doc.nombreMostrar);
    const tipo = this.tiposDocumento().find(t => t.nombre === doc.tipoDocumento);
    this.formIdTipo.set(tipo?.id || null);

    if (doc.nombreCarrera) this.formNivel.set(NivelDocumento.CARRERA);
    else if (doc.nombreFacultad) this.formNivel.set(NivelDocumento.FACULTAD);
    else this.formNivel.set(NivelDocumento.GLOBAL);

    this.modalAbierto.set(true);
  }

  guardarDocumento(): void {
    if (!this.validarFormulario()) return;
    this.guardando.set(true);
    const user = this.authService.getUser();

    // ESTRATEGIA FLAGS (1/0) PARA COORDINADOR
    const facVal = this.esCoordinador()
      ? (this.formNivel() === NivelDocumento.FACULTAD || this.formNivel() === NivelDocumento.CARRERA ? 1 : 0)
      : this.formIdFacultad();

    const carVal = this.esCoordinador()
      ? (this.formNivel() === NivelDocumento.CARRERA ? 1 : 0)
      : this.formIdCarrera();

    if (this.modoEdicion()) {
      const req: DocumentoActualizarRequest = {
        idDocumento: this.documentoEditando()!,
        nombreMostrar: this.formNombre(),
        idTipoDoc: this.formIdTipo()!,
        idFacultad: facVal,
        idCarrera: carVal,
        idUsuario: user!.idUsuario
      };
      this.subs.add(this.docService.actualizarDocumento(req).subscribe({
        next: () => this.finalizarProceso('Documento actualizado'),
        error: (err) => { this.guardando.set(false); this.mostrarToast(err.message, 'err'); }
      }));
    } else {
      const req: DocumentoCrearRequest = {
        archivo: this.archivoSeleccionado()!,
        nombre: this.formNombre(),
        idTipo: this.formIdTipo()!,
        idUsuario: user!.idUsuario,
        idFacultad: facVal,
        idCarrera: carVal
      };
      this.subs.add(this.docService.crearDocumento(req).subscribe({
        next: () => this.finalizarProceso('Documento subido correctamente'),
        error: (err) => { this.guardando.set(false); this.mostrarToast(err.message, 'err'); }
      }));
    }
  }

  confirmarEliminar(): void {
    const id = this.confirmacion();
    if (!id) return;
    this.subs.add(this.docService.eliminarDocumento(id).subscribe({
      next: () => {
        this.documentos.update(list => list.filter(d => d.idDocumento !== id));
        this.confirmacion.set(null);
        this.mostrarToast('Documento eliminado', 'ok');
      },
      error: (err) => this.mostrarToast(err.message, 'err')
    }));
  }

  // --- HELPERS VISUALES (REFACTORIZADOS) ---

  obtenerEtiquetaExtension(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (['doc', 'docx'].includes(e)) return 'WORD';
    if (['xls', 'xlsx'].includes(e)) return 'EXCEL';
    if (['png', 'jpg', 'jpeg', 'gif'].includes(e)) return 'IMG';
    if (!ext) return 'FILE';
    return e.toUpperCase();
  }

  obtenerIconoLucide(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (e === 'pdf') return 'file-text';
    if (['doc', 'docx'].includes(e)) return 'file-word';
    if (['xls', 'xlsx'].includes(e)) return 'sheet';
    if (['jpg', 'jpeg', 'png', 'gif'].includes(e)) return 'image';
    return 'file';
  }

  obtenerClaseExtension(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (e === 'pdf') return 'ext-pdf';
    if (['doc', 'docx'].includes(e)) return 'ext-doc';
    if (['xls', 'xlsx'].includes(e)) return 'ext-xls';
    if (['jpg', 'jpeg', 'png'].includes(e)) return 'ext-img';
    return 'ext-other';
  }

  formatearBytes = (b: number | null) => !b ? '—' : b < 1024 ? `${b} B` : b < 1048576 ? `${(b / 1024).toFixed(1)} KB` : `${(b / 1048576).toFixed(1)} MB`;
  obtenerNivelBadge = (d: DocumentoVisor) => !d.nombreFacultad ? 'blue' : !d.nombreCarrera ? 'amber' : 'green';
  obtenerTextoNivel = (d: DocumentoVisor) => !d.nombreFacultad ? 'Global' : !d.nombreCarrera ? 'Facultad' : 'Carrera';
  solicitarEliminar = (id: number) => this.confirmacion.set(id);
  cancelarEliminar = () => this.confirmacion.set(null);
  cerrarModal = () => this.modalAbierto.set(false);
  onDragOver = (e: DragEvent) => { e.preventDefault(); this.arrastrandoArchivo.set(true); };
  onDragLeave = () => this.arrastrandoArchivo.set(false);

  private finalizarProceso(msg: string): void {
    this.guardando.set(false);
    this.modalAbierto.set(false);
    this.mostrarToast(msg, 'ok');
    this.cargarDatosIniciales();
  }

  private validarFormulario(): boolean {
    if (!this.formNombre().trim()) { this.mostrarToast('Nombre requerido', 'err'); return false; }
    if (!this.formIdTipo()) { this.mostrarToast('Seleccione un tipo', 'err'); return false; }
    if (!this.modoEdicion() && !this.archivoSeleccionado()) { this.mostrarToast('Archivo requerido', 'err'); return false; }
    return true;
  }

  private resetearFormulario(): void {
    this.formNombre.set('');
    this.formIdTipo.set(null);
    this.formNivel.set(NivelDocumento.GLOBAL);
    this.formIdFacultad.set(null);
    this.formIdCarrera.set(null);
    this.archivoSeleccionado.set(null);
  }

  onFileChange = (e: Event) => {
    const input = e.target as HTMLInputElement;
    if (input.files?.[0]) this.archivoSeleccionado.set(input.files[0]);
  }

  onDrop = (e: DragEvent) => {
    e.preventDefault();
    this.arrastrandoArchivo.set(false);
    if (e.dataTransfer?.files?.[0]) this.archivoSeleccionado.set(e.dataTransfer.files[0]);
  }

  private mostrarToast(msg: string, tipo: 'ok' | 'err'): void {
    this.toast.set({ msg, tipo });
    setTimeout(() => this.toast.set(null), 4000);
  }
}
