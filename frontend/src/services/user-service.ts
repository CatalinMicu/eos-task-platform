import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { PageResponse } from './page-response';

export interface User {
  userId: number;
  username: string;
  birthDate: string;
  email: string;
  roleName: 'USER' | 'ADMIN' | 'SUPER_ADMIN';
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);

  getUsers() {
    return this.http.get<User[]>('http://localhost:8080/users');
  }

  getUserPage(
    page: number,
    size: number,
    sortBy: string,
    direction: string,
    search: string,
  ) {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction);

    if (search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<PageResponse<User>>(
      'http://localhost:8080/users/page',
      { params },
    );
  }

  changeUserRole(userId: number, newRole: 'USER' | 'ADMIN') {
    const params = new HttpParams().set('roleName', newRole);
    return this.http.patch<User>(
      `http://localhost:8080/users/${userId}/role`,
      null,
      { params },
    );
  }

  deleteUser(userId: number) {
    return this.http.delete(`http://localhost:8080/users/${userId}`);
  }
}
