import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layouts/customer-layout/customer-layout.component').then(
        (m) => m.CustomerLayoutComponent
      ),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/product-listing/product-listing.component').then(
            (m) => m.ProductListingComponent
          ),
      },
      {
        path: 'products/:id',
        loadComponent: () =>
          import('./features/product-detail/product-detail.component').then(
            (m) => m.ProductDetailComponent
          ),
      },
      {
        path: 'search',
        loadComponent: () =>
          import('./features/search/search-results.component').then(
            (m) => m.SearchResultsComponent
          ),
      },
      {
        path: 'cart',
        loadComponent: () =>
          import('./features/cart/cart.component').then((m) => m.CartComponent),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/profile.component').then(
            (m) => m.ProfileComponent
          ),
      },
    ],
  },
  {
    path: 'checkout',
    loadComponent: () =>
      import('./features/checkout/checkout.component').then(
        (m) => m.CheckoutComponent
      ),
  },
  {
    path: 'auth',
    loadComponent: () =>
      import('./features/auth/auth.component').then((m) => m.AuthComponent),
  },

  // ── Admin layout ───────────────────────────────────────────────────────────
  {
    path: 'admin',
    loadComponent: () =>
      import('./layouts/admin-layout/admin-layout.component').then(
        (m) => m.AdminLayoutComponent
      ),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/admin/products/admin-products.component').then(
            (m) => m.AdminProductsComponent
          ),
      },
      {
        path: 'products/new',
        loadComponent: () =>
          import('./features/admin/products/components/product-form/product-form.component').then(
            (m) => m.ProductFormComponent
          ),
      },
      {
        path: 'products/create',
        loadComponent: () =>
          import('./features/admin/products/components/product-form/product-form.component').then(
            (m) => m.ProductFormComponent
          ),
      },
      {
        path: 'products/:id/edit',
        loadComponent: () =>
          import('./features/admin/products/components/product-form/product-form.component').then(
            (m) => m.ProductFormComponent
          ),
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./features/admin/categories/category-management.component').then(
            (m) => m.CategoryManagementComponent
          ),
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./features/admin/orders/admin-orders.component').then(
            (m) => m.AdminOrdersComponent
          ),
      },
      {
        path: 'customers',
        loadComponent: () =>
          import('./features/admin/customers/admin-customers.component').then(
            (m) => m.AdminCustomersComponent
          ),
      },
      {
        path: 'inventory',
        loadComponent: () =>
          import('./features/admin/inventory/admin-inventory.component').then(
            (m) => m.AdminInventoryComponent
          ),
      },
      {
        path: 'reviews',
        loadComponent: () =>
          import('./features/admin/reviews/admin-reviews.component').then(
            (m) => m.AdminReviewsComponent
          ),
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('./features/admin/reports/admin-reports.component').then(
            (m) => m.AdminReportsComponent
          ),
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/admin/settings/admin-settings.component').then(
            (m) => m.AdminSettingsComponent
          ),
      },
    ],
  },

  // ── Fallback ───────────────────────────────────────────────────────────────
  { path: '**', redirectTo: '' },
];
