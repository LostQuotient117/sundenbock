package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.model.comment.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDTO toCommentDTO(Comment comment);
    Comment toCommentForCreate(CreateCommentDTO createCommentDTO);

    void updateCommentFromDTO(CommentDTO dto, @MappingTarget Comment entity);

}
