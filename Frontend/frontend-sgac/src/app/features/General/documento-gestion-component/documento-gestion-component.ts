import {
  Component, Input, OnInit, OnDestroy,
  inject, signal, computed
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { forkJoin } from 'rxjs';

import { DocumentoAcademicoService } from '../../../core/services/documentos/documento-academico-service';
import { AuthService } from '../../../core/services/auth-service';
import {
  ConvocatoriaActivaResponse,
  DocumentoResponse,
  TipoDocumentoResponse,
} from '../../../core/models/documentos/documento-academico';

@Component({
  selector:    'app-documento-gestion',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './documento-gestion-component.html',
  styleUrls:   ['./documento-gestion-component.css'],
})
export class DocumentoGestionComponent implements OnInit, OnDestroy {
  @Input() idConvocatoriaInicial?: number | null;

  private svc   = inject(DocumentoAcademicoService);
  private auth  = inject(AuthService);
  private subs  = new Subscription();

  readonly esCoordinador = computed(() =>
    this.auth.getUser()?.rolActual?.toUpperCase() === 'COORDINADOR'
  );

  documentos     = signal<DocumentoResponse[]>([]);
  tipos          = signal<TipoDocumentoResponse[]>([]);
  convocatorias  = signal<ConvocatoriaActivaResponse[]>([]);
  loading        = signal(true);
  guardando      = signal(false);
  isDragging     = signal(false);
  idConvFiltro = signal<number | null>(null);
  mostrarModal = signal(false);
  modoEdicion  = signal(false);
  editandoId   = signal<number | null>(null);
  formNombre      = '';
  formIdTipo:   number | null = null;
  formIdConv:   number | null = null;
  archivoSel:   File | null   = null;

  // ── Búsqueda ───────────────────────────────────────────────────
  busqueda = '';

  // ── Confirm eliminar ───────────────────────────────────────────
  confirmId = signal<number | null>(null);

  // ── Toast ──────────────────────────────────────────────────────
  toast = signal<{ msg: string; tipo: 'ok' | 'err' } | null>(null);
  private toastTimer?: ReturnType<typeof setTimeout>;

  // ── Computed ───────────────────────────────────────────────────
  get filtrados(): DocumentoResponse[] {
    const b = this.busqueda.toLowerCase().trim();
    return b
      ? this.documentos().filter(
        d => d.nombreMostrar.toLowerCase().includes(b) ||
          d.tipoNombre.toLowerCase().includes(b)
      )
      : this.documentos();
  }

  // ── Ciclo de vida ──────────────────────────────────────────────
  ngOnInit(): void {
    this.idConvFiltro.set(this.idConvocatoriaInicial ?? null);

    // Carga paralela de catálogos + documentos + convocatorias (si coord.)
    const base$ = forkJoin({
      tipos:     this.svc.listarTipos(),
      documentos: this.svc.listarVisor(this.idConvFiltro()),
    });

    if (this.esCoordinador()) {
      // Coordinador: carga las tres colecciones en paralelo
      this.subs.add(
        forkJoin({
          tipos:        this.svc.listarTipos(),
          documentos:   this.svc.listarVisor(this.idConvFiltro()),
          convocatorias: this.svc.listarConvocatoriasActivas(),
        }).subscribe({
          next: ({ tipos, documentos, convocatorias }) => {
            this.tipos.set(tipos);
            this.documentos.set(documentos);
            this.convocatorias.set(convocatorias);
            this.loading.set(false);
          },
          error: () => {
            this.loading.set(false);
            this.showToast('Error al cargar los datos iniciales.', 'err');
          },
        })
      );
    } else {
      // Administrador: no necesita lista de convocatorias
      this.subs.add(
        base$.subscribe({
          next: ({ tipos, documentos }) => {
            this.tipos.set(tipos);
            this.documentos.set(documentos);
            this.loading.set(false);
          },
          error: () => {
            this.loading.set(false);
            this.showToast('Error al cargar los datos iniciales.', 'err');
          },
        })
      );
    }
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    clearTimeout(this.toastTimer);
  }

  // ── Cambiar filtro de convocatoria (visor) ─────────────────────
  onFiltroConvChange(idConv: number | null): void {
    this.idConvFiltro.set(idConv);
    this.recargarDocumentos();
  }

  private recargarDocumentos(): void {
    this.loading.set(true);
    this.subs.add(
      this.svc.listarVisor(this.idConvFiltro()).subscribe({
        next:  docs => { this.documentos.set(docs); this.loading.set(false); },
        error: ()   => { this.loading.set(false); this.showToast('Error al cargar documentos.', 'err'); },
      })
    );
  }

  // ── Modal ──────────────────────────────────────────────────────
  abrirModalNuevo(): void {
    this.modoEdicion.set(false);
    this.editandoId.set(null);
    this.formNombre = '';
    this.formIdTipo = null;
    // Administrador → siempre global (null). Coordinador → elige convocatoria.
    this.formIdConv = this.esCoordinador() ? (this.idConvFiltro() ?? null) : null;
    this.archivoSel = null;
    this.mostrarModal.set(true);
  }

  abrirModalEditar(doc: DocumentoResponse): void {
    this.modoEdicion.set(true);
    this.editandoId.set(doc.id);
    this.formNombre = doc.nombreMostrar;
    this.formIdTipo = doc.idTipoDocumento;
    this.formIdConv = doc.idConvocatoria ?? null;
    this.archivoSel = null;
    this.mostrarModal.set(true);
  }

  cerrarModal(): void {
    this.mostrarModal.set(false);
    this.archivoSel = null;
  }

  // ── Dropzone ───────────────────────────────────────────────────
  onDragOver(e: DragEvent): void  { e.preventDefault(); this.isDragging.set(true); }
  onDragLeave(): void             { this.isDragging.set(false); }
  onDrop(e: DragEvent): void {
    e.preventDefault();
    this.isDragging.set(false);
    const file = e.dataTransfer?.files?.[0];
    if (file) this.archivoSel = file;
  }
  onFileChange(e: Event): void {
    const input = e.target as HTMLInputElement;
    if (input.files?.[0]) this.archivoSel = input.files[0];
  }

  // ── Guardar ────────────────────────────────────────────────────
  guardar(): void {
    if (!this.formNombre.trim() || !this.formIdTipo) {
      this.showToast('Completa nombre y tipo de documento.', 'err');
      return;
    }
    if (!this.modoEdicion() && !this.archivoSel) {
      this.showToast('Selecciona un archivo para subir.', 'err');
      return;
    }

    this.guardando.set(true);

    const accion$ = this.modoEdicion()
      ? this.svc.actualizar(
        this.editandoId()!, this.formNombre, this.formIdTipo, this.archivoSel
      )
      : this.svc.subir(
        this.archivoSel!,
        this.formNombre,
        this.formIdTipo,
        // Administrador → null (global). Coordinador → convocatoria elegida.
        this.esCoordinador() ? this.formIdConv : null
      );

    this.subs.add(
      accion$.subscribe({
        next: doc => {
          this.guardando.set(false);
          this.cerrarModal();
          if (this.modoEdicion()) {
            this.documentos.update(list => list.map(d => d.id === doc.id ? doc : d));
          } else {
            this.documentos.update(list => [doc, ...list]);
          }
          this.showToast(
            this.modoEdicion() ? 'Documento actualizado.' : 'Documento subido correctamente.',
            'ok'
          );
        },
        error: err => {
          this.guardando.set(false);
          this.showToast(err.error?.message ?? 'Error al guardar el documento.', 'err');
        },
      })
    );
  }

  // ── Eliminar ───────────────────────────────────────────────────
  solicitarEliminar(id: number): void  { this.confirmId.set(id); }
  cancelarEliminar(): void             { this.confirmId.set(null); }

  confirmarEliminar(): void {
    const id = this.confirmId();
    if (!id) return;
    this.subs.add(
      this.svc.eliminar(id).subscribe({
        next: () => {
          this.documentos.update(list => list.filter(d => d.id !== id));
          this.confirmId.set(null);
          this.showToast('Documento desactivado.', 'ok');
        },
        error: err => {
          this.confirmId.set(null);
          this.showToast(err.error?.message ?? 'Error al eliminar.', 'err');
        },
      })
    );
  }

  // ── Helpers UI ─────────────────────────────────────────────────
  formatBytes(bytes: number | null): string {
    if (!bytes) return '—';
    if (bytes < 1024)        return `${bytes} B`;
    if (bytes < 1048576)     return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }

  extBadgeClass(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (e === 'pdf')                       return 'ext-pdf';
    if (['doc', 'docx'].includes(e))       return 'ext-doc';
    if (['xls', 'xlsx'].includes(e))       return 'ext-xls';
    if (['jpg', 'jpeg', 'png'].includes(e)) return 'ext-img';
    return 'ext-other';
  }

  private showToast(msg: string, tipo: 'ok' | 'err'): void {
    clearTimeout(this.toastTimer);
    this.toast.set({ msg, tipo });
    this.toastTimer = setTimeout(
      () => this.toast.set(null),
      tipo === 'err' ? 8000 : 4000
    );
  }
}
