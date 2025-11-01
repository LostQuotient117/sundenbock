package de.nak.iaa.sundenbock.dto.commentDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentDTO(
        @NotNull(message = "The associated ticket id is needed")
        Long ticketId,
        Long parentCommentId,
        @NotBlank(message = "The comment needs a body text")
        @Size(max = 2000, message = "The comment body-text must not exceed 2000 characters")
        String commentText
) {}
