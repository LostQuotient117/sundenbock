package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.CommentDTO;
import de.nak.iaa.sundenbock.model.comment.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDTO toCommentDTO(Comment comment);
    Comment toComment(CommentDTO commentDTO);
}
