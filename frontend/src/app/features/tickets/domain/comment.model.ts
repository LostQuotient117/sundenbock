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