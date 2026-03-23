import {
  Component,
  inject,
  signal,
  computed,
  OnInit,
  ElementRef,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ProductService } from '../../services/product.service';
import { CategoryService, CategoryFlatResponse } from '../../../../../features/admin/categories/services/category.service';
import { CloudinaryService } from '../../../../../core/services/cloudinary.service';
import { ToastService } from '../../../../../core/services/toast.service';
import { ProductImageRequest, ProductVariantRequest } from '../../../../../models/product.model';

interface ImagePreview {
  /** Present for new (local) files that still need Cloudinary upload */
  file: File | null;
  /** base64 data URL for local files, or Cloudinary URL for existing images */
  previewUrl: string;
  isMain: boolean;
  sortOrder: number;
}

interface VariantRow {
  size: string;
  color: string;
  sku: string;
  price: number | null;
  stock: number;
}

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css',
})
export class ProductFormComponent implements OnInit {
  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly cloudinaryService = inject(CloudinaryService);
  private readonly toast = inject(ToastService);

  // ── Mode (create vs edit) ─────────────────────────────────────────────────
  editProductId: number | null = null;
  readonly isEditMode = computed(() => this.editProductId !== null);
  readonly pageTitle = computed(() => this.isEditMode() ? 'Edit Product' : 'Create New Product');
  readonly submitLabel = computed(() => this.isEditMode() ? 'Save Changes' : 'Create Product');
  readonly submittingLabel = computed(() => this.isEditMode() ? 'Saving...' : 'Creating...');

