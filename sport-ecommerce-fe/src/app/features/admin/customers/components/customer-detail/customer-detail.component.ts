import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CustomerDetail } from '../../models/customer-admin.model';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-detail.component.html',
  styleUrl: './customer-detail.component.css',
})
export class CustomerDetailComponent {
  @Input() customer: CustomerDetail | null = null;
  @Input() isLoading = false;

  formatLtv(ltv: number): string {
    if (ltv == null) return '$0.00';
    return '$' + ltv.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }
}
