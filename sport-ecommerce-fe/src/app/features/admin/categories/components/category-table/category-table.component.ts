import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryService } from '../../services/category.service';
import { CategoryStoreService } from '../../services/category-store.service';
import { CategoryFormComponent } from '../category-form/category-form.component';

@Component({
  selector: 'app-category-table',
  standalone: true,
  imports: [CommonModule, CategoryFormComponent],
  templateUrl: './category-table.component.html',
  styleUrl: './category-table.component.css',
})
export class CategoryTableComponent implements OnInit {
  categoryServices = inject(CategoryService);
  categoryStore = inject(CategoryStoreService);
  mode: string = 'edit';
  showModal = false;
  selectedCategory: any = null;

  categories = this.categoryStore.categories;

  ngOnInit(): void {
    this.categoryServices.getFlatCategories().subscribe({
      next: (response) => {
        this.categoryStore.setCategories(response.data.content);
      },
      error: (error) => {
        console.error('Error fetching categories:', error);
      },
    });
  }

  openModal(cat: any) { 
    this.showModal = true; 
    this.selectedCategory = cat;
  }
  closeModal() { this.showModal = false; }
}
