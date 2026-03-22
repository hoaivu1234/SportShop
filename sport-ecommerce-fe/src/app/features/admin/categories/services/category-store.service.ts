import { inject, Injectable, signal } from '@angular/core';
import { CategoryFlatResponse, CategoryResponse } from './category.service';

@Injectable({
  providedIn: 'root',
})
export class CategoryStoreService {
  private _categories = signal<any[]>([]);

  readonly categories = this._categories.asReadonly();

  setCategories(value: any[]) {
    this._categories.set(value);
  }

  addCategory(newCategory: CategoryResponse) {
    const flat = this.mapToFlat(newCategory);

    this._categories.update((prev) => [flat, ...prev]);
  }

  updateCategory(updatedCategory: CategoryResponse) {
    const flat = this.mapToFlat(updatedCategory);

    this._categories.update((prev) => prev.map((cat) => (cat.id === flat.id ? flat : cat)));
  }

  private mapToFlat(category: CategoryResponse): CategoryFlatResponse {
    return {
      id: category.id,
      name: category.name,
      createdAt: category.createdAt,
      productCount: 0,
      slug: category.slug,
      parentId: category.parentId,
      parentName: category.parentName,
    };
  }
}
