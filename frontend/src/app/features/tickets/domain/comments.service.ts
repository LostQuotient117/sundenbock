import { Injectable, inject } from '@angular/core';
import { Observable, map, switchMap } from 'rxjs';
import { Page, PageQuery } from '@shared/models/paging';
import { CommentsClient } from '@features/tickets/data/comments.client';
import { CommentDto, CreateCommentDto } from '@features/tickets/data/comment.dto';
import { TicketComment } from './comment.model';
import { mapPage } from '@shared/utils/mapping.base';
import { mapComment } from './comment.mapper';
import { mapTicket } from './ticket.mapper';
import { Ticket } from './ticket.model';

@Injectable({ providedIn: 'root' })
export class CommentsService {
  private api = inject(CommentsClient);
  private votes: Record<number, 'LIKE' | 'DISLIKE' | null> = {};

  isVoted(commentId: number | undefined, type: 'LIKE' | 'DISLIKE'): boolean {
    if (commentId == null) return false;
    return this.votes[commentId] === type;
  }

  listByTicket(ticketId: number | string, q?: PageQuery<CommentDto>): Observable<Page<TicketComment>> {
    return this.api.listByTicket(ticketId, q).pipe(map((p) => mapPage(p, mapComment)));
  }

  // Kommentar für ticket erstellen
  create(ticketId: number, body: CreateCommentDto): Observable<Ticket> {
    return this.api.createForTicket(ticketId, body).pipe(map(mapTicket));
  }

  toggleVote(
    ticketId: number | string,
    comment: TicketComment,
    type: 'LIKE' | 'DISLIKE'
  ): Observable<Page<TicketComment>> {
    if (!comment.id) {
      return this.listByTicket(ticketId);
    }

    const commentId = comment.id;
    const prev = this.votes[commentId] ?? null;

    let likes = comment.likes ?? 0;
    let dislikes = comment.dislikes ?? 0;
    let next: 'LIKE' | 'DISLIKE' | null = prev;

    if (type === 'LIKE') {
      if (prev === 'LIKE') {
        // Like zurücknehmen
        likes = Math.max(0, likes - 1);
        next = null;
      } else {
        // Like setzen
        likes = likes + 1;
        if (prev === 'DISLIKE') {
          // alten Dislike zurücknehmen
          dislikes = Math.max(0, dislikes - 1);
        }
        next = 'LIKE';
      }
    } else {
      // type === 'DISLIKE'
      if (prev === 'DISLIKE') {
        // Dislike zurücknehmen
        dislikes = Math.max(0, dislikes - 1);
        next = null;
      } else {
        // Dislike setzen
        dislikes = dislikes + 1;
        if (prev === 'LIKE') {
          // alten Like zurücknehmen
          likes = Math.max(0, likes - 1);
        }
        next = 'DISLIKE';
      }
    }

    // Ticket-ID für DTO als number
    const numericTicketId =
      typeof ticketId === 'string' ? Number(ticketId) : ticketId;

    if (!numericTicketId || Number.isNaN(numericTicketId)) {
      console.error('toggleVote: ungültige Ticket-ID', ticketId);
      return this.listByTicket(ticketId);
    }

    const dto: CommentDto = {
      id: comment.id,
      ticketId: numericTicketId,
      parentCommentId: comment.parentCommentId,
      commentText: comment.commentText,
      likes,
      dislikes,
    };

    return this.api
      .updateComment(numericTicketId, commentId, dto)
      .pipe(
        // nach erfolgreichem Update Kommentare neu laden
        switchMap(() => this.listByTicket(numericTicketId)),
        map((page) => {
          // lokalen Vote-State erst nach Erfolg setzen
          this.votes[commentId] = next;
          return page;
        })
      );
  }
}
