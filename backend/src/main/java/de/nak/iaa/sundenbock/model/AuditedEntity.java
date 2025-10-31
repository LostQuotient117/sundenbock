package de.nak.iaa.sundenbock.model;

import de.nak.iaa.sundenbock.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditedEntity {
    @CreatedDate @Column(nullable = false, updatable = false)
    @NotNull(message = "The date/time for 'created date' must not be empty")
    private Instant createdDate;
    @LastModifiedDate @Column(nullable = false)
    @NotNull(message = "The date/time for 'last modified date' must not be empty")
    private Instant lastModifiedDate;
    @CreatedBy
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id")
    @NotNull(message = "'Created by' must not be empty")
    private User createdBy;
    @LastModifiedBy
    @ManyToOne(optional = false)
    @JoinColumn(name = "last_modified_by_id")
    @NotNull(message = "'Last modified by' must not be empty")
    private User lastModifiedBy;
}
