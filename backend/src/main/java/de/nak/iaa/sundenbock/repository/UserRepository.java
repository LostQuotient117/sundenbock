package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteByUsername(String username);

    /**
     * Counts how many users are directly assigned the specified permission.
     * @param permissionName The name of the permission.
     * @return The number of users with this permission.
     */
    long countByPermissions_Name(String permissionName);

    /**
     * Counts how many users are assigned the specified role.
     * @param roleId The ID of the role.
     * @return The number of users with this role.
     */
    long countByRoles_Id(Long roleId);
}
