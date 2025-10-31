package de.nak.iaa.sundenbock.model.comment;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdDate ASC")
    private List<Comment> comments = new ArrayList<>();
    @ManyToOne
    private Comment parentComment;
    private String commentText; //TODO: pictures? (blob maybe)
    private int likes;
    private int dislikes;
}

