package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.CommentDTO;
import de.nak.iaa.sundenbock.model.comment.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDTO toCommentDTO(Comment comment);
    Comment toComment(CommentDTO commentDTO);

    void updateCommentFromDTO(CommentDTO dto, @MappingTarget Comment entity);
}
