import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthSession } from '../auth/auth-session';
import { AuthTokenStorage } from '../auth/auth-token-storage';

export const authErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(AuthSession);
  const tokenStorage = inject(AuthTokenStorage);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 401) {
          tokenStorage.clear();
          session.clear();
          router.navigateByUrl('/login');
        }
        // 403: keep session, rethrow — caller decides how to render forbidden state
      }
      return throwError(() => error);
    }),
  );
};
