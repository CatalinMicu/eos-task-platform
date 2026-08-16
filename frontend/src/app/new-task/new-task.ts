import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StatusType } from '../../services/status-service';
import { Task, TaskData } from '../../services/task-service';
import { User } from '../../services/user-service';

@Component({
  selector: 'app-new-task',
  imports: [FormsModule],
  templateUrl: './new-task.html',
  styleUrl: './new-task.css',
})
export class NewTask implements OnInit {
  @Input() task: Task | null = null;
  @Input() statuses: StatusType[] = [];
  @Input() users: User[] = [];
  @Output() save = new EventEmitter<TaskData>();
  @Output() cancel = new EventEmitter<void>();

  today = '';

  model: TaskData = {
    taskId: null,
    name: '',
    dueDate: '',
    statusName: '',
    userId: 0,
    body: '',
  };

  ngOnInit(): void {
    const currentDate = new Date();
    const year = currentDate.getFullYear();
    const month = String(currentDate.getMonth() + 1).padStart(2, '0');
    const day = String(currentDate.getDate()).padStart(2, '0');
    this.today = `${year}-${month}-${day}`;

    if (this.task) {
      this.model.taskId = this.task.taskId;
      this.model.name = this.task.name;
      this.model.dueDate = this.task.dueDate;
      this.model.statusName = this.task.statusName;
      this.model.userId = this.task.userId;
      if (this.task.body === null) {
        this.model.body = '';
      } else {
        this.model.body = this.task.body;
      }
    } else if (this.statuses.length > 0) {
      this.model.statusName = this.statuses[0].statusName;
    }
  }

  submit(): void {
    if (
      !this.model.name.trim() ||
      !this.model.body.trim() ||
      !this.model.dueDate ||
      this.model.dueDate < this.today ||
      !this.model.statusName ||
      this.model.userId === 0
    ) {
      return;
    }

    this.save.emit({
      taskId: this.model.taskId,
      name: this.model.name,
      dueDate: this.model.dueDate,
      statusName: this.model.statusName,
      userId: this.model.userId,
      body: this.model.body.trim(),
    });
  }
}
