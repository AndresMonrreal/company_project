import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable, tap } from 'rxjs';
import { AuthApiClient } from './auth-api.client';
import { AuthSession } from './auth-session';
import { AuthTokenStorage } from './auth-token-storage';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthApiClient);
  private readonly tokenStorage = inject(AuthTokenStorage);
  private readonly session = inject(AuthSession);
  private readonly router = inject(Router);

  login(username: string, password: string): Observable<void> {
    return this.api.login(username, password).pipe(
      tap((response) => {
        this.tokenStorage.save(
          { accessToken: response.accessToken, expiresAt: response.expiresAt },
          response.user,
        );
        this.session.set(response.user);
        this.router.navigateByUrl('/dashboard');
      }),
      map(() => undefined),
    );
  }

  logout(): void {
    this.tokenStorage.clear();
    this.session.clear();
    this.router.navigateByUrl('/login');
  }

  isAuthenticated(): boolean {
    return !this.tokenStorage.isExpired() && this.session.session() !== null;
  }
}
