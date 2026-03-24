import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImageResponse } from '../../../../models/product.model';

@Component({
  selector: 'app-product-images',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-images.component.html',
  styleUrl: './product-images.component.css',
})
export class ProductImagesComponent implements OnChanges {
  @Input() images: ImageResponse[] = [];

  mainImage = '';
  thumbnails: string[] = [];
  selectedIndex = 0;

  readonly fallbackImage = 'assets/images/placeholder.png';

  ngOnChanges(): void {
    if (this.images.length > 0) {
      const main = this.images.find(i => i.isMain) ?? this.images[0];
      this.thumbnails = this.images.map(i => i.imageUrl);
      this.selectedIndex = this.images.indexOf(main);
      this.mainImage = main.imageUrl;
    } else {
      this.mainImage = this.fallbackImage;
      this.thumbnails = [];
      this.selectedIndex = 0;
    }
  }

  selectThumbnail(index: number): void {
    this.selectedIndex = index;
    this.mainImage = this.thumbnails[index];
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallbackImage;
  }
}
