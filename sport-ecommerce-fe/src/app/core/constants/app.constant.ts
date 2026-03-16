export const APP_CONFIG = {
  APP_NAME: 'Sport E-Commerce',
  APP_VERSION: '1.0.0',
  DEFAULT_PAGE_SIZE: 12,
  MAX_PAGE_SIZE: 100,
  DEFAULT_CURRENCY: 'VND',
  DEFAULT_LANGUAGE: 'vi',
  DATE_FORMAT: 'dd/MM/yyyy',
  DATETIME_FORMAT: 'dd/MM/yyyy HH:mm',
  CURRENCY_LOCALE: 'vi-VN',
} as const;

export const TOAST_CONFIG = {
  DURATION_MS: 3000,
  ERROR_DURATION_MS: 5000,
  MAX_TOASTS: 5,
} as const;

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
} as const;

export type HttpStatusCode = (typeof HTTP_STATUS)[keyof typeof HTTP_STATUS];

export const STORAGE_KEYS = {
  CART: 'cart',
  THEME: 'theme',
  LANGUAGE: 'language',
  RECENT_SEARCHES: 'recent_searches',
  WISHLIST: 'wishlist',
} as const;

export const ERROR_MESSAGES: Record<number, string> = {
  [HTTP_STATUS.BAD_REQUEST]: 'Yêu cầu không hợp lệ.',
  [HTTP_STATUS.UNAUTHORIZED]: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
  [HTTP_STATUS.FORBIDDEN]: 'Bạn không có quyền thực hiện thao tác này.',
  [HTTP_STATUS.NOT_FOUND]: 'Không tìm thấy tài nguyên yêu cầu.',
  [HTTP_STATUS.INTERNAL_SERVER_ERROR]: 'Lỗi máy chủ. Vui lòng thử lại sau.',
  [HTTP_STATUS.SERVICE_UNAVAILABLE]: 'Dịch vụ tạm thời không khả dụng.',
};
