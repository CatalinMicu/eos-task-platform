import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StatusService, StatusType } from '../../services/status-service';
import { Task, TaskData, TaskService } from '../../services/task-service';
import { User, UserService } from '../../services/user-service';
import { LoginService } from '../../services/login-service';
import { NewTask } from '../new-task/new-task';
import { TaskComments } from '../task-comments/task-comments';
import { ConfirmDialog } from '../confirm-dialog/confirm-dialog';
import { ActivatedRoute } from '@angular/router';

type TaskView = 'mine' | 'assigned' | 'all';

@Component({
  selector: 'app-my-tasks',
  imports: [FormsModule, NewTask, TaskComments, ConfirmDialog],
  templateUrl: './my-tasks.html',
  styleUrl: './my-tasks.css',
})
export class MyTasks implements OnInit {
  private taskService = inject(TaskService);
  private statusService = inject(StatusService);
  private userService = inject(UserService);
  loginService = inject(LoginService);
  private changeDetectorRef = inject(ChangeDetectorRef);
  private route = inject(ActivatedRoute);

  tasks: Task[] = [];
  statuses: StatusType[] = [];
  users: User[] = [];
  selectedTask: Task | null = null;
  commentTask: Task | null = null;
  taskToDelete: Task | null = null;
  isModalOpen = false;
  taskView: TaskView = 'mine';
  highlightedTaskId: number | null = null;
  currentPage = 1;
  totalPages = 1;
  totalElements = 0;
  readonly pageSize = 8;
  sortBy = 'id';
  sortDirection = 'asc';

  get pageTitle(): string {
    if (this.taskView === 'assigned') {
      return 'Assigned Tasks';
    }
    if (this.taskView === 'all') {
      return 'All Tasks';
    }
    return 'My Tasks';
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const taskId = Number(params.get('taskId'));
      if (taskId > 0) {
        this.loadRequestedTask(taskId);
      } else {
        this.loadTasks();
      }
    });

    this.loadStatuses();
    if (this.loginService.isAdmin) {
      this.loadUsers();
    }
  }

  openNewTask(): void {
    this.selectedTask = null;
    this.isModalOpen = true;
  }

  openEditTask(task: Task): void {
    this.selectedTask = task;
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.selectedTask = null;
  }

  openComments(task: Task): void {
    this.commentTask = task;
  }

  closeComments(): void {
    this.commentTask = null;
  }

  showMyTasks(): void {
    this.taskView = 'mine';
    this.currentPage = 1;
    this.loadTasks();
  }

  showAssignedTasks(): void {
    this.taskView = 'assigned';
    this.currentPage = 1;
    this.loadTasks();
  }

  showEveryTask(): void {
    this.taskView = 'all';
    this.currentPage = 1;
    this.loadTasks();
  }

  changeTaskSorting(): void {
    this.currentPage = 1;
    this.loadTasks();
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadTasks();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadTasks();
    }
  }

  saveTask(taskData: TaskData): void {
    if (taskData.taskId !== null) {
      this.taskService
        .updateTask(taskData.taskId, taskData)
        .subscribe(() => {
          this.showAssignedTask(taskData);
          this.closeModal();
          this.loadTasks();
        });
      return;
    }

    this.taskService.createTask(taskData).subscribe(() => {
      this.showAssignedTask(taskData);
      this.closeModal();
      this.loadTasks();
    });
  }

  requestTaskDelete(task: Task): void {
    this.taskToDelete = task;
  }

  confirmTaskDelete(task: Task): void {
    this.taskService.deleteTask(task.taskId).subscribe(() => {
      this.taskToDelete = null;
      this.loadTasks();
    });
  }

  private loadTasks(): void {
    this.taskService
      .getTaskPage(
        this.currentPage - 1,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
        this.taskView,
      )
      .subscribe((response) => {
        this.totalPages = Math.max(1, response.totalPages);
        this.totalElements = response.totalElements;

        if (this.currentPage > this.totalPages) {
          this.currentPage = this.totalPages;
          this.loadTasks();
          return;
        }

        this.tasks = response.content;
        this.highlightedTaskId = null;
        this.changeDetectorRef.detectChanges();
      });
  }

  private loadStatuses(): void {
    this.statusService.getStatuses().subscribe((res) => {
      this.statuses = res;
      this.changeDetectorRef.detectChanges();
    });
  }

  private loadUsers(): void {
    this.userService.getUsers().subscribe((response) => {
      this.users = response;
      this.changeDetectorRef.detectChanges();
    });
  }

  private showAssignedTask(taskData: TaskData): void {
    const currentUser = this.loginService.currentUser();
    if (currentUser === null) {
      return;
    }

    if (this.loginService.isAdmin && taskData.userId !== currentUser.userId) {
      this.taskView = 'assigned';
      this.currentPage = 1;
    }
  }

  private loadRequestedTask(taskId: number): void {
    this.taskService.getTaskById(taskId).subscribe({
      next: (task) => {
        if (task === null) {
          this.loadTasks();
          return;
        }

        this.showRequestedTask(task);
      },
      error: () => this.loadTasks(),
    });
  }

  private showRequestedTask(task: Task): void {
    const currentUser = this.loginService.currentUser();
    if (currentUser === null) {
      return;
    }

    let createdByCurrentUser = false;
    if (task.createdBy) {
      createdByCurrentUser =
        task.createdBy.toLowerCase() === currentUser.email.toLowerCase();
    }

    if (task.userId === currentUser.userId) {
      this.taskView = 'mine';
    } else if (createdByCurrentUser) {
      this.taskView = 'assigned';
    } else {
      this.taskView = 'all';
    }

    this.tasks = [task];
    this.currentPage = 1;
    this.totalPages = 1;
    this.totalElements = 1;
    this.highlightedTaskId = task.taskId;
    this.changeDetectorRef.detectChanges();

    setTimeout(() => {
      const taskElement = document.getElementById(`task-${task.taskId}`);
      if (taskElement !== null) {
        taskElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  }

  updateStatus(taskId: number, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const statusName = select.value;

    this.taskService.updateTaskStatus(taskId, statusName).subscribe({
      next: () => this.loadTasks(),
      error: () => this.loadTasks(),
    });
  }
}
