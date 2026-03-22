import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryFormComponent } from './components/category-form/category-form.component';
import { CategoryTableComponent } from './components/category-table/category-table.component';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, CategoryFormComponent, CategoryTableComponent],
  templateUrl: './category-management.component.html',
  styleUrl: './category-management.component.css'
})
export class CategoryManagementComponent {
  showModal = false;
  mode: string = 'create';

  treeItems = [
    {
      name: 'Footwear', icon: 'fa-folder', products: 248, expanded: true,
      children: ['Running', 'Basketball', 'Lifestyle']
    },
    {
      name: 'Apparel', icon: 'fa-folder', products: 412, expanded: true,
      children: ['Tops', 'Bottoms', 'Socks']
    },
    {
      name: 'Equipment', icon: 'fa-folder', products: 184, expanded: false,
      children: []
    },
    {
      name: 'Wellness', icon: 'fa-folder', products: 76, expanded: false,
      children: []
    },
  ];

  openModal() { this.showModal = true; }
  closeModal() { this.showModal = false; }
  toggleTree(item: any) { item.expanded = !item.expanded; }
}
