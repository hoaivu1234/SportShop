import {
  Component,
  Output,
  EventEmitter,
  inject,
  input,
  computed,
  effect,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { CategoryService } from '../../services/category.service';
import { CategoryStoreService } from '../../services/category-store.service';
import { ToastService } from '../../../../../core/services/toast.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './category-form.component.html',
  styleUrl: './category-form.component.css',
})
export class CategoryFormComponent {
  @Output() close = new EventEmitter<void>();
  /** Emitted after a successful create or update — parent should reload data */
  @Output() saved = new EventEmitter<void>();

  mode = input<string>('create');
  category = input<any | null>(null);

  form: FormGroup;

  titleModal = computed(() =>
    this.mode() === 'edit' ? 'Edit Category' : 'New Category',
  );

  /** All categories except the one being edited (prevents self-parent) */
  filteredCategories = computed(() => {
    const all = this.categoryStore.allFlat();
    const cat = this.category();
    return this.mode() === 'edit' && cat
      ? all.filter((c: any) => c.id !== cat.id)
      : all;
  });

  private readonly fb = inject(FormBuilder);
  private readonly categoryStore = inject(CategoryStoreService);
  private readonly categoryService = inject(CategoryService);
  private readonly toastService = inject(ToastService);

  constructor() {
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      parentId: [null],
    });

    effect(() => {
      const cat = this.category();
      if (cat) {
        this.form.patchValue({ name: cat.name, parentId: cat.parentId ?? null });
      } else {
        this.form.reset({ name: '', parentId: null });
      }
    });
  }

  onSubmit(): void {
    if (!this.form.valid) return;

    if (this.mode() === 'create') {
      this.categoryService.createCategory(this.form.value).subscribe({
        next: () => {
          this.toastService.success('Category created successfully!');
          this.saved.emit();
          this.close.emit();
        },
        error: () => {
          this.toastService.error('Failed to create category. Please try again.');
        },
      });
    } else {
      this.categoryService.updateCategory(this.category().id, this.form.value).subscribe({
        next: () => {
          this.toastService.success('Category updated successfully!');
          this.saved.emit();
          this.close.emit();
        },
        error: () => {
          this.toastService.error('Failed to update category. Please try again.');
        },
      });
    }
  }

  onCancel(): void {
    this.close.emit();
  }
}
