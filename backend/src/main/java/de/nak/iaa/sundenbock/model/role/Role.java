package de.nak.iaa.sundenbock.model.role;

import de.nak.iaa.sundenbock.model.AuditedEntity;
import de.nak.iaa.sundenbock.model.permission.Permission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Entity
@Getter
@Setter
public class Role extends AuditedEntity {

    // quasi group of permissions

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // zB "DEVELOPER"

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    private Set<Permission> permissions;

}
