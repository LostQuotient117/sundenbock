package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.navigation.NavItem;
import de.nak.iaa.sundenbock.exception.MismatchedIdException;
import de.nak.iaa.sundenbock.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST controller exposing operations for managing comments that belong to a ticket.
 * <p>
 * Base path: {@code /api/v1/tickets/{ticketId}/comments}
 * </p>
 * <p>
 * All request payloads are validated using Jakarta Bean Validation. Path variables annotated with
 * {@link Min} ensure positive identifiers.
 * </p>
 */
@NavItem(label = "Comments", path = "/comments", icon = "comment")
@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@Validated
public class CommentController {

    private final CommentService commentService;

    /**
     * Creates a new instance of the controller.
     *
     * @param commentService service handling the comment business logic
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Retrieves all comments for a given ticket.
     *
     * @param ticketId the id of the ticket, must be greater than or equal to 1
     * @return list of comments for the specified ticket as {@link CommentDTO}
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TICKET_READ_ALL') or @customSecurityService.canAccessTicket(#ticketId, authentication)")
    public List<CommentDTO> getCommentsByTicket(@PathVariable @Min(1) Long ticketId) {
         return commentService.getCommentsByTicketId(ticketId);
    }

    /**
     * Creates a new comment.
     *
     * @param createCommentDTO payload containing the data required to create a comment; validated
     * @return the created comment as {@link CommentDTO}
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('COMMENT_CREATE') or @customSecurityService.canAccessTicket(#ticketId, authentication)")
    public CommentDTO createComment(@PathVariable @Min(1) Long ticketId,@Valid @RequestBody CreateCommentDTO createCommentDTO) {
        if (Objects.equals(ticketId, createCommentDTO.ticketId())) {
            return commentService.createComment(createCommentDTO);
        } else {
            throw new MismatchedIdException("Path variable 'ticketId' = " + ticketId + "does not match 'ticketId = " + createCommentDTO.ticketId() + " in request body");
        }
    }

    /**
     * Updates an existing comment.
     *
     * @param commentDTO payload containing the updated comment data; validated
     * @return the updated comment as {@link CommentDTO}
     */
    @PutMapping("/{commentId}/update")
    @PreAuthorize("hasAuthority('COMMENT_UPDATE') or @customSecurityService.isCommentOwner(#commentId, authentication)")
    public CommentDTO updateComment(@PathVariable @Min(1) Long commentId, @Valid @RequestBody CommentDTO commentDTO){
        if (Objects.equals(commentId, commentDTO.id())) {
            return commentService.updateComment(commentDTO);
        } else {
            throw new MismatchedIdException("Path variable 'commentId' = " + commentId + " does not match 'id' = " + commentDTO.id() +" in request body");
        }
    }

    /**
     * Deletes a comment and all of its child comments (if any).
     *
     * @param commentId the id of the comment to delete, must be greater than or equal to 1
     */
    @DeleteMapping("/{commentId}/delete")
    @PreAuthorize("hasAuthority('COMMENT_DELETE') or @customSecurityService.isCommentOwner(#commentId, authentication)")
    public void deleteComment(@PathVariable @Min(1) Long commentId) {
        commentService.deleteCommentWithChildren(commentId);
    }
}
