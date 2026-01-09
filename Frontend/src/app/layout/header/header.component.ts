import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit, OnDestroy {
  menuOpen = false;
  notificationsOpen = false;
  notifications: Notification[] = [];
  unreadCount = 0;
  private subscription?: Subscription;

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.subscription = this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.unreadCount = notifications.filter(n => !n.read).length;
      }
    });
  }

  ngOnDestroy() {
    this.subscription?.unsubscribe();
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  toggleNotifications() {
    this.notificationsOpen = !this.notificationsOpen;
    if (this.notificationsOpen) {
      // Marquer toutes comme lues après ouverture
      this.notifications.forEach(n => {
        if (!n.read) {
          this.notificationService.markAsRead(n.id);
        }
      });
    }
  }

  removeNotification(id: number) {
    this.notificationService.removeNotification(id);
  }

  clearAllNotifications() {
    this.notificationService.clearAll();
  }

  getTimeAgo(timestamp: Date): string {
    const seconds = Math.floor((new Date().getTime() - new Date(timestamp).getTime()) / 1000);
    
    if (seconds < 60) return 'À l\'instant';
    if (seconds < 3600) return `Il y a ${Math.floor(seconds / 60)} min`;
    if (seconds < 86400) return `Il y a ${Math.floor(seconds / 3600)}h`;
    return `Il y a ${Math.floor(seconds / 86400)}j`;
  }
}
