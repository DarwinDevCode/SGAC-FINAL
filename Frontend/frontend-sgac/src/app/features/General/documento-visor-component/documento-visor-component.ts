import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  inject,
  signal,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';

import { AuthService } from '../../../core/services/auth-service';
import { DocumentoService } from '../../../core/services/documentos/documento-academico-service';
import { DocumentoVisor } from '../../../core/models/documentos/documento-academico';

@Component({
  selector: 'app-documento-visor',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: 'documento-visor-component.html',
  styleUrl: 'documento-visor-component.css'
})
export class DocumentoVisorComponent implements OnInit, OnDestroy {

  private readonly docService = inject(DocumentoService);
  private readonly authService = inject(AuthService);
  private readonly subs = new Subscription();

  @Input() titulo = 'Documentos Académicos';

  documentos = signal<DocumentoVisor[]>([]);
  loading = signal(true);
  busqueda = signal('');

  documentosFiltrados = computed(() => {
    const docs = this.documentos();
    const filtro = this.busqueda().toLowerCase().trim();
    return filtro
      ? docs.filter(d =>
        d.nombreMostrar.toLowerCase().includes(filtro) ||
        d.tipoDocumento.toLowerCase().includes(filtro)
      )
      : docs;
  });

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user?.idUsuario && user?.rolActual) {
      this.loading.set(true);
      this.subs.add(
        this.docService.listarDocumentos(user.idUsuario, user.rolActual).subscribe({
          next: docs => {
            this.documentos.set(docs);
            this.loading.set(false);
          },
          error: () => this.loading.set(false),
        })
      );
    } else {
      this.loading.set(false);
    }
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // --- MÉTODOS DE APOYO VISUAL ---

  obtenerEtiquetaExtension(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (['doc', 'docx'].includes(e)) return 'WORD';
    if (['xls', 'xlsx'].includes(e)) return 'EXCEL';
    if (['png', 'jpg', 'jpeg'].includes(e)) return 'IMG';
    return e ? e.toUpperCase() : 'FILE';
  }

  obtenerIconoLucide(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (e === 'pdf') return 'file-text';
    if (['doc', 'docx'].includes(e)) return 'file-word';
    if (['xls', 'xlsx'].includes(e)) return 'sheet';
    if (['jpg', 'jpeg', 'png'].includes(e)) return 'image';
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

  obtenerClaseNivel(doc: DocumentoVisor): string {
    if (!doc.nombreFacultad) return 'global';
    if (!doc.nombreCarrera) return 'facultad';
    return 'carrera';
  }

  obtenerEtiquetaNivel(doc: DocumentoVisor): string {
    if (!doc.nombreFacultad) return 'Global';
    if (!doc.nombreCarrera) return 'Facultad';
    return 'Carrera';
  }

  formatearBytes(bytes: number | null): string {
    if (!bytes) return '—';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }
}
