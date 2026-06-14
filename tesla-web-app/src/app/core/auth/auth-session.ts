import { computed, inject, Injectable, signal } from '@angular/core';
import { AuthTokenStorage } from './auth-token-storage';
import { UserSession } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthSession {
  private readonly tokenStorage = inject(AuthTokenStorage);

  private readonly _session = signal<UserSession | null>(null);

  readonly session = this._session.asReadonly();

  readonly role = computed(() => this._session()?.role ?? null);

  readonly initials = computed(() => {
    const user = this._session();
    if (!user) {
      return '';
    }
    const words = user.fullName.trim().split(/\s+/);
    if (words.length >= 2) {
      return (words[0][0] + words[words.length - 1][0]).toUpperCase();
    }
    return user.username.slice(0, 2).toUpperCase();
  });

  constructor() {
    const token = this.tokenStorage.load();
    if (token !== null && !this.tokenStorage.isExpired()) {
      const user = this.tokenStorage.loadUser();
      if (user !== null) {
        this.set(user);
      }
    }
  }

  set(user: UserSession): void {
    this._session.set(user);
  }

  clear(): void {
    this._session.set(null);
  }
}
