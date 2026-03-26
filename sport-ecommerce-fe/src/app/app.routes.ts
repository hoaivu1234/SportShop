import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth/auth.guard';
import { roleGuard } from './core/guards/admin/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layouts/customer-layout/customer-layout.component').then(
        (m) => m.CustomerLayoutComponent,
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
            (m) => m.ProductListingComponent,
          ),
      },
      {
        path: 'products/:id',
        loadComponent: () =>
          import('./features/product-detail/product-detail.component').then(
            (m) => m.ProductDetailComponent,
          ),
      },
      {
        path: 'search',
        loadComponent: () =>
          import('./features/search/search-results.component').then(
            (m) => m.SearchResultsComponent,
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
            (m) => m.ProfileComponent,
          ),
        canActivate: [authGuard],
      },
      {
        path: 'wishlist',
        loadComponent: () =>
          import('./features/wishlist/wishlist.component').then(
            (m) => m.WishlistComponent,
          ),
        canActivate: [authGuard],
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./features/orders/order-list/order-list.component').then(
            (m) => m.OrderListComponent,
          ),
        canActivate: [authGuard],
      },
      {
        path: 'orders/:id',
        loadComponent: () =>
          import('./features/orders/order-detail/order-detail.component').then(
            (m) => m.OrderDetailComponent,
          ),
        canActivate: [authGuard],
      },
    ],
  },
  {
    path: 'order-confirmation/:orderNumber',
    loadComponent: () =>
      import('./features/orders/order-confirmation/order-confirmation.component').then(
        (m) => m.OrderConfirmationComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'checkout',
    loadComponent: () =>
      import('./features/checkout/checkout.component').then(
        (m) => m.CheckoutComponent,
      ),
      canActivate: [authGuard]
  },
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./features/auth/auth.component').then((m) => m.AuthComponent),
  },

  // ── Admin layout ───────────────────────────────────────────────────────────
  {
    path: 'admin',
    loadComponent: () =>
      import('./layouts/admin-layout/admin-layout.component').then(
        (m) => m.AdminLayoutComponent,
      ),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent,
          ),
        data: { title: 'Dashboard Overview' }
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/admin/products/admin-products.component').then(
            (m) => m.AdminProductsComponent,
          ),
        data: { title: 'Product Management' }
      },
      {
        path: 'products/new',
        loadComponent: () =>
          import('./features/admin/products/components/product-form/product-form.component').then(
            (m) => m.ProductFormComponent,
          ),
        data: { title: 'Create Product' }
      },
      {
        path: 'products/create',
        loadComponent: () =>
          import('./features/admin/products/components/product-form/product-form.component').then(
            (m) => m.ProductFormComponent,
          ),
        data: { title: 'Create Product' }
      },
      {
        path: 'products/:id/edit',
        loadComponent: () =>
          import('./features/admin/products/components/product-form/product-form.component').then(
            (m) => m.ProductFormComponent,
          ),
        data: { title: 'Edit Product' }
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./features/admin/categories/category-management.component').then(
            (m) => m.CategoryManagementComponent,
          ),
        data: { title: 'Category Management' }
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./features/admin/orders/admin-orders.component').then(
            (m) => m.AdminOrdersComponent,
          ),
        data: { title: 'Order Management' }
      },
      {
        path: 'customers',
        loadComponent: () =>
          import('./features/admin/customers/admin-customers.component').then(
            (m) => m.AdminCustomersComponent,
          ),
        data: { title: 'Customer Management' }
      },
      {
        path: 'inventory',
        loadComponent: () =>
          import('./features/admin/inventory/admin-inventory.component').then(
            (m) => m.AdminInventoryComponent,
          ),
        data: { title: 'Inventory Management' }
      },
      {
        path: 'reviews',
        loadComponent: () =>
          import('./features/admin/reviews/admin-reviews.component').then(
            (m) => m.AdminReviewsComponent,
          ),
        data: { title: 'Review Management' }
      },
      {
        path: 'coupons',
        loadComponent: () =>
          import('./features/admin/coupons/admin-coupons.component').then(
            (m) => m.AdminCouponsComponent,
          ),
        data: { title: 'Coupon Management' }
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('./features/admin/reports/admin-reports.component').then(
            (m) => m.AdminReportsComponent,
          ),
        data: { title: 'Analytics' }
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/admin/settings/admin-settings.component').then(
            (m) => m.AdminSettingsComponent,
          ),
        data: { title: 'Settings' }
      },
    ],
    data: { roles: ['ROLE_ADMIN'] },
    canActivate: [authGuard, roleGuard],
  },

  // ── Error pages ────────────────────────────────────────────────────────────
  {
    path: 'forbidden',
    loadComponent: () =>
      import('./features/errors/forbidden/forbidden.component').then(
        (m) => m.ForbiddenComponent,
      ),
  },

  // ── Fallback ───────────────────────────────────────────────────────────────
  { path: '**', redirectTo: '' },
];
