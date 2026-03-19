import { Component, HostListener, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../../features/auth/services/auth.service';
import { LoginPopupComponent } from './login-popup/login-popup.component';
import { UserDropdownComponent } from './user-dropdown/user-dropdown.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, LoginPopupComponent, UserDropdownComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  isScrolled = signal(false);
  mobileMenuOpen = signal(false);
  cartCount = signal(3);
  loginPopupOpen = signal(false);
  userDropdownOpen = signal(false);

  readonly authService = inject(AuthService);

  navLinks = [
    { label: 'Running', path: '/products/running' },
    { label: 'Sports', path: '/products/sports' },
    { label: 'Training', path: '/products/training' },
    { label: 'Sale', path: '/sale', highlight: true },
  ];

  @HostListener('window:scroll')
  onScroll() {
    this.isScrolled.set(window.scrollY > 40);
  }

  toggleMobileMenu() {
    this.mobileMenuOpen.update((v) => !v);
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
