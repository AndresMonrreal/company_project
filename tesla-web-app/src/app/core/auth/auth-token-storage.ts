import { Injectable } from '@angular/core';
import { StoredToken, UserSession } from './auth.models';

const TOKEN_KEY = 'rubbertrace_token';
const SESSION_KEY = 'rubbertrace_session';

@Injectable({ providedIn: 'root' })
export class AuthTokenStorage {
  save(token: StoredToken, user?: UserSession): void {
    localStorage.setItem(TOKEN_KEY, JSON.stringify(token));
    if (user !== undefined) {
      localStorage.setItem(SESSION_KEY, JSON.stringify(user));
    }
  }

  load(): StoredToken | null {
    const raw = localStorage.getItem(TOKEN_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as StoredToken;
    } catch {
      return null;
    }
  }

  loadUser(): UserSession | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as UserSession;
    } catch {
      return null;
    }
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESSION_KEY);
  }

  isExpired(): boolean {
    const token = this.load();
    if (!token) {
      return true;
    }
    return new Date(token.expiresAt).getTime() <= Date.now();
  }
}
