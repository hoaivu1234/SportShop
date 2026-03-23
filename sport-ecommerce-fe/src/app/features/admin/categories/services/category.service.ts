import { inject, Injectable } from '@angular/core';
import { BaseHttpService } from '../../../../core/services/base-http.service';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../../models/api-response.model';
import { PageApiResponse } from '../../../../models/page-response.model';
import { PaginationParams } from '../../../../models/pagination.model';
import { CATEGORY_API } from '../../../../core/constants/api-path.constant';

interface CategoryRequest {
  name: string;
  parentId?: number | null;
}

export interface CategoryResponse {
  id: number;
  name: string;
  createdAt: Date;
  parentId: number | null;
  parentName: string | null;
  slug: string;
}

export interface CategoryFlatResponse {
  id: number;
  name: string;
  createdAt: Date;
  productCount: number;
  slug: string;
  parentId: number | null;
  parentName: string | null;
}

/** Recursive tree node returned by /categories/tree — fully built by backend */
export interface CategoryTreeNode {
  id: number;
  name: string;
  slug: string;
  /** Products directly assigned to this category */
  productCount: number;
  /** productCount + all descendants — computed server-side */
  totalProductCount: number;
  children: CategoryTreeNode[];
}

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(BaseHttpService);

  getFlatCategories(pagination: PaginationParams): Observable<PageApiResponse<CategoryFlatResponse>> {
    return this.http.getPaged<CategoryFlatResponse>(CATEGORY_API.FLAT, pagination);
  }

  /** Returns all categories as a flat list (no pagination) — used for dropdown */
  getAllCategories(): Observable<ApiResponse<CategoryFlatResponse[]>> {
    return this.http.get<CategoryFlatResponse[]>(CATEGORY_API.ALL);
  }

  /** Returns the full category tree built by the backend */
  getTreeCategories(): Observable<ApiResponse<CategoryTreeNode[]>> {
    return this.http.get<CategoryTreeNode[]>(CATEGORY_API.TREE);
  }

  createCategory(data: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.post<CategoryResponse>(CATEGORY_API.BASE, data);
  }

  updateCategory(id: number, data: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.put<CategoryResponse>(`${CATEGORY_API.BASE}/${id}`, data);
  }
}
