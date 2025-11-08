package de.nak.iaa.sundenbock.model.role;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import de.nak.iaa.sundenbock.model.permission.Permission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

/**
 * Entity representing a Role (a group of permissions).
 * <p>
 * Roles are assigned to users and grant them a set of permissions
 * defined in the 'permissions' collection.
 */
@Entity
@Getter
@Setter
public class Role extends AuditedEntity {

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";
    public static final String DEVELOPER = "ROLE_DEVELOPER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    private Set<Permission> permissions;

}
