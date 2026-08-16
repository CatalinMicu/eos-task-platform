import { Injectable, inject } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { LoginService } from './login-service';

@Injectable({
  providedIn: 'root',
})
export class LoggedInGuardService implements CanActivate {
  private router = inject(Router);
  private loginService = inject(LoginService);

  canActivate(): boolean {
    if (this.loginService.isLoggedIn) {
      return true;
    }

    this.router.navigate(['/login']);
    return false;
  }
}
