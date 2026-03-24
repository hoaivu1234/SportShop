import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ProductImagesComponent } from './components/product-images/product-images.component';
import { ProductInfoComponent } from './components/product-info/product-info.component';
import { RelatedProductsComponent } from './components/related-products/related-products.component';
import { ReviewListComponent } from './components/review-list/review-list.component';
import { ProductService } from '../admin/products/services/product.service';
import { ProductDetailResponse } from '../../models/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ProductImagesComponent,
    ProductInfoComponent,
    RelatedProductsComponent,
    ReviewListComponent,
  ],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css',
})
export class ProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);

  product = signal<ProductDetailResponse | null>(null);
  isLoading = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.isLoading.set(true);
      this.productService.getProductById(id).subscribe({
        next: (res) => this.product.set(res.data),
        complete: () => this.isLoading.set(false),
      });
    }
  }
}
