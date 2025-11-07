package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.model.comment.Comment;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between {@link Comment} entities and DTOs.
 * <p>
 * The component model is Spring, so the generated implementation is available for dependency
 * injection. User-related mappings are delegated to {@link UserMapper}.
 * </p>
 * <p>
 * Notable mappings when converting to {@link CommentDTO}:
 * </p>
 * <ul>
 *   <li>{@code ticket.id -> ticketId}</li>
 *   <li>{@code parentComment.id -> parentCommentId}</li>
 *   <li>{@code childComments -> childComments} (list of child DTOs)</li>
 * </ul>
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    /**
     * Maps a {@link Comment} entity to a {@link CommentDTO}.
     *
     * @param comment the entity to map; may be {@code null}
     * @return the mapped DTO or {@code null} if the input was {@code null}
     */
    @Mapping(source = "ticket.id", target = "ticketId")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    @Mapping(source = "childComments", target = "childComments")
    CommentDTO toCommentDTO(Comment comment);

    /**
     * Maps a {@link CreateCommentDTO} to a new {@link Comment} entity instance.
     * Fields not populated by the DTO (e.g. ids, timestamps, relationships) should be set by the
     * calling service layer prior to persistence.
     *
     * @param createCommentDTO the DTO carrying the data for a new comment; may be {@code null}
     * @return a new {@link Comment} populated from the DTO or {@code null} if the input was {@code null}
     */
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "childComments", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "dislikes", ignore = true)
    Comment toCommentForCreate(CreateCommentDTO createCommentDTO);

    /**
     * Updates an existing {@link Comment} entity with values from a {@link CommentDTO}.
     * MapStruct typically copies non-null values from the DTO into the target entity, preserving
     * existing values for {@code null} properties unless additional configuration is provided.
     *
     * @param dto    the source DTO (may be {@code null})
     * @param entity the target entity to update; must not be {@code null}
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "childComments", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    void updateCommentFromDTO(CommentDTO dto, @MappingTarget Comment entity);

}
