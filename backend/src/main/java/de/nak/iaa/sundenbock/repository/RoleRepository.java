package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link Role} entities.
 * <p>
 * Provides CRUD operations and derived queries for role lookups and statistics.
 * </p>
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its unique name.
     * @param name the role name
     * @return an {@link Optional} containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Counts how many roles include the specified permission.
     * @param permissionName the name of the permission
     * @return the number of roles with this permission
     */
    long countByPermissions_Name(String permissionName);
}
