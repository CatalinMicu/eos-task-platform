import { Injectable, inject } from '@angular/core';
import { LoginService } from './login-service';
import { Router, CanActivate } from '@angular/router';


@Injectable({
  providedIn: 'root',
})
export class AdminGuard implements CanActivate {
  private loginService = inject(LoginService);
  private router = inject(Router);

  canActivate(): boolean {
    if(this.loginService.isAdmin) {
      return true;
    }

    this.router.navigate(['/my-tasks']);
    return false;
  }
}
