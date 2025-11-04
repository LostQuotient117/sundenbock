package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    /**
     * Counts how many roles include the specified permission.
     * @param permissionName The name of the permission.
     * @return The number of roles with this permission.
     */
    long countByPermissions_Name(String permissionName);
}
