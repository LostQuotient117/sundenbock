import { CommonModule, DatePipe } from '@angular/common';
import { Component, effect, inject, input, Output, EventEmitter, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Page } from '@shared/models/paging';
import { Ticket } from '@features/tickets/domain/ticket.model';
import { TicketComment } from '@features/tickets/domain/comment.model';
import { CommentsService } from '@features/tickets/domain/comments.service';
import { CreateCommentDto } from '@features/tickets/data/comment.dto';

@Component({
  standalone: true,
  selector: 'ticket-comments',
  imports: [CommonModule, DatePipe, ReactiveFormsModule],
  templateUrl: './ticket-comments.html',
})
export class TicketCommentsComponent {
  private commentsSvc = inject(CommentsService);
  private fb = inject(FormBuilder);

  ticket = input.required<Ticket>();
  @Output() ticketChanged = new EventEmitter<Ticket>();

  comments = signal<Page<TicketComment> | null>(null);
  commentSaving = signal(false);
  commentError = signal<string | null>(null);
  replyToCommentId = signal<number | null>(null);

  readonly Math = Math;

  commentForm = this.fb.nonNullable.group({
    commentText: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  constructor() {
    effect(() => {
      const t = this.ticket();
      if (!t?.id) return;
      this.loadComments(t.id);
    });
  }

  private loadComments(ticketId: number | string) {
    this.comments.set(null);
    this.commentsSvc
      .listByTicket(ticketId, { page: 0, pageSize: 20, sort: 'createdDate:asc' })
      .subscribe({
        next: (p) => this.comments.set(p),
        error: () => {
          this.comments.set({ items: [], total: 0, page: 0, pageSize: 20 });
        },
      });
  }

  // Helper fürs Template: Delegation an Service
  isVoted(commentId: number | undefined, type: 'LIKE' | 'DISLIKE'): boolean {
    return this.commentsSvc.isVoted(commentId, type);
  }

  startReply(commentId: number) {
    this.replyToCommentId.set(commentId);
    this.commentError.set(null);
    this.commentForm.reset({ commentText: '' });
  }

  cancelReply() {
    this.replyToCommentId.set(null);
    this.commentError.set(null);
    this.commentForm.reset({ commentText: '' });
  }

  onCommentFormSubmit(ev: Event) {
    ev.preventDefault();
    this.replyToCommentId.set(null);
    this.submitComment();
  }

  onReplyFormSubmit(ev: Event, parentId: number) {
    ev.preventDefault();
    this.replyToCommentId.set(parentId);
    this.submitComment();
  }

  private submitComment() {
    const t = this.ticket();
    if (!t || this.commentForm.invalid) {
      this.commentForm.markAllAsTouched();
      return;
    }

    const ticketId = Number(t.id);

    const body: CreateCommentDto = {
      ticketId,
      commentText: this.commentForm.value.commentText!,
      parentCommentId: this.replyToCommentId() ?? undefined,
    };

    this.commentSaving.set(true);
    this.commentError.set(null);

    this.commentsSvc.create(ticketId, body).subscribe({
      next: (updatedTicket: Ticket) => {
        this.commentSaving.set(false);
        this.cancelReply();
        this.ticketChanged.emit(updatedTicket);
        this.loadComments(ticketId);
      },
      error: (err) => {
        this.commentSaving.set(false);
        this.commentError.set(err?.error?.message ?? 'Kommentar konnte nicht gespeichert werden.');
      },
    });
  }

  onLike(comment: TicketComment) {
    const t = this.ticket();
    if (!t?.id) return;

    this.commentsSvc.toggleVote(t.id, comment, 'LIKE').subscribe({
      next: (page) => this.comments.set(page),
      error: (err) => {
        console.error('Like fehlgeschlagen', err);
      },
    });
  }

  onDislike(comment: TicketComment) {
    const t = this.ticket();
    if (!t?.id) return;

    this.commentsSvc.toggleVote(t.id, comment, 'DISLIKE').subscribe({
      next: (page) => this.comments.set(page),
      error: (err) => {
        console.error('Dislike fehlgeschlagen', err);
      },
    });
  }
}
