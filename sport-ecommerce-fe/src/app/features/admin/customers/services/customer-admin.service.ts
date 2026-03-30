import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';
import { CustomerDetail, CustomerListParams, CustomerSummary } from '../models/customer-admin.model';

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class CustomerAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/v1/admin/customers`;

  getCustomers(params: CustomerListParams): Observable<PageResponse<CustomerSummary>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 10);
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.status)  httpParams = httpParams.set('status',  params.status);

    return this.http
      .get<ApiResponse<PageResponse<CustomerSummary>>>(this.base, { params: httpParams })
      .pipe(map(res => res.data));
  }

  getCustomerById(id: number): Observable<CustomerDetail> {
    return this.http
      .get<ApiResponse<CustomerDetail>>(`${this.base}/${id}`)
      .pipe(map(res => res.data));
  }
}
