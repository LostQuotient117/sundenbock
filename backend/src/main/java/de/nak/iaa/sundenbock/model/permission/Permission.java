package de.nak.iaa.sundenbock.model.permission;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a single authority or permission (e.g., "TICKET_UPDATE").
 * <p>
 * Permissions are the most granular level of security and can be assigned
 * directly to users or grouped into Roles. The 'name' field serves as
 * the primary key.
 */
@Entity
@Getter
@Setter
public class Permission extends AuditedEntity {

    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";

    @Id
    private String name;
}
