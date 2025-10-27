package de.nak.iaa.sundenbock.model.comment;

import de.nak.iaa.sundenbock.model.ticket.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //--Hierarchy for CommentToTicket--
    @ManyToOne
    public Ticket ticket;
    //--Hierarchy for Comments--
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("CreatedOn ASC")
    private List<Comment> comments = new ArrayList<>();
    @ManyToOne
    private Comment parentComment;
    public String Author; //TODO: Placeholder for user-class
    @CreatedDate
    @Column(nullable = false, updatable = false)
    public LocalDateTime CreatedOn;
    @LastModifiedDate
    @Column(nullable = false)
    public LocalDateTime LastChange;
    public String CommentText; //TODO: pictures? (blob maybe)
    public int Likes;
    public int Dislikes;
}

