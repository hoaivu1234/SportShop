import { Component, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductTableComponent } from './components/product-table/product-table.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, ProductTableComponent],
  templateUrl: './admin-products.component.html',
  styleUrl: './admin-products.component.css'
})
export class AdminProductsComponent {
  router = inject(Router);

  @ViewChild(ProductTableComponent) private productTable?: ProductTableComponent;

  miniStats = [
    { label: 'Total Products', value: '1,248', note: '+12 this month', noteClass: 'positive' },
    { label: 'Active Listings', value: '1,102', note: '92% of total', noteClass: 'info' },
    { label: 'Low Stock Alerts', value: '18', note: 'Requires action', noteClass: 'warning' },
    { label: 'Out of Stock', value: '4', note: 'Decreased from 7', noteClass: 'positive' },
  ];

  openModal(): void {
    this.router.navigate(['/admin/products/create']);
  }

  /** Delegates to the table component — it owns the active filters and export state. */
  exportCsv(): void {
    this.productTable?.exportCsv();
  }

  /** Exposes the table's exporting signal to the template for the button loading state. */
  get isExporting(): boolean {
    return this.productTable?.isExporting() ?? false;
  }
}
