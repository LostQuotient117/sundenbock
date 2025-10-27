package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
