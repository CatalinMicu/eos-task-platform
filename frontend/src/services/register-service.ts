import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface RegisterData {
  username: string;
  birthDate: string;
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root',
})
export class RegisterService {
  private http = inject(HttpClient);

  register(data: RegisterData) {
    return this.http.post(
      'http://localhost:8080/register',
      {
        username: data.username,
        birthDate: data.birthDate,
        email: btoa(data.email),
        password: btoa(data.password),
      },
      {
        responseType: 'text',
      },
    );
  }
}
