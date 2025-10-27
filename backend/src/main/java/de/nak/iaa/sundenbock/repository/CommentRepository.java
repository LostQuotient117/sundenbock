package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.comment.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    //find all top-level-comments
    @EntityGraph(attributePaths = {"comments"})
    List<Comment> findByTicketId(Long ticketId);
    //find only top-level-comments
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.parentComment IS NULL")
    List<Comment> findTopLevelCommentsByTicketId(@Param("ticketId") Long ticketId); //TODO: Remove when not in use
}
