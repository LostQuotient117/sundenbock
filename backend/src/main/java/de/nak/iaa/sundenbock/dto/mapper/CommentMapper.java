package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.model.comment.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    @Mapping(source = "ticket.id", target = "ticketId")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    @Mapping(source = "childComments", target = "childComments")
    CommentDTO toCommentDTO(Comment comment);
    Comment toCommentForCreate(CreateCommentDTO createCommentDTO);

    void updateCommentFromDTO(CommentDTO dto, @MappingTarget Comment entity);

}
