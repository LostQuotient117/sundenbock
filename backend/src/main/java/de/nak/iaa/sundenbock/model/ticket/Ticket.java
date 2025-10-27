package de.nak.iaa.sundenbock.model.ticket;

import de.nak.iaa.sundenbock.model.Comment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    public String Title;
    public String Description; //TODO: Add Title? Pictures (maybe Blob?)?
    public TicketStatus Status;
    public LocalDateTime LastChange;
    public LocalDateTime CreatedOn;
    public String ResponsiblePerson; //TODO: Placehoilder for user-class as datatype
    public String Author; //TODO: Placehoilder for user-class as datatype
    public String Project; //TODO: Placeholer for project-class as datatype
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();


}
