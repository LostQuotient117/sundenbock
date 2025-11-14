import { Injectable, inject } from '@angular/core';
import { ApiService } from '@core/http/api.service';
import { ResourceClient } from '@core/http/resource-client';
import { Observable } from 'rxjs';
import { Page, PageQuery } from '@shared/models/paging';
import { CommentDto, CreateCommentDto } from './comment.dto';
import { TicketDto } from './ticket.dto';

@Injectable({ providedIn: 'root' })
export class CommentsClient extends ResourceClient<CommentDto> {
  constructor() {
    super(inject(ApiService), '/tickets');
  }

  // Liste aller Kommentare zu einem Ticket
  listByTicket(ticketId: number | string, q?: PageQuery<CommentDto>): Observable<Page<CommentDto>> {
    return this.api.get<Page<CommentDto>>(`/tickets/${ticketId}/comments`, q);
  }

  //neuen Kommentar erstellen zu Ticket
  createForTicket(ticketId: number, body: CreateCommentDto): Observable<TicketDto> {
    return this.api.post<TicketDto>(`/tickets/${ticketId}/comments/create`, body);
  }
}