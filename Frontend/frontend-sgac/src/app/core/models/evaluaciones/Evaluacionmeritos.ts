export type EstadoEvalMeritos = 'BORRADOR' | 'FINALIZADA';
export type EstadoPostMeritos = 'APROBADA' | 'EN_EVALUACION';

export interface EvaluacionMeritosDTO {
  idEvaluacionMeritos: number;
  notaAsignatura:      number;
  notaSemestres:       number;
  notaExperiencia:     number;
  notaEventos:         number;
  notaTotal:           number;
  estado:              EstadoEvalMeritos;
  nombreEstado:        string;
  fechaEvaluacion:     string;
}

export interface EvaluacionMeritosResponse {
  exito:              boolean;
  mensaje?:           string;
  faseActiva?:        boolean;
  idPostulacion?:     number;
  nombres?:           string;
  apellidos?:         string;
  correo?:            string;
  matricula?:         string;
  semestreEstudiante?: number;
  nombreAsignatura?:  string;
  semestreAsignatura?: number;
  nombreCarrera?:     string;
  estadoPostulacion?: EstadoPostMeritos;
  evaluacion?:        EvaluacionMeritosDTO | null;
}

export interface PostulacionParaMeritosItem {
  idPostulacion:       number;
  nombres:             string;
  apellidos:           string;
  correo:              string;
  matricula:           string;
  semestreEstudiante:  number;
  nombreAsignatura:    string;
  semestreAsignatura:  number;
  nombreCarrera:       string;
  estadoPostulacion:   EstadoPostMeritos;
  estadoEvaluacion?:   EstadoEvalMeritos;
  nombreEstadoEval?:   string;
  notaTotal?:          number;
  fechaEvaluacion?:    string;
  idEvaluacionMeritos?: number;
}

export interface ListaPostulacionesMeritosResponse {
  exito:          boolean;
  mensaje?:       string;
  faseActiva?:    boolean;
  postulaciones?: PostulacionParaMeritosItem[];
}

export interface GuardarMeritosRequest {
  idPostulacion:             number;
  notaAprobacionAsignatura:  number;
  semestresNotas:            number[];
  notaExperiencia:           number;
  notaEventos:               number;
  finalizar:                 boolean;
}

export interface GuardarMeritosResponse {
  exito:               boolean;
  mensaje?:            string;
  idEvaluacionMeritos?: number;
  notaAsignatura?:     number;
  notaSemestres?:      number;
  notaExperiencia?:    number;
  notaEventos?:        number;
  notaTotal?:          number;
  finalizada?:         boolean;
}

export function calcularNotaAsignatura(nota: number): number {
  if (nota >= 9.50) return 10.00;
  if (nota >= 9.00) return  9.00;
  if (nota >= 8.50) return  8.00;
  if (nota >= 8.00) return  7.00;
  return 0.00;
}

export function calcularNotaSemestres(notas: number[]): number {
  const suma = notas.reduce((acc, nota) => {
    if (nota >= 9.50) return acc + 1.00;
    if (nota >= 9.00) return acc + 0.70;
    if (nota >= 8.50) return acc + 0.50;
    if (nota >= 8.00) return acc + 0.25;
    return acc;
  }, 0);
  return Math.min(Math.round(suma * 100) / 100, 4.00);
}

export function calcularTotal(
  notaAsig: number,
  notaSem: number,
  exp: number,
  eventos: number
): number {
  return Math.round((notaAsig + notaSem + exp + eventos) * 100) / 100;
}

export function puntajesPorNota(nota: number): number {
  if (nota >= 9.50) return 1.00;
  if (nota >= 9.00) return 0.70;
  if (nota >= 8.50) return 0.50;
  if (nota >= 8.00) return 0.25;
  return 0.00;
}
