import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { FormsModule } from '@angular/forms';
import { ChatInternoComponent as ChatInternoDecanoComponent } from './chat-interno/chat-interno.component';
import { AuthService } from '../../../core/services/auth-service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { finalize } from 'rxjs';

interface AyudantiaItem {
  idAyudantia: number;
  nombreEstudiante: string;
  asignatura: string;
  estado: string;
  horasCumplidas: number;
}

@Component({
  selector: 'app-comunicacion-decano',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule, ChatInternoDecanoComponent],
  templateUrl: './comunicacion.html',
  styleUrls: ['./comunicacion.css']
})
export class ComunicacionDecanoComponent implements OnInit {
  private auth = inject(AuthService);
  private http = inject(HttpClient);
  private apiUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

  ayudantias: AyudantiaItem[] = [];
  idAyudantiaSeleccionada: number | null = null;
  isLoading = false;
  searchQuery = '';

  ngOnInit() {
    this.cargarAyudantias();
  }

  cargarAyudantias() {
    this.isLoading = true;
    this.http.get<any[]>(`${this.apiUrl}/ayudantias`)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (data) => { this.ayudantias = data ?? []; },
        error: () => { this.ayudantias = []; }
      });
  }

  get filteredAyudantias(): AyudantiaItem[] {
    if (!this.searchQuery.trim()) return this.ayudantias;
    const q = this.searchQuery.toLowerCase();
    return this.ayudantias.filter(a =>
      a.nombreEstudiante?.toLowerCase().includes(q) ||
      a.asignatura?.toLowerCase().includes(q)
    );
  }

  seleccionar(a: AyudantiaItem) {
    this.idAyudantiaSeleccionada = a.idAyudantia;
  }
}
