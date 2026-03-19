export const AUTH_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_INFO: 'user_info',
} as const;

export type AuthKey = (typeof AUTH_KEYS)[keyof typeof AUTH_KEYS];

export const TOKEN_PREFIX = 'Bearer';
export const AUTH_HEADER = 'Authorization';

export const ROLES = {
  ADMIN: 'ROLE_ADMIN',
  USER: 'ROLE_USER',
  MODERATOR: 'ROLE_MODERATOR',
} as const;

export type Role = (typeof ROLES)[keyof typeof ROLES];

export const AUTH_ROUTES = {
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  FORBIDDEN: '/forbidden',
  HOME: '/',
  ADMIN_DASHBOARD: '/admin/dashboard',
} as const;

/** Public routes that skip the auth interceptor */
export const PUBLIC_URLS: readonly string[] = [
  '/api/v1/auth/login',
  '/api/v1/auth/register',
  '/api/v1/auth/refresh-token',
] as const;
