package de.nak.iaa.sundenbock.dto.commentDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object used to create a new comment.
 * <p>
 * This immutable record captures the minimal information required by the API to create a comment
 * on a ticket (optionally as a reply to another comment). It relies on Jakarta Bean Validation
 * to enforce basic constraints on incoming requests.
 * <p>
 * Instances are typically produced by the web layer when deserializing a request body and consumed
 * by the service layer to create the corresponding domain entity.
 */
public record CreateCommentDTO(
        @NotNull(message = "The associated ticket id is needed")
        Long ticketId,
        Long parentCommentId,
        @NotBlank(message = "The comment needs a body text")
        @Size(max = 2000, message = "The comment body-text must not exceed 2000 characters")
        String commentText
) {}
