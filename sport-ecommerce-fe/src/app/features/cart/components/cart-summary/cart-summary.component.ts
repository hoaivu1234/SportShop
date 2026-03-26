import { Component, Input, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CouponStateService } from '../../services/coupon-state.service';

@Component({
  selector: 'app-cart-summary',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, FormsModule, RouterLink],
  templateUrl: './cart-summary.component.html',
  styleUrl: './cart-summary.component.css',
})
export class CartSummaryComponent {
  @Input() subtotal = 0;

  readonly couponState = inject(CouponStateService);

  couponCode = '';

  get shipping(): number {
    return this.subtotal >= 100 ? 0 : 9.99;
  }

  get tax(): number {
    return this.subtotal * 0.219;
  }

  get total(): number {
    return this.subtotal + this.shipping + this.tax - this.couponState.discountAmount();
  }

  applyPromo(): void {
    this.couponState.apply(this.couponCode, this.subtotal);
  }

  removePromo(): void {
    this.couponState.remove();
    this.couponCode = '';
  }
}
