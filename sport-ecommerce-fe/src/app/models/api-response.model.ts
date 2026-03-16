export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
  errorCode?: string;
  errors?: Record<string, string[]>;
}

export interface ApiErrorResponse {
  success: false;
  message: string;
  errorCode: string;
  timestamp: string;
  errors?: Record<string, string[]>;
}

export type ApiResult<T> = ApiResponse<T> | ApiErrorResponse;
