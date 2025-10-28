package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.user.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
