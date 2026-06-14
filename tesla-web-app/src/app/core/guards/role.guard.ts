import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthSession } from '../auth/auth-session';

export const roleGuard: CanActivateFn = (route) => {
  const session = inject(AuthSession);
  const router = inject(Router);

  const allowedRoles = route.data['roles'] as string[] | undefined;
  const currentRole = session.role();

  if (currentRole !== null && allowedRoles?.includes(currentRole)) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
