const API_BASE = '/api/v1';

export const AUTH_API = {
  LOGIN: `${API_BASE}/auth/login`,
  REGISTER: `${API_BASE}/auth/register`,
  LOGOUT: `${API_BASE}/auth/logout`,
  REFRESH: `${API_BASE}/auth/refresh-token`,
  ME: `${API_BASE}/auth/me`,
} as const;

export const USER_API = {
  BASE: `${API_BASE}/users`,
  BY_ID: (id: number | string) => `${API_BASE}/users/${id}`,
  PROFILE: `${API_BASE}/users/profile`,
  CHANGE_PASSWORD: `${API_BASE}/users/change-password`,
  AVATAR: (id: number | string) => `${API_BASE}/users/${id}/avatar`,
} as const;

export const PRODUCT_API = {
  BASE: `${API_BASE}/products`,
  BY_ID: (id: number | string) => `${API_BASE}/products/${id}`,
  SEARCH: `${API_BASE}/products/search`,
  BY_CATEGORY: (categoryId: number | string) => `${API_BASE}/products/category/${categoryId}`,
  FEATURED: `${API_BASE}/products/featured`,
} as const;

export const CATEGORY_API = {
  BASE: `${API_BASE}/categories`,
  BY_ID: (id: number | string) => `${API_BASE}/categories/${id}`,
  TREE: `${API_BASE}/categories/tree`,
} as const;

export const ORDER_API = {
  BASE: `${API_BASE}/orders`,
  BY_ID: (id: number | string) => `${API_BASE}/orders/${id}`,
  MY_ORDERS: `${API_BASE}/orders/my`,
  CANCEL: (id: number | string) => `${API_BASE}/orders/${id}/cancel`,
  STATUS: (id: number | string) => `${API_BASE}/orders/${id}/status`,
} as const;

export const CART_API = {
  BASE: `${API_BASE}/cart`,
  ADD: `${API_BASE}/cart/items`,
  UPDATE: (itemId: number | string) => `${API_BASE}/cart/items/${itemId}`,
  REMOVE: (itemId: number | string) => `${API_BASE}/cart/items/${itemId}`,
  CLEAR: `${API_BASE}/cart/clear`,
} as const;

export const REVIEW_API = {
  BASE: `${API_BASE}/reviews`,
  BY_PRODUCT: (productId: number | string) => `${API_BASE}/products/${productId}/reviews`,
  BY_ID: (id: number | string) => `${API_BASE}/reviews/${id}`,
} as const;

export const ADMIN_API = {
  DASHBOARD: `${API_BASE}/admin/dashboard/stats`,
  USERS: `${API_BASE}/admin/users`,
  USER_BY_ID: (id: number | string) => `${API_BASE}/admin/users/${id}`,
  PRODUCTS: `${API_BASE}/admin/products`,
  ORDERS: `${API_BASE}/admin/orders`,
  REPORTS_SALES: `${API_BASE}/admin/reports/sales`,
  REPORTS_PRODUCTS: `${API_BASE}/admin/reports/products`,
} as const;
