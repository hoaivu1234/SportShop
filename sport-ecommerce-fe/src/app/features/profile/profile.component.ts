import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderHistoryComponent } from './components/order-history/order-history.component';
import { ProfileInfoComponent } from './components/profile-info/profile-info.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { StorageService } from '../../core/services/storage/storage.service';

type ProfileTab = 'orders' | 'saved' | 'address' | 'settings' | 'security';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, OrderHistoryComponent, ProfileInfoComponent, ChangePasswordComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent implements OnInit{
  private readonly storageService = inject(StorageService)

  activeTab: ProfileTab = 'orders';

  navItems: { key: ProfileTab; label: string; icon: string }[] = [
    { key: 'orders', label: 'My Orders', icon: 'fa-box' },
    { key: 'saved', label: 'Saved Items', icon: 'fa-heart' },
    { key: 'address', label: 'Address Book', icon: 'fa-map-marker-alt' },
    { key: 'settings', label: 'Profile Settings', icon: 'fa-user-cog' },
    { key: 'security', label: 'Security', icon: 'fa-shield-alt' },
  ];

  setTab(tab: ProfileTab) {
    this.activeTab = tab;
  }

  user: any;

  ngOnInit(): void {
    this.user = this.storageService.getUserInfo();
    console.log(this.user)
  }
}
