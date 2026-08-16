import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface StatusType {
  statusTypeId: string;
  statusName: string;
}

@Injectable({
  providedIn: 'root',
})
export class StatusService {
  private http = inject(HttpClient);

  getStatuses() {
    return this.http.get<StatusType[]>('http://localhost:8080/statuses');
  }
}
