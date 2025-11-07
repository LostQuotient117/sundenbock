package de.nak.iaa.sundenbock.model.project;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a project that groups related tickets.
 * <p>
 * A project has a unique title and an optional description. It owns a collection of
 * {@link Ticket} entities via a one-to-many relationship. Auditing metadata such as
 * creation and modification timestamps are inherited from {@link AuditedEntity}.
 * Deleting a project cascades to its tickets due to {@link CascadeType#ALL} with
 * {@code orphanRemoval=true}.
 * </p>
 */
@Entity
@Setter
@Getter
public class Project extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String title;
    private String description;
    private String abbreviation;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Ticket> tickets = new ArrayList<>();
}
