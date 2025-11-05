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

    /**
     * Finds all comments for the given ticket and eagerly loads their direct child comments
     * via an {@link EntityGraph} on the {@code childComments} association.
     *
     * @param ticketId the ID of the ticket whose comments should be retrieved
     * @return a list of comments associated with the ticket, with child comments initialized
     */
    @EntityGraph(attributePaths = {"childComments"})
    List<Comment> findByTicketId(Long ticketId);

    /**
     * Returns the IDs of the direct child comments for the given parent comment ID.
     * Uses a native query to efficiently traverse the hierarchy.
     *
     * @param parentId the ID of the parent comment
     * @return list of IDs of direct child comments
     */
    @Query(value = "SELECT id FROM comment WHERE parent_comment_id = :parentId", nativeQuery = true)
    List<Long> findChildIdsByParentId(@Param("parentId") Long parentId);

    /**
     * Deletes a comment by its ID using a native query.
     * <p>
     * Marked with {@link Modifying} and {@link Transactional} because it performs a write operation.
     * Typically used as part of a recursive deletion strategy in the service layer.
     * </p>
     *
     * @param id the ID of the comment to delete
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment WHERE id = :id", nativeQuery = true)
    void deleteByIdQuery(@Param("id") Long id);
}
