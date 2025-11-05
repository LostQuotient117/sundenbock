package de.nak.iaa.sundenbock.model.comment;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a comment that can be attached to a {@link Ticket}
 * and can form a threaded discussion via parent/child relations.
 * <p>
 * Inherits auditing information from {@link AuditedEntity} (e.g. created and last modified timestamps).
 * Child comments are ordered by {@code createdDate} ascending. Deleting a parent
 * comment cascades the deletion to its children (database-level cascade via {@link OnDelete}).
 */
@Entity
@Setter
@Getter
public class Comment extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //--Hierarchy for CommentToTicket--
    @ManyToOne
    private Ticket ticket;
    //--Hierarchy for Comments--
    @OneToMany(mappedBy = "parentComment")
    @OrderBy("createdDate ASC")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> childComments = new ArrayList<>();
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Comment parentComment;
    private String commentText;
    private int likes;
    private int dislikes;
}

