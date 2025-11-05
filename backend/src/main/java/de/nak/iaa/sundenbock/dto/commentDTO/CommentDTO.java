package de.nak.iaa.sundenbock.dto.commentDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object representing a comment as exposed by the API.
 * <p>
 * This immutable record is used for reading and transferring comment data across application layers.
 * It includes the comment hierarchy (parent/children), reaction counts, and auditing information
 * such as creation/modification timestamps and users.
 * <p>
 * Jakarta Bean Validation annotations document expected constraints for values handled by the API.
 */
public record CommentDTO(
        Long id,
        @NotNull
        Long ticketId,
        Long parentCommentId,
        @NotBlank(message = "The comment text must not be empty")
        @Size(max = 2000, message = "the comment text must not exceed 2000 characters")
        String commentText,
        int likes,
        int dislikes,
        @JsonIgnoreProperties("childComments")
        List<CommentDTO> childComments,
        //!-- from AuditedEntity --!
        Instant createdDate,
        Instant lastModifiedDate,
        UserDTO createdBy,
        UserDTO lastModifiedBy
        ){}
