export interface MateriaDTO {
  idMateria: number;
  nombreMateria: string;
}

export interface TemaDTO {
  idTema: number;
  nombreTema: string;
  idMateria: number;
  nombreMateria: string;
}
