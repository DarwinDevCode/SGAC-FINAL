import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth-service';

@Component({
    selector: 'app-coordinador-seguimiento',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
    templateUrl: './seguimiento.html',
    styleUrl: './seguimiento.css',
})
export class SeguimientoComponent implements OnInit, OnDestroy {
    private http = inject(HttpClient);
    private authService = inject(AuthService);
    private subs = new Subscription();

    ayudantes: any[] = [];
    loading = true;
    errorMensaje = '';

    ngOnInit(): void { this.cargarAyudantes(); }
    ngOnDestroy(): void { this.subs.unsubscribe(); }

    cargarAyudantes() {
        this.loading = true;
        this.subs.add(
            this.http.get<any[]>('http://localhost:8080/api/ayudantes/listar').subscribe({
                next: (data) => { this.ayudantes = data || []; this.loading = false; },
                error: () => { this.errorMensaje = 'No se pudieron cargar los ayudantes.'; this.loading = false; }
            })
        );
    }
}
