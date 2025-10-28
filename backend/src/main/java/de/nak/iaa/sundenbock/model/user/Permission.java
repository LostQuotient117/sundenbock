package de.nak.iaa.sundenbock.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Permission {

    @Id
    private String name; // Sowas wie "TICKET_EDIT" oder andere permission
}
