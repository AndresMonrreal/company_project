import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { AuthTokenStorage } from '../auth/auth-token-storage';
import { API_BASE_URL } from './api-url.token';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(AuthTokenStorage);
  const apiBaseUrl = inject(API_BASE_URL);

  if (!request.url.startsWith(apiBaseUrl)) {
    return next(request);
  }

  if (tokenStorage.isExpired()) {
    return next(request);
  }

  const token = tokenStorage.load();
  if (!token) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: { Authorization: `Bearer ${token.accessToken}` },
    }),
  );
};
