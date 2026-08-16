import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LoginService } from '../services/login-service';
import { NotificationsPanel } from './notifications-panel/notifications-panel';

@Component({
  selector: 'app-root',
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    NotificationsPanel,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  loginService = inject(LoginService);
}
