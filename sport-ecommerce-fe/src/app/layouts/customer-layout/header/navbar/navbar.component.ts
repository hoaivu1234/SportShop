import { Component, ElementRef, HostListener, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../features/auth/services/auth.service';
import { CartStateService } from '../../../../features/cart/services/cart-state.service';
import { LoginPopupComponent } from './login-popup/login-popup.component';
import { UserDropdownComponent } from './user-dropdown/user-dropdown.component';

interface NavLink {
  label: string;
  path: string;
  queryParams?: Record<string, string>;
  highlight?: boolean;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LoginPopupComponent, UserDropdownComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  isScrolled       = signal(false);
  mobileMenuOpen   = signal(false);
  loginPopupOpen   = signal(false);
  userDropdownOpen = signal(false);
  searchOpen       = signal(false);
  searchQuery      = '';

  @ViewChild('searchInput') searchInputRef?: ElementRef<HTMLInputElement>;

  readonly authService = inject(AuthService);
  readonly cartState   = inject(CartStateService);
  private  readonly router = inject(Router);

  get cartCount(): number { return this.cartState.itemCount(); }

  readonly navLinks: NavLink[] = [
    { label: 'Football', path: '/products', queryParams: { category: 'football' } },
    { label: 'Fitness',  path: '/products', queryParams: { category: 'fitness'  } },
    { label: 'Swimming', path: '/products', queryParams: { category: 'swimming' } },
    { label: 'Sale',     path: '/products', queryParams: { sale: 'true'         }, highlight: true },
  ];

  @HostListener('window:scroll')
  onScroll() {
    this.isScrolled.set(window.scrollY > 40);
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    this.searchOpen.set(false);
  }

  toggleMobileMenu() {
    this.mobileMenuOpen.update((v) => !v);
  }

  toggleSearch() {
    this.searchOpen.update((v) => !v);
    if (this.searchOpen()) {
      // Focus the input on next tick after *ngIf renders it
      setTimeout(() => this.searchInputRef?.nativeElement.focus(), 0);
    }
  }

  submitSearch() {
    const q = this.searchQuery.trim();
    this.searchOpen.set(false);
    this.searchQuery = '';
    if (q) {
      this.router.navigate(['/products'], { queryParams: { keyword: q } });
    }
  }

  /** Returns true when the current URL matches the link's path + queryParams. */
  isLinkActive(link: NavLink): boolean {
    const url = this.router.url;
    if (!url.startsWith(link.path)) return false;
    if (!link.queryParams) return true;
    const currentParams = this.router.parseUrl(url).queryParams;
    return Object.entries(link.queryParams).every(([k, v]) => currentParams[k] === v);
  }

  toggleUserAction() {
    if (this.authService.loggedIn()) {
      this.userDropdownOpen.update((v) => !v);
      this.loginPopupOpen.set(false);
    } else {
      this.loginPopupOpen.update((v) => !v);
      this.userDropdownOpen.set(false);
    }
  }

  onLoginSuccess() {
    this.loginPopupOpen.set(false);
  }

  onLogout() {
    this.authService.logout();
    this.userDropdownOpen.set(false);
  }
}
