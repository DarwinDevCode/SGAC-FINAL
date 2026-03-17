import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { ChatInternoComponent } from './chat-interno/chat-interno.component';
import { SesionService } from '../../../core/services/sesion-service';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-comunicacion-ayudante',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, ChatInternoComponent],
  templateUrl: './comunicacion.html',
  styleUrls: ['./comunicacion.css']
})
export class ComunicacionAyudanteComponent implements OnInit {
  private sesionService = inject(SesionService);
  private authService = inject(AuthService);

  ayudantias: any[] = [];
  idAyudantiaSeleccionada: number | null = null;
  isLoadingAyu = false;

  ngOnInit() {
    this.cargarMisAyudantias();
  }

  cargarMisAyudantias() {
    this.isLoadingAyu = true;
    const userId = this.authService.getUser()?.idUsuario;
    if (!userId) return;

    // Usamos el idUsuario para buscar su ayudantía activa
    this.sesionService.obtenerIdAyudantiaActiva(userId).subscribe({
      next: (id) => {
        if (id) {
          this.idAyudantiaSeleccionada = id;
          // Mocking list item for UI
          this.ayudantias = [{
            idAyudantia: id,
            asignatura: 'Asignatura en curso',
            coordinador: 'Coordinador responsable',
            estado: 'ACTIVA',
            periodo: '2024-2025'
          }];
        }
        this.isLoadingAyu = false;
      },
      error: () => this.isLoadingAyu = false
    });
  }

  seleccionarAyudantia(id: number) {
    this.idAyudantiaSeleccionada = id;
  }
}
