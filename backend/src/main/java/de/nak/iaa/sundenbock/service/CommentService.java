package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.dto.mapper.CommentMapper;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService{
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final TicketRepository ticketRepository;

    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public void deleteCommentWithChildren(Long commentId) {
        List<Long> childIds = commentRepository.findChildIdsByParentId(commentId);
        for (Long childId : childIds) {
            deleteCommentWithChildren(childId);
        }
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment with id " + commentId + " not found");
        }
        commentRepository.deleteByIdQuery(commentId);
    }

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

    @Transactional
    public CommentDTO updateComment(CommentDTO commentDTO) {
        Comment existingComment = commentRepository.findById(commentDTO.id())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " +  commentDTO.id()));

        commentMapper.updateCommentFromDTO(commentDTO, existingComment);
        return commentMapper.toCommentDTO(existingComment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByTicketId(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Associated ticket not found with id " + ticketId);
        }
        List<Comment> allComments = commentRepository.findByTicketId(ticketId);

        List<Comment> topLevelComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .toList();

        Set<Long> visited = new HashSet<>();
        topLevelComments.forEach(c -> buildRepliesTree(c, visited));

        return topLevelComments.stream()
                .map(commentMapper::toCommentDTO)
                .collect(Collectors.toList());
    }

    private void buildRepliesTree(Comment comment, Set<Long> visited) {
        if (!visited.add(comment.getId())) return;

        List<Comment> replies = comment.getChildComments();
        if (replies != null) {
            replies.forEach(c -> buildRepliesTree(c, visited));
        }
    }
}
