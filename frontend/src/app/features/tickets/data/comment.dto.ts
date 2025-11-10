export interface UserDto {
  id?: number;
  username?: string;
  firstName?: string;
  lastName?: string;
}

export interface CommentDto {
  id?: number;
  ticketId: number;
  parentCommentId?: number;
  commentText: string;
  likes?: number;
  dislikes?: number;
  childComments?: CommentDto[];
  createdDate?: string;       
  lastModifiedDate?: string;  
  createdBy?: UserDto;
  lastModifiedBy?: UserDto;
}