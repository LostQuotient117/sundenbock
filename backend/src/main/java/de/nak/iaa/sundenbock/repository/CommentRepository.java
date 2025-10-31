package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.comment.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    //find all top-level-comments
    @EntityGraph(attributePaths = {"comments"})
    List<Comment> findByTicketId(Long ticketId);

    @Query(value = "SELECT id FROM comment WHERE parent_comment_id = :parentId", nativeQuery = true)
    List<Long> findChildIdsByParentId(@Param("parentId") Long parentId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment WHERE id = :id", nativeQuery = true)
    void deleteByIdQuery(@Param("id") Long id);
}
