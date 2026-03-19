// src/app/core/guards/auth.guard.ts
import { inject }          from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService }     from '../../core/services/auth-service';


export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.getToken()) return true;

  router.navigate(['/login']);
  return false;
};

export const coordinadorGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  const user = auth.getUser();
  if (!user || !auth.getToken()) {
    router.navigate(['/login']);
    return false;
  }

  if (user.rolActual?.toUpperCase() !== 'COORDINADOR') {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
