import { Component, inject, Input, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { ComunicacionService, MensajeInterno } from '../../../../core/services/comunicacion-service';
import { AuthService } from '../../../../core/services/auth-service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-chat-interno-decano',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './chat-interno.html',
  styleUrls: ['./chat-interno.css']
})
export class ChatInternoComponent implements OnInit, AfterViewChecked {
  @Input() idAyudantia!: number;
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  private chatService = inject(ComunicacionService);
  private authService = inject(AuthService);

  mensajes: MensajeInterno[] = [];
  nuevoMensaje = '';
  userId = this.authService.getUser()?.idUsuario;
  isSending = false;
  isLoading = false;

  ngOnInit() {
    if (this.idAyudantia) this.cargarHistorial();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  cargarHistorial() {
    if (!this.idAyudantia) return;
    this.isLoading = true;
    this.chatService.obtenerHistorial(this.idAyudantia)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: msgs => { this.mensajes = msgs; this.scrollToBottom(); },
        error: err => console.error('Error chat decano:', err)
      });
  }

  enviar() {
    if (!this.nuevoMensaje.trim() || !this.idAyudantia || this.isSending) return;
    this.isSending = true;
    const msg: MensajeInterno = {
      idAyudantia: this.idAyudantia,
      idUsuarioEmisor: this.userId!,
      mensaje: this.nuevoMensaje
    };
    this.chatService.enviarMensaje(msg)
      .pipe(finalize(() => this.isSending = false))
      .subscribe({
        next: (res) => { this.mensajes.push(res); this.nuevoMensaje = ''; this.scrollToBottom(); },
        error: () => {}
      });
  }

  private scrollToBottom(): void {
    try {
      if (this.scrollContainer)
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch {}
  }
}
