/**
 * @file comment.dto.ts
 *
 * Definiert die Data Transfer Object (DTO) Interfaces für Kommentare.
 * Dies sind die Rohdatenstrukturen für die API-Kommunikation,
 * z.B. `CommentDto` für gelesene Daten und `CreateCommentDto`
 * für das Erstellen neuer Kommentare.
 */
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

export interface CreateCommentDto {
  ticketId: number;
  parentCommentId?: number;
  commentText: string;
}