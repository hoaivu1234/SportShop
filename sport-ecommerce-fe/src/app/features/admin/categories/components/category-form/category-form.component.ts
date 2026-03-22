import {
  Component,
  Output,
  EventEmitter,
  inject,
  Input,
  Signal,
  signal,
  input,
  computed,
  OnInit,
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
  @Output() created = new EventEmitter<any>();
  mode = input<string>('create');
  category = input<any | null>(null);

  form: FormGroup;
  categories: any;
  titleModal = computed(() =>
    this.mode() === 'edit' ? 'Edit Category' : 'New Category',
  );

  filteredCategories = computed(() => {
    const categories = this.categories();
    const cat = this.category();

    if (this.mode() === 'edit' && cat) {
      return categories.filter((c: any) => c.id !== cat.id);
    }

    return categories;
  });

  constructor(
    private fb: FormBuilder,
    private categoryStoreService: CategoryStoreService,
    private categoryService: CategoryService,
    private toastService: ToastService,
  ) {
    this.categories = this.categoryStoreService.categories;

    this.form = this.fb.group({
      name: ['', [Validators.required]],
      parentId: [null],
    });

    effect(() => {
      const cat = this.category();
      const categories = this.categories();

      if (cat) {
        const found = categories.find((c: any) => c.name === cat.name);

        this.form.patchValue({
          name: cat.name,
          parentId: found?.parentId ?? null,
        });
      } else {
        this.form.reset({
          name: '',
          parentId: null,
        });
      }
    });
  }

  onSubmit() {
    if (this.form.valid) {
      if (this.mode() === 'create') {
        this.categoryService.createCategory(this.form.value).subscribe({
          next: (res) => {
            this.categoryStoreService.addCategory(res.data);
            this.toastService.success('Category created successfully!');
            this.onCancel();
          },
          error: (error) => {
            console.error('Error creating category:', error);
            this.toastService.error(
              'Failed to create category. Please try again.',
            );
          },
        });
      } else {
        this.categoryService
          .updateCategory(this.category().id, this.form.value)
          .subscribe({
            next: (res) => {
              this.categoryStoreService.updateCategory(res.data);
              this.toastService.success('Category updated successfully!');
              this.onCancel();
            },
            error: (error) => {
              console.error('Error updating category:', error);
              this.toastService.error(
                'Failed to update category. Please try again.',
              );
            },
          });
      }
    }
  }

  onCancel() {
    this.close.emit();
  }
}
