import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StatusService, StatusType } from '../../services/status-service';
import { SearchParams, Task, TaskService } from '../../services/task-service';

@Component({
  selector: 'app-search',
  imports: [FormsModule],
  templateUrl: './search.html',
  styleUrl: './search.css',
})
export class Search implements OnInit {
  private taskService = inject(TaskService);
  private statusService = inject(StatusService);
  private changeDetectorRef = inject(ChangeDetectorRef);

  tasks: Task[] = [];
  statuses: StatusType[] = [];
  searched = false;
  loading = false;

  assignedTo = '';
  subject = '';
  dueDate = '';
  status = '';

  ngOnInit(): void {
    this.statusService.getStatuses().subscribe((res) => {
      this.statuses = res;
      this.changeDetectorRef.detectChanges();
    });
  }

  search(): void {
    const params: SearchParams = {
      assignedTo: this.assignedTo,
      subject: this.subject,
      dueDate: this.dueDate,
      status: this.status,
    };

    this.loading = true;

    this.taskService.searchTasks(params).subscribe({
      next: (res) => {
        this.tasks = res;
        this.searched = true;
        this.loading = false;
        this.changeDetectorRef.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.changeDetectorRef.detectChanges();
      },
    });
  }

  clear(): void {
    this.assignedTo = '';
    this.subject = '';
    this.dueDate = '';
    this.status = '';
    this.tasks = [];
    this.searched = false;
    this.loading = false;
  }
}
