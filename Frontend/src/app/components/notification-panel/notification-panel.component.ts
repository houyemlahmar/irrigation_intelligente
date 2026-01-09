import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notification-panel',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notification-panel" *ngIf="notifications.length > 0">
      <div *ngFor="let notification of notifications" 
           class="notification-item"
           [ngClass]="'notification-' + notification.type"
           [@slideIn]>
        <div class="notification-header">
          <span class="notification-icon">
            <ng-container [ngSwitch]="notification.type">
              <span *ngSwitchCase="'success'">✅</span>
              <span *ngSwitchCase="'warning'">⚠️</span>
              <span *ngSwitchCase="'error'">❌</span>
              <span *ngSwitchDefault>ℹ️</span>
            </ng-container>
          </span>
          <h4>{{ notification.title }}</h4>
          <button class="close-notification" (click)="removeNotification(notification.id)">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <p class="notification-message">{{ notification.message }}</p>
        <span class="notification-time">{{ getTimeAgo(notification.timestamp) }}</span>
      </div>
    </div>
  `,
  styles: [`
    .notification-panel {
      position: fixed;
      top: 80px;
      right: 20px;
      z-index: 2000;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      max-width: 400px;
    }

    .notification-item {
      background: white;
      border-radius: 12px;
      padding: 1rem 1.25rem;
      box-shadow: 0 4px 16px rgba(0,0,0,0.15);
      border-left: 4px solid;
      animation: slideIn 0.3s ease-out;
    }

    .notification-success { border-left-color: #4CAF50; }
    .notification-warning { border-left-color: #FF9800; }
    .notification-error { border-left-color: #f44336; }
    .notification-info { border-left-color: #2196F3; }

    .notification-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 0.5rem;
    }

    .notification-icon {
      font-size: 1.5rem;
      line-height: 1;
    }

    .notification-header h4 {
      margin: 0;
      font-size: 1rem;
      font-weight: 600;
      flex: 1;
      color: #1a1a1a;
    }

    .close-notification {
      background: none;
      border: none;
      cursor: pointer;
      color: #666;
      padding: 0.25rem;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      transition: all 0.2s ease;

      &:hover {
        background: #f5f5f5;
      }
    }

    .notification-message {
      margin: 0 0 0.5rem 2.25rem;
      color: #555;
      font-size: 0.9rem;
      line-height: 1.4;
    }

    .notification-time {
      display: block;
      margin-left: 2.25rem;
      font-size: 0.75rem;
      color: #999;
    }

    @keyframes slideIn {
      from {
        transform: translateX(400px);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
  `]
})
export class NotificationPanelComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  private subscription?: Subscription;

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.subscription = this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        // Filtrer uniquement les notifications qui doivent être affichées en toast
        this.notifications = notifications.filter(n => n.showInToast);
      }
    });
  }

  ngOnDestroy() {
    this.subscription?.unsubscribe();
  }

  removeNotification(id: number) {
    // Cacher le toast mais garder la notification dans la liste du header
    this.notificationService.hideToast(id);
  }

  getTimeAgo(timestamp: Date): string {
    const seconds = Math.floor((new Date().getTime() - new Date(timestamp).getTime()) / 1000);
    
    if (seconds < 60) return 'À l\'instant';
    if (seconds < 3600) return `Il y a ${Math.floor(seconds / 60)} min`;
    if (seconds < 86400) return `Il y a ${Math.floor(seconds / 3600)}h`;
    return `Il y a ${Math.floor(seconds / 86400)}j`;
  }
}
