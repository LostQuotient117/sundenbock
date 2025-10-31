package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.comment.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    //find all top-level-comments
    @EntityGraph(attributePaths = {"comments"})
    List<Comment> findByTicketId(Long ticketId);
}
