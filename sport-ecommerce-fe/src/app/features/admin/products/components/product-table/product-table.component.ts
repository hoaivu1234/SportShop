import {
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProductService } from '../../services/product.service';
import { CategoryService, CategoryFlatResponse } from '../../../categories/services/category.service';
import { ProductListResponse } from '../../../../../models/product.model';
import { PaginationComponent } from '../../../../../shared/components/pagination/pagination.component';
import { ToastService } from '../../../../../core/services/toast.service';

interface ProductRow extends ProductListResponse {
  checked: boolean;
}

@Component({
  selector: 'app-product-table',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationComponent],
  templateUrl: './product-table.component.html',
  styleUrl: './product-table.component.css',
})
export class ProductTableComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  // ── Table data ─────────────────────────────────────────────────────────────
  products = signal<ProductRow[]>([]);
  categories = signal<CategoryFlatResponse[]>([]);
  totalElements = signal(0);
  isLoading = signal(false);

  // ── Pagination ─────────────────────────────────────────────────────────────
  page = signal(0);
  pageSize = signal(10);
  readonly pageSizeOptions = [5, 10, 20];

  // ── Filters ────────────────────────────────────────────────────────────────
  /** Raw input value — updated on every keystroke for use in other filter handlers */
  keywordInput = '';
  categoryId = signal<number | null>(null);
  statusFilter = signal('');

  private readonly keywordSubject = new Subject<string>();

  readonly statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'INACTIVE', label: 'Inactive' },
    { value: 'DRAFT', label: 'Draft' },
  ];

  allChecked = false;
  isExporting = signal(false);

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.loadCategories();

    // Debounce keyword input — fires 400 ms after the user stops typing
    this.keywordSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => {
      this.page.set(0);
      this.loadProducts();
    });

    this.loadProducts();
  }

  // ── Data loading ───────────────────────────────────────────────────────────

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (res) => this.categories.set(res.data),
      error: () => {},
    });
  }

  loadProducts(): void {
    this.isLoading.set(true);
    this.productService.getProducts({
      page: this.page(),
      size: this.pageSize(),
      sort: 'createdAt',
      direction: 'desc',
      keyword: this.keywordInput || undefined,
      categoryId: this.categoryId() ?? undefined,
      status: this.statusFilter() || undefined,
    }).subscribe({
      next: (res) => {
        this.products.set(res.data.content.map(p => ({ ...p, checked: false })));
        this.totalElements.set(res.data.totalElements);
        this.allChecked = false;
      },
      error: () => this.toast.error('Failed to load products.'),
      complete: () => this.isLoading.set(false),
    });
  }

  // ── Filter handlers ────────────────────────────────────────────────────────

  onKeywordChange(value: string): void {
    this.keywordInput = value;
    this.keywordSubject.next(value);
  }

  onCategoryChange(value: string): void {
    this.categoryId.set(value ? +value : null);
    this.page.set(0);
    this.loadProducts();
  }

  onStatusChange(value: string): void {
    this.statusFilter.set(value);
    this.page.set(0);
    this.loadProducts();
  }

  onPageSizeChange(value: string): void {
    this.pageSize.set(+value);
    this.page.set(0);
    this.loadProducts();
  }

  clearFilters(): void {
    this.keywordInput = '';
    this.keywordSubject.next(''); // cancel any pending debounce
    this.categoryId.set(null);
    this.statusFilter.set('');
    this.page.set(0);
    this.loadProducts();
  }

  get hasActiveFilters(): boolean {
    return !!(this.keywordInput || this.categoryId() || this.statusFilter());
  }

  // ── Export ─────────────────────────────────────────────────────────────────

  /** Returns the filters currently active in the table — used by the parent for CSV export. */
  getActiveFilters() {
    return {
      keyword: this.keywordInput || undefined,
      categoryId: this.categoryId() ?? undefined,
      status: this.statusFilter() || undefined,
    };
  }

  exportCsv(): void {
    if (this.isExporting()) return;
    this.isExporting.set(true);

    this.productService.exportProducts(this.getActiveFilters()).subscribe({
      next: (blob) => {
        const date = new Date().toISOString().split('T')[0];
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `products_${date}.csv`;
        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);
        URL.revokeObjectURL(url);
      },
      error: () => this.toast.error('Export failed. Please try again.'),
      complete: () => this.isExporting.set(false),
    });
  }

  // ── Pagination ─────────────────────────────────────────────────────────────

  onPageChange(newPage: number): void {
    this.page.set(newPage);
    this.loadProducts();
  }

  // ── Row actions ────────────────────────────────────────────────────────────

  navigateToEdit(id: number): void {
    this.router.navigate(['/admin/products', id, 'edit']);
  }

  deleteProduct(id: number): void {
    if (!confirm('Are you sure you want to delete this product?')) return;
    this.productService.deleteProduct(id).subscribe({
      next: () => {
        this.toast.success('Product deleted.');
        this.loadProducts();
      },
      error: () => this.toast.error('Failed to delete product.'),
    });
  }

  // ── Selection ──────────────────────────────────────────────────────────────

  toggleAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.allChecked = checked;
    this.products.update(list => list.map(p => ({ ...p, checked })));
  }

  toggleOne(): void {
    this.allChecked = this.products().every(p => p.checked);
  }

  // ── UI helpers ─────────────────────────────────────────────────────────────

  statusBadgeClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':   return 'badge-active';
      case 'INACTIVE': return 'badge-inactive';
      case 'DRAFT':    return 'badge-draft';
      default:         return 'badge-draft';
    }
  }

  stockClass(stock: number): string {
    if (stock === 0)  return 'red';
    if (stock <= 10)  return 'orange';
    return 'green';
  }
}
