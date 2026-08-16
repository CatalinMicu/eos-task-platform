import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginData, LoginService } from '../../services/login-service';
import {
  RegisterData,
  RegisterService,
} from '../../services/register-service';

@Component({
  selector: 'app-login-component',
  imports: [FormsModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
})
export class LoginComponent implements OnInit {
  private loginService = inject(LoginService);
  private registerService = inject(RegisterService);
  private router = inject(Router);
  private changeDetectorRef = inject(ChangeDetectorRef);

  isRegisterMode = false;
  showPassword = false;
  confirmPassword = '';
  errorMessage = '';
  readonly today = new Date().toISOString().split('T')[0];

  loginData: LoginData = { email: '', password: '' };
  registerData: RegisterData = { username: '', birthDate: '', email: '', password: '' };

  ngOnInit(): void {
    if (this.loginService.isLoggedIn) {
      this.router.navigate(['/my-tasks']);
    }
  }

  showRegisterForm(): void {
    this.isRegisterMode = true;
    this.showPassword = false;
    this.confirmPassword = '';
    this.errorMessage = '';
  }

  showLoginForm(): void {
    this.isRegisterMode = false;
    this.showPassword = false;
    this.confirmPassword = '';
    this.errorMessage = '';
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  get passwordInputType(): string {
    if (this.showPassword) {
      return 'text';
    }

    return 'password';
  }

  get passwordToggleText(): string {
    if (this.showPassword) {
      return 'Hide';
    }

    return 'Show';
  }

  register(): void {
    this.errorMessage = '';

    if (this.registerData.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.registerService.register(this.registerData).subscribe({
      next: (response) => {
        this.loginService.saveToken(response);
        this.router.navigate(['/home']);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 409) {
          this.errorMessage = error.error;
        } else {
          this.errorMessage = 'Register failed.';
        }

        this.changeDetectorRef.detectChanges();
      },
    });
  }

  authenticate(): void {
    this.errorMessage = '';

    this.loginService.login(this.loginData).subscribe({
      next: () => {
        this.router.navigate(['/home']);
      },
      error: () => {
        this.errorMessage = 'Authentication failed.';
        this.changeDetectorRef.detectChanges();
      },
    });

  }

}
