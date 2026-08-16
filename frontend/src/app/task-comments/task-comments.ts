import { DatePipe } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../../services/login-service';
import {
  TaskComment,
  TaskCommentService,
} from '../../services/task-comment-service';
import { Task } from '../../services/task-service';
import { ConfirmDialog } from '../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-task-comments',
  imports: [FormsModule, DatePipe, ConfirmDialog],
  templateUrl: './task-comments.html',
  styleUrl: './task-comments.css',
})
export class TaskComments implements OnInit {
  @Input({ required: true }) task!: Task;
  @Output() close = new EventEmitter<void>();

  private taskCommentService = inject(TaskCommentService);
  private changeDetectorRef = inject(ChangeDetectorRef);
  loginService = inject(LoginService);

  comments: TaskComment[] = [];
  newComment = '';
  isSaving = false;
  commentToDelete: TaskComment | null = null;
  errorMessage = '';

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.taskCommentService.getComments(this.task.taskId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.errorMessage = '';
        this.changeDetectorRef.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Comments could not be loaded.';
        this.changeDetectorRef.detectChanges();
      },
    });
  }

  addComment(): void {
    const body = this.newComment.trim();

    if (!body || this.isSaving) {
      return;
    }

    this.isSaving = true;

    this.taskCommentService.addComment(this.task.taskId, body).subscribe({
      next: (savedComment) => {
        this.comments.push(savedComment);
        this.newComment = '';
        this.isSaving = false;
        this.errorMessage = '';
        this.changeDetectorRef.detectChanges();
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Comment could not be sent.';
        this.changeDetectorRef.detectChanges();
      },
    });
  }

  requestCommentDelete(comment: TaskComment): void {
    this.commentToDelete = comment;
  }

  confirmCommentDelete(comment: TaskComment): void {
    const commentId = comment.commentId;

    this.taskCommentService
      .deleteComment(this.task.taskId, commentId)
      .subscribe({
        next: () => {
          const remainingComments: TaskComment[] = [];

          for (const comment of this.comments) {
            if (comment.commentId !== commentId) {
              remainingComments.push(comment);
            }
          }

          this.comments = remainingComments;
          this.commentToDelete = null;
          this.errorMessage = '';
          this.changeDetectorRef.detectChanges();
        },
        error: () => {
          this.commentToDelete = null;
          this.errorMessage = 'Comment could not be deleted.';
          this.changeDetectorRef.detectChanges();
        },
      });
  }

  isOwnComment(comment: TaskComment): boolean {
    const currentUser = this.loginService.currentUser();
    if (currentUser === null) {
      return false;
    }

    return comment.userId === currentUser.userId;
  }
}
