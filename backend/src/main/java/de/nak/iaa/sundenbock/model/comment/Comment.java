package de.nak.iaa.sundenbock.model.comment;

import de.nak.iaa.sundenbock.model.ticket.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //TODO: For auto-increment (the question is whether it can also be used as an ID that is visible. Otherwise, maybe limit the length?
    private Long id;
    //--Hierarchy for CommentToTicket--
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    public Ticket ticket;
    //--Hierarchy for Comments--
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>(); //TODO: for Comments to Comments
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    public String Author; //TODO: Placeholder for user-class
    public LocalDateTime CreatedOn;
    public String CommentText; //TODO: pictures? (blob maybe)
    public int Likes;
    public int Dislikes;
}

