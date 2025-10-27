package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.CommentDTO;
import de.nak.iaa.sundenbock.dto.mapper.CommentMapper;
import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CommentService{
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
    }

    private CommentDTO convertToDTO(Comment comment) {
        return commentMapper.toCommentDTO(comment);
    }
    private Comment convertToEntity(CommentDTO commentDTO) {
        return commentMapper.toComment(commentDTO);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
