import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Page, PageQuery } from '@shared/models/paging';
import { CommentsClient } from '@features/tickets/data/comments.client';
import { CommentDto } from '@features/tickets/data/comment.dto';
import { TicketComment } from './comment.model';
import { mapPage } from '@shared/utils/mapping.base';
import { mapComment } from './comment.mapper';

@Injectable({ providedIn: 'root' })
export class CommentsService {
  private api = inject(CommentsClient);

  listByTicket(ticketId: number | string, q?: PageQuery<CommentDto>): Observable<Page<TicketComment>> {
    return this.api.listByTicket(ticketId, q).pipe(map((p) => mapPage(p, mapComment)));
  }
}
