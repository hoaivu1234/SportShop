import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FilterSidebarComponent } from './components/filter-sidebar/filter-sidebar.component';
import { SortBarComponent } from './components/sort-bar/sort-bar.component';
import { ProductGridComponent } from './components/product-grid/product-grid.component';
import { ListingProduct } from './components/product-card/product-card.component';
import { ProductService } from '../admin/products/services/product.service';

@Component({
  selector: 'app-product-listing',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    FilterSidebarComponent,
    SortBarComponent,
    ProductGridComponent,
  ],
  templateUrl: './product-listing.component.html',
  styleUrl: './product-listing.component.css',
})
export class ProductListingComponent implements OnInit {
  private readonly productService = inject(ProductService);

  products = signal<ListingProduct[]>([]);
  totalElements = signal(0);
  isLoading = signal(false);

  page = signal(0);
  readonly pageSize = 12;
  viewMode: 'grid' | 'list' = 'grid';
  email = '';

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
      status: 'ACTIVE',
    }).subscribe({
      next: (res) => {
        this.products.set(res.data.content.map(p => ({
          id: p.id,
          name: p.name,
          price: p.price,
          mainImageUrl: p.mainImageUrl,
          totalStock: p.totalStock,
        })));
        this.totalElements.set(res.data.totalElements);
      },
      complete: () => this.isLoading.set(false),
    });
  }

  onPageChange(page: number): void {
    this.page.set(page);
    this.loadProducts();
  }

  onViewModeChange(mode: 'grid' | 'list'): void {
    this.viewMode = mode;
  }

  subscribeNewsletter(): void {
    this.email = '';
  }
}
