package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.permission.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Permission} entities.
 * <p>
 * Provides CRUD access to permissions by their name (as the ID).
 * </p>
 */
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