  // ── Form ──────────────────────────────────────────────────────────────────
  form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    brand: ['', Validators.maxLength(100)],
    price: [null as number | null, [Validators.required, Validators.min(0.01)]],
    discountPrice: [null as number | null],
    categoryId: [null as number | null],
    status: ['ACTIVE', Validators.required],
  });

  // ── Images ────────────────────────────────────────────────────────────────
  images: ImagePreview[] = [];

  // ── Variants ──────────────────────────────────────────────────────────────
  sizeInput = '';
  colorInput = '';
  sizes: string[] = [];
  colors: string[] = [];
  variants: VariantRow[] = [];

  // ── Categories (for dropdown) ─────────────────────────────────────────────
  categories: CategoryFlatResponse[] = [];

  // ── UI State ──────────────────────────────────────────────────────────────
  isSubmitting = signal(false);
  isDragOver = signal(false);

  statusOptions = [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'INACTIVE', label: 'Inactive' },
    { value: 'DRAFT', label: 'Draft' },
  ];

  tips = [
    { icon: 'fa-check-circle', color: '#22c55e', text: 'Add at least 3 high-quality product images for best results.' },
    { icon: 'fa-check-circle', color: '#22c55e', text: 'Product name and price are required fields.' },
    { icon: 'fa-exclamation-triangle', color: '#f59e0b', text: 'Variants with no price or zero price will be skipped.' },
    { icon: 'fa-circle-info', color: '#3b82f6', text: 'Add sizes and/or colors, then click "Generate Combinations".' },
  ];

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    // Load categories for the dropdown
    this.categoryService.getAllCategories().subscribe({
      next: (res) => (this.categories = res.data),
      error: () => console.error('Failed to load categories'),
    });

    // Check if we are in edit mode
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.editProductId = Number(idParam);
      this.loadProductForEdit(this.editProductId);
    }
  }

  private loadProductForEdit(id: number): void {
    this.productService.getProductById(id).subscribe({
      next: (res) => {
        const p = res.data;
        this.form.patchValue({
          name: p.name,
          description: p.description ?? '',
          brand: p.brand ?? '',
          price: p.price,
          discountPrice: p.discountPrice ?? null,
          categoryId: p.category?.id ?? null,
          status: p.status,
        });

        // Populate images — existing images have a URL, no local file
        this.images = (p.images ?? []).map((img, i) => ({
          file: null,
          previewUrl: img.imageUrl,
          isMain: img.isMain,
          sortOrder: img.sortOrder ?? i,
        }));

        // Populate variants
        this.variants = (p.variants ?? []).map(v => ({
          size: v.size ?? '',
          color: v.color ?? '',
          sku: v.sku,
          price: v.price,
          stock: v.stock,
        }));

        // Rebuild size/color sets from existing variants
        this.sizes = [...new Set(this.variants.map(v => v.size).filter(Boolean))];
        this.colors = [...new Set(this.variants.map(v => v.color).filter(Boolean))];
      },
      error: () => {
        this.toast.error('Failed to load product. Please try again.');
        this.router.navigate(['/admin/products']);
      },
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  fc(name: string): AbstractControl {
    return this.form.get(name)!;
  }

  hasError(name: string, error: string): boolean {
    const c = this.fc(name);
    return c.invalid && c.touched && c.hasError(error);
  }

  // ── Image Handlers ────────────────────────────────────────────────────────

  openFilePicker(): void {
    this.fileInputRef.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(Array.from(input.files));
      input.value = '';
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver.set(true);
  }

  onDragLeave(): void {
    this.isDragOver.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver.set(false);
    const files = event.dataTransfer?.files;
    if (files) this.addFiles(Array.from(files));
  }

  private addFiles(files: File[]): void {
    const imageFiles = files.filter(f => f.type.startsWith('image/'));
    for (const file of imageFiles) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.images = [
          ...this.images,
          {
            file,
            previewUrl: e.target?.result as string,
            isMain: this.images.length === 0,
            sortOrder: this.images.length,
          },
        ];
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage(index: number): void {
    const wasMain = this.images[index].isMain;
    this.images = this.images.filter((_, i) => i !== index);
    this.images.forEach((img, i) => (img.sortOrder = i));
    if (wasMain && this.images.length > 0) {
      this.images[0].isMain = true;
    }
  }

  setMainImage(index: number): void {
    this.images.forEach((img, i) => (img.isMain = i === index));
  }

  moveImageUp(index: number): void {
    if (index === 0) return;
    const arr = [...this.images];
    [arr[index - 1], arr[index]] = [arr[index], arr[index - 1]];
    arr.forEach((img, i) => (img.sortOrder = i));
    this.images = arr;
  }

  moveImageDown(index: number): void {
    if (index === this.images.length - 1) return;
    const arr = [...this.images];
    [arr[index], arr[index + 1]] = [arr[index + 1], arr[index]];
    arr.forEach((img, i) => (img.sortOrder = i));
    this.images = arr;
  }

  // ── Variant Handlers ──────────────────────────────────────────────────────

  addSize(): void {
    const val = this.sizeInput.trim();
    if (val && !this.sizes.includes(val)) this.sizes = [...this.sizes, val];
    this.sizeInput = '';
  }

  onSizeKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      this.addSize();
    }
  }

  removeSize(size: string): void {
    this.sizes = this.sizes.filter(s => s !== size);
  }

  addColor(): void {
    const val = this.colorInput.trim();
    if (val && !this.colors.includes(val)) this.colors = [...this.colors, val];
    this.colorInput = '';
  }

  onColorKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      this.addColor();
    }
  }

  removeColor(color: string): void {
    this.colors = this.colors.filter(c => c !== color);
  }

  generateVariants(): void {
    if (this.sizes.length === 0 && this.colors.length === 0) return;

    const sizesToUse = this.sizes.length > 0 ? this.sizes : [''];
    const colorsToUse = this.colors.length > 0 ? this.colors : [''];
    const existingMap = new Map(this.variants.map(v => [`${v.size}|${v.color}`, v]));
    const newVariants: VariantRow[] = [];

    for (const size of sizesToUse) {
      for (const color of colorsToUse) {
        const key = `${size}|${color}`;
        if (existingMap.has(key)) {
          newVariants.push(existingMap.get(key)!);
        } else {
          const prefix = (this.form.value.name ?? 'SKU').slice(0, 8).toUpperCase().replace(/\s+/g, '-');
          const parts = [prefix, size, color].filter(Boolean).join('-');
          newVariants.push({
            size,
            color,
            sku: parts || `SKU-${Date.now()}`,
            price: this.form.value.price ?? null,
            stock: 0,
          });
        }
      }
    }
    this.variants = newVariants;
  }

  removeVariant(index: number): void {
    this.variants = this.variants.filter((_, i) => i !== index);
  }

  trackVariant(_: number, variant: VariantRow): string {
    return `${variant.size}|${variant.color}`;
  }

  // ── Submit ────────────────────────────────────────────────────────────────

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.warning('Please fix the form errors before submitting.');
      return;
    }

    this.isSubmitting.set(true);

    try {
      // Step 1: Upload any new (local) images to Cloudinary, reuse URLs for existing ones
      const uploadedImages: ProductImageRequest[] = [];
      for (let i = 0; i < this.images.length; i++) {
        const img = this.images[i];
        let imageUrl: string;

        if (img.file) {
          // New file — upload to Cloudinary
          const cloudResp = await firstValueFrom(this.cloudinaryService.uploadImage(img.file));
          imageUrl = cloudResp.secure_url;
        } else {
          // Existing image — URL is already in previewUrl
          imageUrl = img.previewUrl;
        }

        uploadedImages.push({ imageUrl, isMain: img.isMain, sortOrder: i });
      }

      // Step 2: Filter valid variants
      const validVariants: ProductVariantRequest[] = this.variants
        .filter(v => v.price != null && v.price > 0)
        .map(v => ({
          sku: v.sku,
          size: v.size || undefined,
          color: v.color || undefined,
          price: v.price!,
          stock: v.stock,
        }));

      // Step 3: Single API call — product + images + variants together
      const v = this.form.value;
      const request = {
        name: v.name!,
        description: v.description || undefined,
        brand: v.brand || undefined,
        price: v.price!,
        discountPrice: v.discountPrice ?? undefined,
        categoryId: v.categoryId ?? undefined,
        status: v.status ?? 'ACTIVE',
        images: uploadedImages,
        variants: validVariants,
      };

      if (this.isEditMode()) {
        await firstValueFrom(this.productService.updateProduct(this.editProductId!, request));
        this.toast.success('Product updated successfully!');
      } else {
        await firstValueFrom(this.productService.createProduct(request));
        this.toast.success('Product created successfully!');
      }

      this.router.navigate(['/admin/products']);
    } catch (err: any) {
      const msg = err?.error?.resolvedMessage ?? err?.error?.message ?? 'Failed to save product. Please try again.';
      this.toast.error(msg);
    } finally {
      this.isSubmitting.set(false);
    }
  }

  onDiscard(): void {
    this.router.navigate(['/admin/products']);
  }
}
