/**
 * @file comment.model.ts
 *
 * Definiert das Kern-Domain-Model `TicketComment` und `CommentAuthor`.
 * Diese Interfaces repräsentieren die "saubere" Datenstruktur,
 * die innerhalb der Angular-Anwendung verwendet wird (z.B. mit
 * `Date`-Objekten statt ISO-Strings).
 */
export interface CommentAuthor {
  id?: number;
  username?: string;
  firstName?: string;
  lastName?: string;
}

export interface TicketComment {
  id?: number;
  ticketId: number | string;
  parentCommentId?: number;
  commentText: string;
  likes?: number;
  dislikes?: number;
  childComments?: TicketComment[];
  createdDate?: Date;
  lastModifiedDate?: Date;
  createdBy?: CommentAuthor;
  lastModifiedBy?: CommentAuthor;
}