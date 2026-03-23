import { Injectable, signal } from '@angular/core';
import { CategoryFlatResponse, CategoryTreeNode } from './category.service';

@Injectable({ providedIn: 'root' })
export class CategoryStoreService {

  // ── Paginated flat list (table) ────────────────────────────────────────────
  private _categories = signal<CategoryFlatResponse[]>([]);
  private _totalElements = signal(0);

  readonly categories = this._categories.asReadonly();
  readonly totalElements = this._totalElements.asReadonly();

  setCategories(value: CategoryFlatResponse[]): void {
    this._categories.set(value);
  }

  setTotalElements(value: number): void {
    this._totalElements.set(value);
  }

  // ── Tree — built by backend, stored as-is ─────────────────────────────────
  private _tree = signal<CategoryTreeNode[]>([]);
  readonly tree = this._tree.asReadonly();

  setTree(value: CategoryTreeNode[]): void {
    this._tree.set(value);
  }

  // ── All categories flat — used for the parent dropdown ────────────────────
  private _allFlat = signal<CategoryFlatResponse[]>([]);
  readonly allFlat = this._allFlat.asReadonly();

  setAllFlat(value: CategoryFlatResponse[]): void {
    this._allFlat.set(value);
  }
}
