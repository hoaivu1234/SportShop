import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../features/auth/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  navItems = [
    { label: 'Dashboard', icon: 'fa-gauge', route: '/admin/dashboard' },
    { label: 'Products', icon: 'fa-box', route: '/admin/products' },
    { label: 'Categories', icon: 'fa-tags', route: '/admin/categories' },
    { label: 'Orders', icon: 'fa-receipt', route: '/admin/orders' },
    { label: 'Customers', icon: 'fa-users', route: '/admin/customers' },
    { label: 'Inventory', icon: 'fa-warehouse', route: '/admin/inventory' },
    { label: 'Reviews', icon: 'fa-star', route: '/admin/reviews' },
    { label: 'Analytics', icon: 'fa-chart-line', route: '/admin/analytics' },
    { label: 'Settings', icon: 'fa-gear', route: '/admin/settings' },
  ];

  signOut(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
