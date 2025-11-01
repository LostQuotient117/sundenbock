package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.dto.mapper.CommentMapper;
import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public void deleteCommentWithChildren(Long parentId) {
        List<Long> childIds = commentRepository.findChildIdsByParentId(parentId);
        for (Long childId : childIds) {
            deleteCommentWithChildren(childId);
        }

        commentRepository.deleteByIdQuery(parentId);
    }

    @Transactional
    public CommentDTO createComment(CreateCommentDTO createCommentDTO) {
        Ticket ticket = ticketRepository.getReferenceById(createCommentDTO.ticketId());
        Comment parentComment = commentRepository.getReferenceById(createCommentDTO.parentCommentId());
        Comment comment = commentMapper.toCommentForCreate(createCommentDTO);
        comment.setTicket(ticket);
        comment.setParentComment(parentComment);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDTO(savedComment);
    }

    @Transactional
    public CommentDTO updateComment(CommentDTO commentDTO) {
        Comment existingComment = commentRepository.findById(commentDTO.id())
                .orElseThrow(() -> new  RuntimeException("Comment not found"));

        commentMapper.updateCommentFromDTO(commentDTO, existingComment);
        return commentMapper.toCommentDTO(existingComment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByTicketId(Long ticketId) {
        List<Comment> allComments = commentRepository.findByTicketId(ticketId);

        List<Comment> topLevelComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .toList();

        topLevelComments.forEach(this::buildRepliesTree);

        return topLevelComments.stream()
                .map(commentMapper::toCommentDTO)
                .collect(Collectors.toList());
    }

    private void buildRepliesTree(Comment comment) {
        List<Comment> replies = comment.getChildComments();
        replies.forEach(this::buildRepliesTree);
    }


}
