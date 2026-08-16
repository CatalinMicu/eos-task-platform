import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { PageResponse } from './page-response';

export interface Task {
  taskId: number;
  name: string;
  dueDate: string;
  statusName: string;
  userId: number;
  assignedTo: string;
  body: string | null;
  createdBy: string;
}

export interface SearchParams {
  assignedTo: string;
  subject: string;
  dueDate: string;
  status: string;
}

export interface TaskData {
  taskId: number | null;
  name: string;
  dueDate: string;
  statusName: string;
  userId: number;
  body: string;
}

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private http = inject(HttpClient);

  getTaskPage(
    page: number,
    size: number,
    sortBy: string,
    direction: string,
    view: string,
  ) {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction)
      .set('view', view);

    return this.http.get<PageResponse<Task>>(
      'http://localhost:8080/tasks/page',
      { params },
    );
  }

  getTaskById(taskId: number) {
    return this.http.get<Task | null>(
      `http://localhost:8080/tasks/${taskId}`,
    );
  }

  createTask(task: TaskData) {
    return this.http.post<Task>('http://localhost:8080/tasks', {
      name: task.name,
      dueDate: task.dueDate,
      statusName: task.statusName,
      userId: task.userId,
      body: task.body,
    });
  }

  updateTask(taskId: number, task: TaskData) {
    return this.http.put<Task>(`http://localhost:8080/tasks/${taskId}`, {
      name: task.name,
      dueDate: task.dueDate,
      statusName: task.statusName,
      userId: task.userId,
      body: task.body,
    });
  }

  updateTaskStatus(taskId: number, statusName: string) {
    const params = new HttpParams().set('statusName', statusName);

    return this.http.patch<Task>(
      `http://localhost:8080/tasks/${taskId}/status`,
      null,
      { params },
    );
  }

  deleteTask(taskId: number) {
    return this.http.delete<void>(
      `http://localhost:8080/tasks/${taskId}`,
    );
  }

  searchTasks(params: SearchParams) {
    let httpParams = new HttpParams();

    if (params.assignedTo) {
      httpParams = httpParams.set('assignedTo', params.assignedTo);
    }
    if (params.subject) {
      httpParams = httpParams.set('subject', params.subject);
    }
    if (params.dueDate) {
      httpParams = httpParams.set('dueDate', params.dueDate);
    }
    if (params.status) {
      httpParams = httpParams.set('status', params.status);
    }

    return this.http.get<Task[]>('http://localhost:8080/tasks/search', {
      params: httpParams,
    });
  }
}
