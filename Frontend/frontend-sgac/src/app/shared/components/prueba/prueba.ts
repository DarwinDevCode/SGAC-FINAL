import { Component, OnInit, inject } from '@angular/core';
import {AsignaturaService} from '../../../core/services/asignatura-service';
import {AsignaturaDTO} from '../../../core/dto/asignatura';

@Component({
  selector: 'app-prueba',
  imports: [],
  templateUrl: './prueba.html',
  styleUrl: './prueba.css',
})
export class PruebaComponent implements OnInit{
  ngOnInit(): void {
      this.listAsignaturas();
  }

  private asignaturaService = inject(AsignaturaService);
  asignaturas_list: AsignaturaDTO[] = [];

  listAsignaturas(){
    this.asignaturaService.getAsignaturas().subscribe(
      data => {
        this.asignaturas_list = data;
        console.log(this.asignaturas_list);
      }
    );
  }


}
