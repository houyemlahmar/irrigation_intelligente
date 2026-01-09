import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Notification {
  id: number;
  type: 'info' | 'warning' | 'success' | 'error';
  title: string;
  message: string;
  timestamp: Date;
  read: boolean;
  showInToast: boolean; // Pour affichage dans le popup toast
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications$ = new BehaviorSubject<Notification[]>([]);
  private notificationId = 0;

  getNotifications(): Observable<Notification[]> {
    return this.notifications$.asObservable();
  }

  addNotification(type: 'info' | 'warning' | 'success' | 'error', title: string, message: string) {
    const notification: Notification = {
      id: ++this.notificationId,
      type,
      title,
      message,
      timestamp: new Date(),
      read: false,
      showInToast: true
    };

    const current = this.notifications$.value;
    this.notifications$.next([notification, ...current]);
    
    // Auto-fermeture du toast après 10 secondes (mais garde la notification dans la liste)
    setTimeout(() => {
      this.hideToast(notification.id);
    }, 10000);
  }

  removeNotification(id: number) {
    const current = this.notifications$.value;
    this.notifications$.next(current.filter(n => n.id !== id));
  }

  hideToast(id: number) {
    const current = this.notifications$.value;
    const notification = current.find(n => n.id === id);
    if (notification) {
      notification.showInToast = false;
      this.notifications$.next([...current]);
    }
  }

  markAsRead(id: number) {
    const current = this.notifications$.value;
    const notification = current.find(n => n.id === id);
    if (notification) {
      notification.read = true;
      this.notifications$.next([...current]);
    }
  }

  clearAll() {
    this.notifications$.next([]);
  }

  getUnreadCount(): number {
    return this.notifications$.value.filter(n => !n.read).length;
  }
}
