import { Injectable } from '@angular/core';
import { AUTH_KEYS } from '../../constants/auth.constant';

export interface UserResponse {
  id: number,
  email: string,
  firstName: string,
  lastName: string,
  phone: string,
  status: string,
  createdAt: Date,
  updatedAt: Date
}

@Injectable({ providedIn: 'root' })
export class StorageService {
  // ─── localStorage ───────────────────────────────────────────────────────────

  getLocal<T>(key: string): T | null {
    try {
      const raw = localStorage.getItem(key);
      if (raw === null) return null;
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }

  setLocal<T>(key: string, value: T): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // Silently handle quota exceeded or private-browsing restrictions
    }
  }

  removeLocal(key: string): void {
    localStorage.removeItem(key);
  }

  clearLocal(): void {
    localStorage.clear();
  }

  // ─── sessionStorage ──────────────────────────────────────────────────────────

  getSession<T>(key: string): T | null {
    try {
      const raw = sessionStorage.getItem(key);
      if (raw === null) return null;
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }

  setSession<T>(key: string, value: T): void {
    try {
      sessionStorage.setItem(key, JSON.stringify(value));
    } catch {
      // Silently handle restrictions
    }
  }

  removeSession(key: string): void {
    sessionStorage.removeItem(key);
  }

  clearSession(): void {
    sessionStorage.clear();
  }

  // ─── Raw string helpers (no JSON wrapping) ───────────────────────────────────

  getRawLocal(key: string): any | null {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : null;
  }

  setRawLocal(key: string, value: any): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // Silently handle
    }
  }

  // ─── Token management ────────────────────────────────────────────────────────

  getAccessToken(): string | null {
    return this.getRawLocal(AUTH_KEYS.ACCESS_TOKEN);
  }

  setAccessToken(token: string): void {
    this.setRawLocal(AUTH_KEYS.ACCESS_TOKEN, token);
  }

  getRefreshToken(): string | null {
    return this.getRawLocal(AUTH_KEYS.REFRESH_TOKEN);
  }

  setRefreshToken(token: string): void {
    this.setRawLocal(AUTH_KEYS.REFRESH_TOKEN, token);
  }

  getUserInfo(): string | null {
    return this.getRawLocal(AUTH_KEYS.USER_INFO);
  }

  setUserInfo(userInfo: UserResponse): void {
    this.setRawLocal(AUTH_KEYS.USER_INFO, userInfo);
  }

  clearTokens(): void {
    this.removeLocal(AUTH_KEYS.ACCESS_TOKEN);
    this.removeLocal(AUTH_KEYS.REFRESH_TOKEN);
    this.removeLocal(AUTH_KEYS.USER_INFO);
  }

  hasKey(key: string): boolean {
    return localStorage.getItem(key) !== null;
  }
}
