import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CustomerSummary } from '../../models/customer-admin.model';

@Component({
  selector: 'app-customer-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-table.component.html',
  styleUrl: './customer-table.component.css',
})
export class CustomerTableComponent {
  @Input() customers: CustomerSummary[] = [];
  @Input() totalElements = 0;
  @Input() totalPages    = 0;
  @Input() currentPage   = 0;
  @Input() isLoading     = false;

  @Output() customerSelected = new EventEmitter<CustomerSummary>();
  @Output() pageChanged      = new EventEmitter<number>();

  selectedId: number | null = null;

  /** Shows up to 5 page numbers centred around currentPage. */
  get pageNumbers(): number[] {
    const start = Math.max(0, this.currentPage - 2);
    const end   = Math.min(this.totalPages - 1, this.currentPage + 2);
    const pages: number[] = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  selectCustomer(customer: CustomerSummary): void {
    this.selectedId = customer.id;
    this.customerSelected.emit(customer);
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.pageChanged.emit(page);
    }
  }

  formatLtv(ltv: number): string {
    if (ltv == null) return '$0.00';
    return '$' + ltv.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  }
}
