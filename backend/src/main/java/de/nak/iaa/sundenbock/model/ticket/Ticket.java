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

@Entity
@Setter
@Getter
public class Ticket extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //TODO: For auto-increment (the question is whether it can also be used as an ID that is visible. Otherwise, maybe limit the length?
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description; //TODO: Add Title? Pictures (maybe Blob?)?
    private TicketStatus status;
    @ManyToOne(optional = false)
    private User responsiblePerson; //TODO: Placehoilder for user-class as datatype
    @ManyToOne(optional = false)
    private Project project; //TODO: Placeholer for project-class as datatype
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
