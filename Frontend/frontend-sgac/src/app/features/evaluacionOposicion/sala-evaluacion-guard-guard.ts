import { inject } from '@angular/core';
import { CanDeactivateFn } from '@angular/router';
import { SalaEvaluacionComponent } from './sala-evaluacion-component/sala-evaluacion-component';

export const salaEvaluacionGuard: CanDeactivateFn<SalaEvaluacionComponent> =
  (component) => {
    if (!component.hayEvaluacionEnCurso) return true;

    return window.confirm(
      'Hay una evaluación en curso.\n\n' +
      'Si sales, el auto-guardado habrá guardado tu borrador, ' +
      'pero no podrás volver al timer sincronizado.\n\n' +
      '¿Deseas salir de todas formas?'
    );
  };
