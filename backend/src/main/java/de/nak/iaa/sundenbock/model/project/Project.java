package de.nak.iaa.sundenbock.model.project;

import de.nak.iaa.sundenbock.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public String title;
    public String description;
    @CreatedDate
    @Column(nullable = false)
    public LocalDateTime createdOn;
    @ManyToOne
    public User createdBy;
}
