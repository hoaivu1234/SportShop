import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, switchMap, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CustomerTableComponent } from './components/customer-table/customer-table.component';
import { CustomerDetailComponent } from './components/customer-detail/customer-detail.component';
import { CustomerAdminService, PageResponse } from './services/customer-admin.service';
import { CustomerDetail, CustomerSummary } from './models/customer-admin.model';

@Component({
  selector: 'app-admin-customers',
  standalone: true,
  imports: [CommonModule, FormsModule, CustomerTableComponent, CustomerDetailComponent],
  templateUrl: './admin-customers.component.html',
  styleUrl: './admin-customers.component.css',
})
export class AdminCustomersComponent implements OnInit {
  private readonly customerSvc    = inject(CustomerAdminService);
  private readonly destroyRef     = inject(DestroyRef);
  private readonly searchSubject$ = new Subject<string>();
  private readonly loadTrigger$   = new Subject<{ page: number; keyword: string }>();

  searchQuery = '';
  readonly pageSize = 10;

  customers        = signal<CustomerSummary[]>([]);
  totalElements    = signal(0);
  totalPages       = signal(0);
  currentPage      = signal(0);
  isLoading        = signal(false);
  selectedCustomer = signal<CustomerDetail | null>(null);
  detailLoading    = signal(false);

  statsCards = [
    { label: 'Total Customers',   value: '—', note: '', noteClass: 'positive' },
    { label: 'Active This Month', value: '—', note: '', noteClass: 'positive' },
    { label: 'Revenue (LTV)',     value: '—', note: '', noteClass: 'positive' },
    { label: 'Churn Rate',        value: '—', note: '', noteClass: 'positive' },
  ];

  ngOnInit(): void {
    // Single reactive pipeline — switchMap cancels any in-flight request on new trigger
    this.loadTrigger$.pipe(
      switchMap(({ page, keyword }) => {
        this.isLoading.set(true);
        return this.customerSvc.getCustomers({
          page,
          size: this.pageSize,
          keyword: keyword || undefined,
        });
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (res: PageResponse<CustomerSummary>) => {
        this.customers.set(res.content);
        this.totalElements.set(res.totalElements);
        this.totalPages.set(res.totalPages);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });

    // Debounce keyword changes → reset to page 0
    this.searchSubject$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(keyword => {
      this.currentPage.set(0);
      this.loadTrigger$.next({ page: 0, keyword });
    });

    // Initial load
    this.loadTrigger$.next({ page: 0, keyword: '' });
  }

  onSearchInput(keyword: string): void {
    this.searchSubject$.next(keyword);
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadTrigger$.next({ page, keyword: this.searchQuery });
  }

  onCustomerSelect(customer: CustomerSummary): void {
    this.detailLoading.set(true);
    this.customerSvc.getCustomerById(customer.id).subscribe({
      next: detail => {
        this.selectedCustomer.set(detail);
        this.detailLoading.set(false);
      },
      error: () => this.detailLoading.set(false),
    });
  }
}
