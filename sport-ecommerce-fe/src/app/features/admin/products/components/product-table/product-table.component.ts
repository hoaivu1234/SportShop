import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
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
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  products = signal<ProductRow[]>([]);
  totalElements = signal(0);
  isLoading = signal(false);

  page = signal(0);
  readonly pageSize = 10;

  allChecked = false;

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading.set(true);
    this.productService.getProducts({
      page: this.page(),
      size: this.pageSize,
      sort: 'createdAt',
      direction: 'desc',
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

  onPageChange(newPage: number): void {
    this.page.set(newPage);
    this.loadProducts();
  }

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

  toggleAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.allChecked = checked;
    this.products.update(list => list.map(p => ({ ...p, checked })));
  }

  toggleOne(): void {
    this.allChecked = this.products().every(p => p.checked);
  }

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
