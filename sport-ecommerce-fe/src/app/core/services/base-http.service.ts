import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiResponse } from '../../models/api-response.model';
import { PageApiResponse } from '../../models/page-response.model';
import { PaginationParams } from '../../models/pagination.model';
import { ERROR_MESSAGES, HTTP_STATUS } from '../constants/app.constant';

type QueryRecord = Record<string, string | number | boolean | string[] | number[] | undefined | null>;

@Injectable({ providedIn: 'root' })
export class BaseHttpService {
  private readonly http = inject(HttpClient);

  // ─── Generic CRUD wrappers ───────────────────────────────────────────────────

  get<T>(url: string, params?: QueryRecord): Observable<ApiResponse<T>> {
    return this.http
      .get<ApiResponse<T>>(url, { params: this.buildParams(params) })
      .pipe(catchError(err => this.handleError(err)));
  }

  getPaged<T>(url: string, pagination: PaginationParams, extra?: QueryRecord): Observable<PageApiResponse<T>> {
    const params: QueryRecord = {
      page: pagination.page,
      size: pagination.size,
      ...(pagination.sort ? { sort: `${pagination.sort},${pagination.direction ?? 'desc'}` } : {}),
      ...extra,
    };
    return this.http
      .get<PageApiResponse<T>>(url, { params: this.buildParams(params) })
      .pipe(catchError(err => this.handleError(err)));
  }

  post<T>(url: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http
      .post<ApiResponse<T>>(url, body)
      .pipe(catchError(err => this.handleError(err)));
  }

  put<T>(url: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http
      .put<ApiResponse<T>>(url, body)
      .pipe(catchError(err => this.handleError(err)));
  }

  patch<T>(url: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http
      .patch<ApiResponse<T>>(url, body)
      .pipe(catchError(err => this.handleError(err)));
  }

  delete<T>(url: string): Observable<ApiResponse<T>> {
    return this.http
      .delete<ApiResponse<T>>(url)
      .pipe(catchError(err => this.handleError(err)));
  }

  postFormData<T>(url: string, formData: FormData): Observable<ApiResponse<T>> {
    return this.http
      .post<ApiResponse<T>>(url, formData)
      .pipe(catchError(err => this.handleError(err)));
  }

  /** Downloads a binary/text file as a Blob — used for CSV/PDF export endpoints. */
  getBlob(url: string, params?: QueryRecord): Observable<Blob> {
    return this.http.get(url, {
      params: this.buildParams(params),
      responseType: 'blob',
    });
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  private buildParams(params?: QueryRecord): HttpParams {
    let httpParams = new HttpParams();
    if (!params) return httpParams;

    for (const [key, value] of Object.entries(params)) {
      if (value === null || value === undefined) continue;

      if (Array.isArray(value)) {
        value.forEach(v => (httpParams = httpParams.append(key, String(v))));
      } else {
        httpParams = httpParams.set(key, String(value));
      }
    }
    return httpParams;
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    const defaultMsg =
      ERROR_MESSAGES[error.status] ??
      'Đã xảy ra lỗi không xác định. Vui lòng thử lại.';

    const serverMessage: string =
      (error.error as ApiResponse<unknown>)?.message ?? defaultMsg;

    const enriched = new HttpErrorResponse({
      error: { ...error.error, resolvedMessage: serverMessage },
      headers: error.headers,
      status: error.status,
      statusText: error.statusText,
      url: error.url ?? undefined,
    });

    return throwError(() => enriched);
  }
}
