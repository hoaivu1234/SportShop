import { AUTH_KEYS } from '../../constants/auth.constant';

interface JwtPayload {
  sub?: string;
  exp?: number;
  iat?: number;
  roles?: string[];
  authorities?: string[];
  userId?: number;
  email?: string;
}

/**
 * Pure static utility for JWT operations.
 * Does NOT depend on Angular DI — safe to use anywhere including guards.
 */
export class AuthUtil {
  static getAccessToken(): string | null {
    return localStorage.getItem(AUTH_KEYS.ACCESS_TOKEN);
  }

  static getUserInfo(): string | null {
    return localStorage.getItem(AUTH_KEYS.USER_INFO);
  }

  static getRefreshToken(): string | null {
    return localStorage.getItem(AUTH_KEYS.REFRESH_TOKEN);
  }

  static isLoggedIn(): boolean {
    const token = AuthUtil.getAccessToken();
    const userInfo = AuthUtil.getUserInfo();

    if (token && !AuthUtil.isTokenExpired(token)) {
      return true;
    }

    if (userInfo) {
      return true;
    }

    return false;
  }

  /**
   * Decodes the JWT payload WITHOUT verifying the signature.
   * Verification must be done server-side.
   */
  static decodeJwt<T = JwtPayload>(token: string): T | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;

      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      const jsonPayload = decodeURIComponent(
        atob(padded)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join(''),
      );
      return JSON.parse(jsonPayload) as T;
    } catch {
      return null;
    }
  }

  static getUserRoles(): string[] {
    const token = AuthUtil.getAccessToken();
    if (!token) return [];
    const payload = AuthUtil.decodeJwt<JwtPayload>(token);
    return payload?.roles ?? payload?.authorities ?? [];
  }

  static hasRole(role: string): boolean {
    return AuthUtil.getUserRoles().includes(role);
  }

  static hasAnyRole(roles: string[]): boolean {
    const userRoles = AuthUtil.getUserRoles();
    return roles.some(role => userRoles.includes(role));
  }

  static getUserId(): number | null {
    const token = AuthUtil.getAccessToken();
    if (!token) return null;
    const payload = AuthUtil.decodeJwt<JwtPayload>(token);
    return payload?.userId ?? null;
  }

  static getSubject(): string | null {
    const token = AuthUtil.getAccessToken();
    if (!token) return null;
    const payload = AuthUtil.decodeJwt<JwtPayload>(token);
    return payload?.sub ?? null;
  }

  static isTokenExpired(token: string): boolean {
    const payload = AuthUtil.decodeJwt<JwtPayload>(token);
    if (!payload?.exp) return true;
    // Add 5-second clock skew buffer
    return Date.now() >= (payload.exp - 5) * 1000;
  }

  static getTokenExpiryDate(token: string): Date | null {
    const payload = AuthUtil.decodeJwt<JwtPayload>(token);
    if (!payload?.exp) return null;
    return new Date(payload.exp * 1000);
  }
}
