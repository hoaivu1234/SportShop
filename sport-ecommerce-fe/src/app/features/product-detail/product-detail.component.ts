import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ProductImagesComponent } from './components/product-images/product-images.component';
import { ProductInfoComponent } from './components/product-info/product-info.component';
import { RelatedProductsComponent } from './components/related-products/related-products.component';
import { ReviewListComponent } from './components/review-list/review-list.component';
import { ProductService } from '../admin/products/services/product.service';
import { ProductDetailResponse } from '../../models/product.model';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
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
    const slug = this.route.snapshot.paramMap.get('slug') ?? '';

    this.isLoading.set(true);

    const request$ = id
      ? this.productService.getProductById(id)
      : this.productService.getProductBySlug(slug);

    request$
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (res) => this.product.set(res.data),
      });
  }
}
