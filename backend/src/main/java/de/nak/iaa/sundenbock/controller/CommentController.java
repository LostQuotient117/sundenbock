package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.PageDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.MismatchedIdException;
import de.nak.iaa.sundenbock.pageable.PageableFactory;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private final PageableFactory pageableFactory;
    private final TicketRepository  ticketRepository;

    private static final Map<String, String> SORT_ALIAS = Map.of("createdOn", "createdDate",
                                                                "author", "createdBy.username");

    private static final Set<String> SORT_WHITELIST = Set.of("createdDate", "lastModifiedDate", "likes", "dislikes", "commentText", "createdBy.username");

    /**
     * Creates a new instance of the controller.
     *
     * @param commentService service handling the comment business logic
     */
    public CommentController(CommentService commentService, PageableFactory pageableFactory, TicketRepository ticketRepository) {
        this.commentService = commentService;
        this.pageableFactory = pageableFactory;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Retrieves a paginated list of comments, including their nested replies,
     * for a specific ticket.
     * <p>
     * This endpoint fetches comments in a structured, paginated, and sortable manner.
     * The resulting {@link CommentDTO} objects contain their respective replies.
     *
     * <h3>Security:</h3>
     * Access is granted based on two conditions:
     * <ol>
     * <li>The user has the global {@code TICKET_READ_ALL} authority.</li>
     * <li>OR, the {@code @customSecurityService} determines that the current
     * authenticated user has permission to access the specific {@code ticketId}
     * (e.g., they are the author, assignee, etc.).</li>
     * </ol>
     *
     * @param ticketId The unique identifier (from the URL path) of the ticket
     * for which to retrieve comments.
     * @param page     (Optional) The page number of comments to retrieve,
     * 0-indexed. Defaults to 0.
     * @param pageSize (Optional) The number of comments to retrieve per
     * page. Defaults to 20.
     * @param sort     (Optional) A string defining the sorting criteria for the
     * comments (e.g., "createdAt,desc"). Must adhere
     * to the {@code SORT_WHITELIST}.
     * @return A {@link PageDTO} containing the requested page of {@link CommentDTO}s
     * (which include their replies), along with pagination metadata.
     * @throws ResourceNotFoundException if the ticket specified by {@code ticketId}
     * does not exist.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TICKET_READ_ALL') or @customSecurityService.canAccessTicket(#ticketId, authentication)")
    public PageDTO<CommentDTO> getPagedCommentsWithReplies(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String sort
    ) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Associated ticket not found with id " + ticketId);
        }
        Pageable pageable = pageableFactory.createPageable(page, pageSize, sort, SORT_WHITELIST, SORT_ALIAS);
        Page<CommentDTO> commentPage = commentService.getPagedCommentsWithReplies(ticketId, pageable);

        return PageDTO.of(
                commentPage.getContent(),
                commentPage.getTotalElements(),
                page,
                pageSize
        );
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
    public void deleteComment(@PathVariable @Min(1) Long commentId, @PathVariable @Min(1) Long ticketId) {
        commentService.deleteCommentWithChildren(commentId, ticketId);
    }
}
