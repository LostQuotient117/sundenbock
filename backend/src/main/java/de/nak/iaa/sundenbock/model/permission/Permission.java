package de.nak.iaa.sundenbock.model.permission;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Permission extends AuditedEntity {

    @Id
    private String name; // Sowas wie "TICKET_EDIT" oder andere permission
}
