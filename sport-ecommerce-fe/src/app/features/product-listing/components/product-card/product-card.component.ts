import { Component, Input } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

export interface ListingProduct {
  id: number;
  name: string;
  price: number;
  badge?: 'Sale' | 'New';
  mainImageUrl?: string;
  totalStock?: number;
}

@Component({
  selector: 'app-listing-product-card',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.css',
})
export class ListingProductCardComponent {
  @Input() product!: ListingProduct;

  readonly fallbackImage = 'assets/images/placeholder.png';

  get imageUrl(): string {
    return this.product.mainImageUrl || this.fallbackImage;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallbackImage;
  }
}
