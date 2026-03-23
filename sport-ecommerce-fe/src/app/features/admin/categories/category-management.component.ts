import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryService } from './services/category.service';
import { CategoryStoreService } from './services/category-store.service';
import { CategoryFormComponent } from './components/category-form/category-form.component';
import { CategoryTableComponent } from './components/category-table/category-table.component';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, CategoryFormComponent, CategoryTableComponent],
  templateUrl: './category-management.component.html',
  styleUrl: './category-management.component.css',
})
export class CategoryManagementComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);
  readonly categoryStore = inject(CategoryStoreService);

  showModal = false;
  readonly mode = 'create';

  readonly tree = this.categoryStore.tree;
  readonly totalCategories = this.categoryStore.allFlat;

  /** IDs of currently expanded nodes — local UI state only */
  private readonly expandedIds = signal(new Set<number>());

  ngOnInit(): void {
    this.loadSharedData();
  }

  /** Load tree (for left panel) and full flat list (for parent dropdown) */
  loadSharedData(): void {
    this.categoryService.getTreeCategories().subscribe({
      next: (res) => this.categoryStore.setTree(res.data),
      error: (err) => console.error('Error loading category tree:', err),
    });

    this.categoryService.getAllCategories().subscribe({
      next: (res) => this.categoryStore.setAllFlat(res.data),
      error: (err) => console.error('Error loading all categories:', err),
    });
  }

  /** Called when the form successfully creates or updates a category */
  onSaved(): void {
    this.loadSharedData();
  }

  isExpanded(id: number): boolean {
    return this.expandedIds().has(id);
  }

  toggleTree(id: number): void {
    this.expandedIds.update(current => {
      const next = new Set(current);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  openModal(): void { this.showModal = true; }

  closeModal(): void { this.showModal = false; }
}
