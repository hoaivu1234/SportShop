import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryService } from '../../services/category.service';
import { CategoryStoreService } from '../../services/category-store.service';
import { CategoryFormComponent } from '../category-form/category-form.component';
import { PaginationComponent } from '../../../../../shared/components/pagination/pagination.component';
import { PaginationParams } from '../../../../../models/pagination.model';
import { NoResultsComponent } from '../../../../search/components/no-results/no-results.component';

@Component({
  selector: 'app-category-table',
  standalone: true,
  imports: [CommonModule, CategoryFormComponent, PaginationComponent, NoResultsComponent],
  templateUrl: './category-table.component.html',
  styleUrl: './category-table.component.css',
})
export class CategoryTableComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);
  readonly categoryStore = inject(CategoryStoreService);

  readonly mode = 'edit';
  showModal = false;
  selectedCategory: any = null;

  readonly categories = this.categoryStore.categories;
  readonly totalElements = this.categoryStore.totalElements;

  pagination = signal<PaginationParams>({
    page: 0,
    size: 5,
    sort: 'createdAt',
    direction: 'desc',
  });

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getFlatCategories(this.pagination()).subscribe({
      next: (res) => {
        this.categoryStore.setCategories(res.data.content);
        this.categoryStore.setTotalElements(res.data.totalElements);
      },
      error: (err) => console.error('Error fetching categories:', err),
    });
  }

  onPageChange(page: number): void {
    this.pagination.update(p => ({ ...p, page }));
    this.loadCategories();
  }

  openModal(cat: any): void {
    this.selectedCategory = cat;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  /** After a successful save, refresh the table to reflect the latest data */
  onSaved(): void {
    this.loadCategories();
  }
}
