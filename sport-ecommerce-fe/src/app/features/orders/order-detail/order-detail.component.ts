import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { OrderService } from '../services/order.service';
import { OrderResponse } from '../../../models/order.model';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.css',
})
export class OrderDetailComponent implements OnInit {
  private readonly route      = inject(ActivatedRoute);
  private readonly orderSvc   = inject(OrderService);
  private readonly destroyRef = inject(DestroyRef);

  readonly order     = signal<OrderResponse | null>(null);
  readonly isLoading = signal(true);
  readonly error     = signal<string | null>(null);

  private orderId = 0;

  readonly skeletonItems = [1, 2, 3];

  ngOnInit(): void {
    this.orderId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.orderId) {
      this.error.set('Invalid order ID.');
      this.isLoading.set(false);
      return;
    }
    this.loadOrder();
  }

  loadOrder(): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.orderSvc.getOrderById(this.orderId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.order.set(res.data);
          this.isLoading.set(false);
        },
        error: () => {
          this.error.set('Order not found or you do not have access to it.');
          this.isLoading.set(false);
        },
      });
  }

  statusClass(status: string): string {
    return ({
      PENDING:   'badge--warning',
      CONFIRMED: 'badge--info',
      SHIPPED:   'badge--primary',
      COMPLETED: 'badge--success',
      CANCELLED: 'badge--danger',
    } as Record<string, string>)[status] ?? 'badge--default';
  }

  statusLabel(status: string): string {
    return ({
      PENDING:   'Pending',
      CONFIRMED: 'Confirmed',
      SHIPPED:   'Shipped',
      COMPLETED: 'Delivered',
      CANCELLED: 'Cancelled',
    } as Record<string, string>)[status] ?? status;
  }

  get fullAddress(): string {
    const a = this.order()?.shippingAddress;
    if (!a) return '';
    return [a.addressLine, a.ward, a.district, a.province]
      .filter(Boolean)
      .join(', ');
  }
}
