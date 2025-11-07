package de.nak.iaa.sundenbock.model.ticket;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a ticket in the issue tracking domain.
 * <p>
 * A ticket has a title, optional description and a {@link TicketStatus}.
 * It is always associated with exactly one {@link Project} and one responsible {@link User}.
 * The entity inherits auditing information (creation and modification metadata)
 * from {@link AuditedEntity}. Comments are modeled via a one-to-many relationship
 * to {@link Comment} and are removed when the ticket is deleted.
 * </p>
 */
@Entity
@Setter
@Getter
public class Ticket extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String ticketKey;
    @Column(nullable = false)
    private String title;
    private String description;
    private TicketStatus status;
    @ManyToOne(optional = false)
    private User responsiblePerson;
    @ManyToOne(optional = false)
    private Project project;
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
