import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {UsuarioComisionDTO} from '../../../core/dto/usuario-comision';
import {ComisionSeleccionService} from '../../../core/services/comision-seleccion-service';

@Component({
    selector: 'app-revision',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './revision.component.html',
    styleUrl: './revision.component.css'
})
export class RevisionComponent implements OnInit {
    asignaciones: UsuarioComisionDTO[] = [];
    comisionService = inject(ComisionSeleccionService);

    ngOnInit(): void {
        const idUsuario = 1;
        this.comisionService.listarMisAsignaciones(idUsuario).subscribe({
            next: (data) => this.asignaciones = data,
            error: (err) => console.error(err)
        });
    }
}
