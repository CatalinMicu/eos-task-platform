import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface TaskComment {
  commentId: number;
  taskId: number;
  userId: number;
  username: string;
  body: string;
  creationDate: string;
}

@Injectable({
  providedIn: 'root',
})
export class TaskCommentService {
  private http = inject(HttpClient);

  getComments(taskId: number) {
    return this.http.get<TaskComment[]>(
      `http://localhost:8080/tasks/${taskId}/comments`,
    );
  }

  addComment(taskId: number, body: string) {
    return this.http.post<TaskComment>(
      `http://localhost:8080/tasks/${taskId}/comments`,
      { body },
    );
  }

  deleteComment(taskId: number, commentId: number) {
    return this.http.delete<void>(
      `http://localhost:8080/tasks/${taskId}/comments/${commentId}`,
    );
  }
}
