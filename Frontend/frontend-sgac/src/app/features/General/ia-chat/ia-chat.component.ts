import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IaAsistenteService, IaRequest } from './ia-asistente.service';
import { LucideAngularModule, Bot, X, Send, Maximize2, Minimize2 } from 'lucide-angular';

@Component({
  selector: 'app-ia-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  template: `
    <!-- Floating button -->
    <button *ngIf="!isOpen()" 
            (click)="toggleChat()"
            class="fixed bottom-6 right-6 bg-indigo-600 text-white p-4 rounded-full shadow-lg hover:bg-indigo-700 transition-all z-50 flex items-center justify-center animate-bounce">
      <lucide-icon name="bot" [size]="28"></lucide-icon>
    </button>

    <!-- Chat Window -->
    <div *ngIf="isOpen()" 
         [ngClass]="{'w-96 h-[500px]': !isExpanded(), 'w-11/12 h-[80vh] md:w-3/4': isExpanded()}"
         class="fixed bottom-6 right-6 bg-white rounded-xl shadow-2xl flex flex-col z-50 border border-gray-200 transition-all duration-300 overflow-hidden">
      
      <!-- Header -->
      <div class="bg-indigo-600 text-white p-4 flex justify-between items-center shrink-0">
        <div class="flex items-center gap-2">
          <lucide-icon name="bot" [size]="24"></lucide-icon>
          <div>
            <h3 class="font-bold text-lg leading-tight">Asistente IA</h3>
            <p class="text-xs text-indigo-200">Reglamentos, redacción y rúbricas</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button (click)="toggleExpand()" class="p-1 hover:bg-indigo-500 rounded transition-colors text-white">
            <lucide-icon [name]="isExpanded() ? 'minimize-2' : 'maximize-2'" [size]="18"></lucide-icon>
          </button>
          <button (click)="toggleChat()" class="p-1 hover:bg-indigo-500 rounded transition-colors text-white">
            <lucide-icon name="x" [size]="20"></lucide-icon>
          </button>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="flex-1 p-4 overflow-y-auto bg-gray-50 flex flex-col gap-4">
        <div class="bg-indigo-50 border border-indigo-100 rounded-lg p-3 text-sm text-indigo-900 shadow-sm">
          👋 ¡Hola! Soy el Asistente Académico. Puedo ayudarte con:
          <ul class="list-disc pl-5 mt-2 space-y-1">
            <li>Consultas sobre el Reglamento de Ayudantías.</li>
            <li>Revisión y corrección de redacción.</li>
            <li>Sugerencias de rúbricas de calificación.</li>
          </ul>
        </div>

        <div *ngFor="let msg of messages()" 
             [ngClass]="{'self-end bg-indigo-600 text-white': msg.isUser, 'self-start bg-white border border-gray-200 text-gray-800': !msg.isUser}"
             class="max-w-[85%] rounded-lg p-3 shadow-sm text-sm whitespace-pre-wrap">
          {{ msg.text }}
        </div>

        <!-- Typing indicator -->
        <div *ngIf="isLoading()" class="self-start bg-white border border-gray-200 text-gray-500 rounded-lg p-3 shadow-sm flex items-center gap-2">
          <lucide-icon name="bot" [size]="16" class="animate-pulse"></lucide-icon>
          <span class="text-sm">Pensando...</span>
        </div>
      </div>

      <!-- Input Area -->
      <div class="p-3 bg-white border-t border-gray-200 shrink-0">
        <div class="mb-2">
          <select [(ngModel)]="tipoConsulta" class="w-full text-xs border border-gray-300 rounded p-1.5 focus:ring-indigo-500 focus:border-indigo-500 outline-none text-gray-700">
            <option value="REGLAMENTO">Consultar Reglamento</option>
            <option value="REVISION_TEXTO">Revisar Redacción</option>
            <option value="SUGERENCIA_RUBRICA">Sugerir Rúbrica</option>
          </select>
        </div>
        <div class="flex items-end gap-2">
          <textarea 
            [(ngModel)]="currentMessage" 
            (keydown.enter)="handleEnter($event)"
            placeholder="Escribe tu consulta aquí..."
            class="flex-1 resize-none border border-gray-300 rounded-lg p-2 max-h-32 text-sm focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
            rows="2"
            [disabled]="isLoading()">
          </textarea>
          <button (click)="sendMessage()" 
                  [disabled]="!currentMessage.trim() || isLoading()"
                  class="bg-indigo-600 text-white p-2.5 rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shrink-0">
            <lucide-icon name="send" [size]="20"></lucide-icon>
          </button>
        </div>
      </div>
    </div>
  `
})
export class IaChatComponent {
  private iaService = inject(IaAsistenteService);

  readonly Bot = Bot;
  readonly X = X;
  readonly Send = Send;
  readonly Maximize2 = Maximize2;
  readonly Minimize2 = Minimize2;

  isOpen = signal(false);
  isExpanded = signal(false);
  isLoading = signal(false);
  
  messages = signal<{text: string, isUser: boolean}[]>([]);
  currentMessage = '';
  tipoConsulta: 'REGLAMENTO' | 'REVISION_TEXTO' | 'SUGERENCIA_RUBRICA' = 'REGLAMENTO';

  toggleChat() {
    this.isOpen.update(v => !v);
    if (!this.isOpen()) {
      this.isExpanded.set(false);
    }
  }

  toggleExpand() {
    this.isExpanded.update(v => !v);
  }

  handleEnter(event: Event) {
    const keyboardEvent = event as KeyboardEvent;
    if (!keyboardEvent.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  sendMessage() {
    if (!this.currentMessage.trim() || this.isLoading()) return;

    const userText = this.currentMessage;
    this.messages.update(m => [...m, { text: userText, isUser: true }]);
    this.currentMessage = '';
    this.isLoading.set(true);

    const request: IaRequest = {
      tipoConsulta: this.tipoConsulta,
      prompt: userText
    };

    this.iaService.consultar(request).subscribe({
      next: (res) => {
        this.messages.update(m => [...m, { text: res.respuesta, isUser: false }]);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.messages.update(m => [...m, { text: 'Ocurrió un error de conexión con la IA.', isUser: false }]);
        this.isLoading.set(false);
        console.error(err);
      }
    });
  }
}
