export interface CommentAuthor {
  id?: number;
  username?: string;
  firstName?: string;
  lastName?: string;
}

export interface Comment {
  id?: number;
  ticketId: number | string;
  parentCommentId?: number;
  commentText: string;
  likes?: number;
  dislikes?: number;
  childComments?: Comment[];
  createdDate?: Date;
  lastModifiedDate?: Date;
  createdBy?: CommentAuthor;
  lastModifiedBy?: CommentAuthor;
}