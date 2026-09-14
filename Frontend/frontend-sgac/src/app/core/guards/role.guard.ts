import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = route.data['roles'] as Array<string>;
  const currentRole = authService.getRolActivo();

  if (allowedRoles && currentRole && allowedRoles.includes(currentRole)) {
    return true;
  }

  return router.createUrlTree(['/forbidden']);
};
