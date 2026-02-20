import { Component, OnInit } from '@angular/core';
import {AsignaturaService} from '../../core/services/asignatura';
import {AsignaturaDTO} from '../../core/dto/Asignatura';

@Component({
  selector: 'app-practica',
  imports: [],
  templateUrl: './practica.html',
  styleUrl: './practica.css',
})
export class PracticaComponent implements OnInit{
  ngOnInit(): void {
    this.listAsignaturas();
  }

  constructor(private asignaturaService:AsignaturaService) {
  }

  asignaturas_list: AsignaturaDTO[] = [];

  listAsignaturas(){
    this.asignaturaService.getAsignaturaList().subscribe(
      data => {
        this.asignaturas_list = data;
        console.log(this.asignaturas_list);
      }
    );
  }

}
