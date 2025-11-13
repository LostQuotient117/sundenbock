import { defineMapper, mapBySpec } from '@shared/utils/mapping/mapping.dsl';
import { CommentDto, UserDto } from '@features/tickets/data/comment.dto';
import { CommentAuthor, TicketComment } from './comment.model';

const toDate = (s?: string) => (s ? new Date(s) : undefined);

const authorSpec = defineMapper<UserDto, CommentAuthor>()({
  id:        { kind: 'keep' },
  username:  { kind: 'keep' },
  firstName: { kind: 'keep' },
  lastName:  { kind: 'keep' },
} as const);

export const commentSpec = defineMapper<CommentDto, TicketComment>()({
  id:              { kind: 'keep' },
  ticketId:        { kind: 'keep' },
  parentCommentId: { kind: 'keep' },
  commentText:     { kind: 'keep' },
  likes:           { kind: 'keep' },
  dislikes:        { kind: 'keep' },

  createdDate:     { kind: 'map', map: toDate },
  lastModifiedDate:{ kind: 'map', map: toDate },

  createdBy: {
    kind: 'map',
    from: 'createdBy',
    map: (u) => (u ? mapBySpec(u, authorSpec) : undefined),
  },
  lastModifiedBy: {
    kind: 'map',
    from: 'lastModifiedBy',
    map: (u) => (u ? mapBySpec(u, authorSpec) : undefined),
  },

  childComments: {
    kind: 'array',
    from: 'childComments',
    map: (c: CommentDto) => mapBySpec(c, commentSpec),
  },
} as const);

export function mapComment(dto: CommentDto): TicketComment {
  return mapBySpec(dto, commentSpec);
}
