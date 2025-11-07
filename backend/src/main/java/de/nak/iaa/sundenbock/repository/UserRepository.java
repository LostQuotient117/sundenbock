package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link User} entities.
 * <p>
 * Provides CRUD operations and derived queries for user lookups and constraints.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a username is already taken.
     * @param username the username to check
     * @return {@code true} if a user with this username exists; otherwise {@code false}
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email address is already taken.
     * @param email the email to check
     * @return {@code true} if a user with this email exists; otherwise {@code false}
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by username.
     * @param username the username of the user to delete
     */
    void deleteByUsername(String username);

    /**
     * Counts how many users are directly assigned the specified permission.
     * @param permissionName the name of the permission
     * @return the number of users with this permission
     */
    long countByPermissions_Name(String permissionName);

    /**
     * Counts how many users are assigned the specified role.
     * @param roleId the ID of the role
     * @return the number of users with this role
     */
    long countByRoles_Id(Long roleId);
}
