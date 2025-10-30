package de.nak.iaa.sundenbock.model.ticket;

import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //TODO: For auto-increment (the question is whether it can also be used as an ID that is visible. Otherwise, maybe limit the length?
    private Long id;
    @Column(nullable = false)
    public String Title;
    public String Description; //TODO: Add Title? Pictures (maybe Blob?)?
    public TicketStatus Status;
    @LastModifiedBy
    @Column(nullable = false)
    public LocalDateTime LastChange;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    public LocalDateTime CreatedOn;
    @ManyToOne(optional = false)
    public User ResponsiblePerson; //TODO: Placehoilder for user-class as datatype
    @ManyToOne(optional = false)
    public User Author; //TODO: Placehoilder for user-class as datatype
    @ManyToOne(optional = false)
    public Project Project; //TODO: Placeholer for project-class as datatype
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();


}
