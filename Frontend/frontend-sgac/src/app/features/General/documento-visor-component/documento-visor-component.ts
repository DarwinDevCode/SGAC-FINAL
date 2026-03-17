import {
  Component, Input, OnInit, OnDestroy,
  inject, signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';

import { DocumentoAcademicoService } from '../../../core/services/documentos/documento-academico-service';
import { DocumentoResponse } from '../../../core/models/documentos/documento-academico';

@Component({
  selector:    'app-documento-visor',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './documento-visor-component.html',
  styleUrls:   ['./documento-visor-component.css'],
})
export class DocumentoVisorComponent implements OnInit, OnDestroy {

  // idPeriodo eliminado — el periodo se resuelve en PG
  @Input() idConvocatoria?: number | null;
  @Input() titulo = 'Centro de Documentos';

  private svc  = inject(DocumentoAcademicoService);
  private subs = new Subscription();

  documentos = signal<DocumentoResponse[]>([]);
  loading    = signal(true);
  busqueda   = '';

  // ── Computed getters ───────────────────────────────────────────
  get filtrados(): DocumentoResponse[] {
    const b = this.busqueda.toLowerCase().trim();
    return b
      ? this.documentos().filter(
        d => d.nombreMostrar.toLowerCase().includes(b) ||
          d.tipoNombre.toLowerCase().includes(b)
      )
      : this.documentos();
  }

  get globales(): DocumentoResponse[] {
    return this.filtrados.filter(d => d.esGlobal);
  }

  get porConvocatoria(): DocumentoResponse[] {
    return this.filtrados.filter(d => !d.esGlobal);
  }

  // ── Ciclo de vida ──────────────────────────────────────────────
  ngOnInit(): void {
    // Sin idPeriodo — el backend filtra por el periodo activo automáticamente
    this.subs.add(
      this.svc.listarVisor(this.idConvocatoria).subscribe({
        next:  docs => { this.documentos.set(docs); this.loading.set(false); },
        error: ()   => this.loading.set(false),
      })
    );
  }

  ngOnDestroy(): void { this.subs.unsubscribe(); }

  abrirDocumento(url: string): void {
    window.open(url, '_blank', 'noopener,noreferrer');
  }

  formatBytes(bytes: number | null): string {
    if (!bytes) return '—';
    if (bytes < 1024)    return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }

  extIcon(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (e === 'pdf')                       return 'file-text';
    if (['doc', 'docx'].includes(e))       return 'file-type';
    if (['xls', 'xlsx'].includes(e))       return 'table-2';
    if (['jpg', 'jpeg', 'png'].includes(e)) return 'image';
    return 'file';
  }

  extColor(ext: string | null): string {
    const e = (ext ?? '').toLowerCase();
    if (e === 'pdf')                       return '#dc2626';
    if (['doc', 'docx'].includes(e))       return '#2563eb';
    if (['xls', 'xlsx'].includes(e))       return '#16a34a';
    if (['jpg', 'jpeg', 'png'].includes(e)) return '#7c3aed';
    return '#64748b';
  }
}
