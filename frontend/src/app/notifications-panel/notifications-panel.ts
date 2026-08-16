import { DatePipe } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import {
  AppNotification,
  NotificationService,
} from '../../services/notification-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-notifications-panel',
  imports: [DatePipe],
  templateUrl: './notifications-panel.html',
  styleUrl: './notifications-panel.css',
})
export class NotificationsPanel implements OnInit {
  private notificationService = inject(NotificationService);
  private changeDetectorRef = inject(ChangeDetectorRef);
  private router = inject(Router);

  notifications: AppNotification[] = [];
  isOpen = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadNotifications();
  }

  get unreadCount(): number {
    let count = 0;

    for (const notification of this.notifications) {
      if (notification.isRead === 0) {
        count++;
      }
    }

    return count;
  }

  togglePanel(): void {
    this.isOpen = !this.isOpen;

    if (this.isOpen) {
      this.loadNotifications();
    }
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.errorMessage = '';
        this.changeDetectorRef.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Notifications could not be loaded.';
        this.changeDetectorRef.detectChanges();
      },
    });
  }

  openNotification(notification: AppNotification): void {
    if (notification.isRead === 1) {
      this.openTask(notification);
      return;
    }

    this.notificationService
      .markAsRead(notification.notificationId)
      .subscribe({
        next: (updatedNotification) => {
          for (let index = 0; index < this.notifications.length; index++) {
            if (
              this.notifications[index].notificationId ===
              updatedNotification.notificationId
            ) {
              this.notifications[index] = updatedNotification;
              break;
            }
          }

          this.changeDetectorRef.detectChanges();
          this.openTask(updatedNotification);
        },
      });
  }

  private openTask(notification: AppNotification): void {
    this.isOpen = false;

    if (notification.taskId !== null) {
      this.router.navigate(['/my-tasks'], {
        queryParams: { taskId: notification.taskId },
      });
    }
  }
}
