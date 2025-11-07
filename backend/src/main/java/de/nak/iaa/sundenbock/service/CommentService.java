package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.dto.mapper.CommentMapper;
import de.nak.iaa.sundenbock.exception.MismatchedIdException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class for managing {@link Comment} entities and their hierarchy.
 * <p>
 * Provides creation, update, and deletion logic for comments, including recursive
 * deletion of entire reply trees. Also handles associations to {@link Ticket}
 * and converts entities to DTOs using the {@link CommentMapper}.
 * </p>
 */
@Service
public class CommentService{
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final TicketRepository ticketRepository;

    /**
     * Creates a new {@code CommentService}.
     *
     * @param commentRepository repository for comments
     * @param commentMapper     mapper for comment and DTO conversions
     * @param ticketRepository  repository for tickets (to validate and resolve the associated ticket)
     */
    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Deletes a comment including all of its child comments (replies) recursively.
     *
     * @param commentId the ID of the comment to delete
     * @throws ResourceNotFoundException if the comment with the given ID does not exist
     */
    @Transactional
    public void deleteCommentWithChildren(Long commentId, Long ticketId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with id " + commentId + " not found"));

        Long commentReferenceTicketId = comment.getTicket().getId();

        if (commentReferenceTicketId == null) {
            throw new ResourceNotFoundException("Comment with id " + commentId + " is not associated with any ticket.");
        }

        if (!commentReferenceTicketId.equals(ticketId)) {
            throw new MismatchedIdException("Path variable 'ticketId' = " + ticketId
                    + " does not match 'ticketId' of original Comment = " + commentReferenceTicketId);
        }

        List<Long> childIds = commentRepository.findChildIdsByParentId(commentId);
        for (Long childId : childIds) {
            this.deleteCommentWithChildren(childId, ticketId);
        }

        commentRepository.deleteByIdQuery(commentId);
    }

    /**
     * Creates a new comment.
     * <p>
     * Validates the existence of the associated ticket and, if provided,
     * the parent comment. The created entity is persisted and returned as a DTO.
     * </p>
     *
     * @param createCommentDTO data for creating the comment
     * @return the created comment as {@link CommentDTO}
     * @throws ResourceNotFoundException if the ticket or the specified parent comment cannot be found
     */
    @Transactional
    public CommentDTO createComment(CreateCommentDTO createCommentDTO) {
        if (!ticketRepository.existsById(createCommentDTO.ticketId())){
            throw new ResourceNotFoundException("Ticket not found with id " + createCommentDTO.ticketId());
        }
        Ticket ticket = ticketRepository.getReferenceById(createCommentDTO.ticketId());

        Comment parentComment = null;
        if (createCommentDTO.parentCommentId() != null) {
            if (!commentRepository.existsById(createCommentDTO.parentCommentId())) {
                throw new ResourceNotFoundException("Parent comment not found with id " + createCommentDTO.parentCommentId());
            }
            parentComment = commentRepository.getReferenceById(createCommentDTO.parentCommentId());
        }
        Comment comment = commentMapper.toCommentForCreate(createCommentDTO);
        comment.setTicket(ticket);
        comment.setParentComment(parentComment);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDTO(savedComment);
    }

    /**
     * Updates an existing comment with the values from the provided {@link CommentDTO}.
     *
     * @param commentDTO the new values for the comment
     * @return the updated comment as {@link CommentDTO}
     * @throws ResourceNotFoundException if no comment exists with the given ID
     */
    @Transactional
    public CommentDTO updateComment(CommentDTO commentDTO) {
        Comment existingComment = commentRepository.findById(commentDTO.id())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " +  commentDTO.id()));

        commentMapper.updateCommentFromDTO(commentDTO, existingComment);
        return commentMapper.toCommentDTO(existingComment);
    }

    /**
     * Retrieves a paged list of top-level comments for a given ticket, including their nested replies.
     * <p>
     * Throws {@link ResourceNotFoundException} if the ticket with the specified ID does not exist.
     * </p>
     *
     * @param ticketId the ID of the ticket
     * @param pageable pagination information
     * @return a page of {@link CommentDTO} objects representing top-level comments and their replies
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> getPagedCommentsWithReplies(Long ticketId, Pageable pageable) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket not found with id " + ticketId);
        }

        Page<Comment> topLevelPage = commentRepository.findByTicketIdAndParentCommentIsNull(ticketId, pageable);

        List<Comment> topLevelComments = topLevelPage.getContent();
        Set<Long> visited = new HashSet<>();
        topLevelComments.forEach(c -> buildRepliesTree(c, visited));

        List<CommentDTO> dtos = topLevelComments.stream()
                .map(commentMapper::toCommentDTO)
                .toList();

        return new PageImpl<>(dtos, pageable, topLevelPage.getTotalElements());
    }

    /**
     * Recursively builds the tree of replies for a given comment, avoiding cycles.
     *
     * @param comment the comment to process
     * @param visited a set of comment IDs already visited to prevent infinite recursion
     */
    private void buildRepliesTree(Comment comment, Set<Long> visited) {
        if (!visited.add(comment.getId())) return;

        List<Comment> replies = comment.getChildComments();
        if (replies != null) {
            replies.forEach(c -> buildRepliesTree(c, visited));
        }
    }
}
