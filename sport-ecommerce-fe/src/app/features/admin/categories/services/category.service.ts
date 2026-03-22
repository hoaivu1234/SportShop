import { inject, Injectable } from '@angular/core';
import { BaseHttpService } from '../../../../core/services/base-http.service';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../../models/api-response.model';
import { CATEGORY_API } from '../../../../core/constants/api-path.constant';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  page: number;
  last: boolean;
}

interface CategoryRequest {
  name: string;
  parentId?: number;
}

export interface CategoryResponse {
  id: number;
  name: string;
  createdAt: Date;
  parentId: string;
  parentName: string;
  slug: string;
}

export interface CategoryFlatResponse {
  id: number;
  name: string;
  createdAt: Date;
  productCount: number;
  slug: string;
  parentId: string;
  parentName: string;
}

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(BaseHttpService);

  getFlatCategories(): Observable<ApiResponse<PageResponse<CategoryFlatResponse>>> {
    return this.http.get<PageResponse<CategoryFlatResponse>>(CATEGORY_API.FLAT);
  }

  createCategory(data: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.post<CategoryResponse>(CATEGORY_API.BASE, data);
  }

  updateCategory(id: number, data: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.put<CategoryResponse>(`${CATEGORY_API.BASE}/${id}`, data);
  }
}
