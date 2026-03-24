import { Component, Input } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SizeSelectorComponent } from '../size-selector/size-selector.component';
import { ProductDetailResponse, VariantResponse } from '../../../../models/product.model';

@Component({
  selector: 'app-product-info',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink, SizeSelectorComponent],
  templateUrl: './product-info.component.html',
  styleUrl: './product-info.component.css',
})
export class ProductInfoComponent {
  @Input() product: ProductDetailResponse | null = null;

  selectedVariant: VariantResponse | null = null;
  selectedSize = '';
  quantity = 1;
  isWishlisted = false;

  get uniqueSizes(): string[] {
    if (!this.product?.variants) return [];
    return [...new Set(this.product.variants.filter(v => v.size).map(v => v.size!))];
  }

  get uniqueColors(): string[] {
    if (!this.product?.variants) return [];
    return [...new Set(this.product.variants.filter(v => v.color).map(v => v.color!))];
  }

  get selectedColor(): string {
    return this.selectedVariant?.color ?? this.uniqueColors[0] ?? '';
  }

  get currentPrice(): number {
    return this.selectedVariant?.price ?? this.product?.price ?? 0;
  }

  get firstSku(): string {
    return this.product?.variants?.[0]?.sku ?? '';
  }

  onSizeSelected(size: string): void {
    this.selectedSize = size;
    this.selectedVariant = this.product?.variants.find(v => v.size === size) ?? null;
  }

  onColorSelected(color: string): void {
    this.selectedVariant = this.product?.variants.find(v => v.color === color) ?? null;
  }

  decreaseQty(): void {
    if (this.quantity > 1) this.quantity--;
  }

  increaseQty(): void {
    this.quantity++;
  }

  toggleWishlist(): void {
    this.isWishlisted = !this.isWishlisted;
  }
}
