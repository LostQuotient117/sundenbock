package de.nak.iaa.sundenbock.dto.commentDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.nak.iaa.sundenbock.model.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

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
        @JsonIgnoreProperties("replies")
        List<CommentDTO> replies,
        //!-- from AuditedEntity --!
        Instant createdDate,
        Instant lastModifiedDate,
        User createdBy,
        User lastModifiedBy
        ){}
