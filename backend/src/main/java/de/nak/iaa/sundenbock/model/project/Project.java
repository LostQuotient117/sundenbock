package de.nak.iaa.sundenbock.model.project;

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
public class Project extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String title;
    private String description;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Ticket> tickets = new ArrayList<>();
}
