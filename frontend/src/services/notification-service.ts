import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface AppNotification {
  notificationId: number;
  taskId: number | null;
  message: string;
  isRead: number;
  creationDate: string;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private http = inject(HttpClient);

  getNotifications() {
    return this.http.get<AppNotification[]>(
      'http://localhost:8080/notifications',
    );
  }

  markAsRead(notificationId: number) {
    return this.http.patch<AppNotification>(
      `http://localhost:8080/notifications/${notificationId}/read`,
      null,
    );
  }
}
