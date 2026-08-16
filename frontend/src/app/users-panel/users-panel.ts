import { ChangeDetectorRef, Component } from '@angular/core';
import { OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { User, UserService } from '../../services/user-service';
import { LoginService } from '../../services/login-service';
import { ConfirmDialog } from '../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-users-panel',
  imports: [FormsModule, ConfirmDialog],
  templateUrl: './users-panel.html',
  styleUrl: './users-panel.css',
})
export class UsersPanel implements OnInit {
  private userService = inject(UserService);
  private loginService = inject(LoginService);
  private changeDetectorRef = inject(ChangeDetectorRef);

  users: User[] = [];
  userToDelete: User | null = null;
  currentPage = 1;
  totalPages = 1;
  totalUsers = 0;
  readonly pageSize = 8;
  sortBy = 'username';
  sortDirection = 'asc';
  searchText = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  changeUserRole(userId: number, newRole: 'USER' | 'ADMIN'): void {
    this.userService.changeUserRole(userId, newRole).subscribe(() => {
      this.loadUsers();
    });
  }
  
  requestUserDelete(user: User): void {
    this.userToDelete = user;
  }

  confirmUserDelete(user: User): void {
    const userId = user.userId;

    this.userService.deleteUser(userId).subscribe(() => {
      this.userToDelete = null;
      this.loadUsers();
    });
  }

  changeUserSorting(): void {
    this.currentPage = 1;
    this.loadUsers();
  }

  searchUsers(): void {
    this.currentPage = 1;
    this.loadUsers();
  }

  clearSearch(): void {
    this.searchText = '';
    this.currentPage = 1;
    this.loadUsers();
  }

  isCurrentUser(userId: number): boolean {
    const currentUser = this.loginService.currentUser();
    if (currentUser === null) {
      return false;
    }

    return currentUser.userId === userId;
  }

  canChangeRole(user: User): boolean {
    if (user.roleName === 'SUPER_ADMIN') {
      return false;
    }

    if (user.roleName === 'ADMIN') {
      return this.loginService.isSuperAdmin;
    }

    return true;
  }

  canDeleteUser(user: User): boolean {
    if (this.isCurrentUser(user.userId)) {
      return false;
    }
    if (user.roleName === 'SUPER_ADMIN') {
      return false;
    }
    if (user.roleName === 'ADMIN') {
      return this.loginService.isSuperAdmin;
    }

    return true;
  }

  getRoleTitle(user: User): string {
    if (user.roleName === 'SUPER_ADMIN') {
      return 'Super administrator roles cannot be changed';
    }
    if (user.roleName === 'ADMIN' && !this.loginService.isSuperAdmin) {
      return 'Only a super administrator can change this role';
    }

    return 'Change user role';
  }

  getDeleteTitle(user: User): string {
    if (this.isCurrentUser(user.userId)) {
      return 'You cannot delete your own account';
    }
    if (user.roleName === 'SUPER_ADMIN') {
      return 'Super administrator accounts cannot be deleted';
    }
    if (user.roleName === 'ADMIN' && !this.loginService.isSuperAdmin) {
      return 'Only a super administrator can delete this account';
    }

    return 'Delete user';
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  private loadUsers(): void {
    this.userService
      .getUserPage(
        this.currentPage - 1,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
        this.searchText,
      )
      .subscribe((response) => {
        this.totalPages = Math.max(1, response.totalPages);
        this.totalUsers = response.totalElements;

        if (this.currentPage > this.totalPages) {
          this.currentPage = this.totalPages;
          this.loadUsers();
          return;
        }

        this.users = response.content;
        this.changeDetectorRef.detectChanges();
      });
  }
}
