package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.auth.ChangePasswordRequest;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing user management endpoints.
 * <p>
 * Includes endpoints for listing users, retrieving details, creating, updating, deleting users,
 * and managing roles/permissions assigned to users.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all users.
     *
     * @return a list of {@link UserDTO} representing all users
     */
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Retrieves a user by username.
     *
     * @param username the username to look up
     * @return a {@link UserDTO} for the specified username
     */
    @GetMapping("/{username}")
    public UserDTO getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * Retrieves detailed information for a user by username.
     *
     * @param username the username to look up
     * @return a {@link UserDetailDTO} with detailed user information
     */
    @GetMapping("/{username}/details")
    public UserDetailDTO getDetailedUserByUsername(@PathVariable String username) {
        return userService.getDetailedUserByUsername(username);
    }

    /**
     * Updates a user's details.
     *
     * @param username      the username of the user to update
     * @param userDetailDTO the new user details
     * @return the updated {@link UserDetailDTO}
     */
    @PutMapping("/{username}/update")
    public UserDetailDTO updateUser(@Valid @PathVariable String username, @RequestBody UserDetailDTO userDetailDTO) {
        return userService.updateUser(username, userDetailDTO);
    }

    /**
     * Administrative endpoint to create a new user.
     *
     * @param request the create user DTO
     * @return the created {@link UserDetailDTO}
     */
    @PostMapping("/create")
    public UserDetailDTO createUser(@Valid @RequestBody CreateUserDTO request) {
        return userService.createUser(request);
    }

    /**
     * Deletes a user by username.
     *
     * @param username the username of the user to delete
     */
    @DeleteMapping("/{username}/delete")
    public void deleteUserByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
    }

    /**
     * Allows the currently authenticated user to change their own password.
     *
     * @param request the change password request containing old and new passwords
     */
    @PutMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
    }

    /**
     * Assigns a role to a user.
     *
     * @param username the username
     * @param roleName the role name to assign
     */
    @PutMapping("/{username}/roles/{roleName}")
    public void assignRoleToUser(@PathVariable String username, @PathVariable String roleName) {
        userService.assignRoleToUser(username, roleName);
    }

    /**
     * Removes a role from a user.
     *
     * @param username the username
     * @param roleName the role name to remove
     */
    @DeleteMapping("/{username}/roles/{roleName}")
    public void removeRoleFromUser(@PathVariable String username, @PathVariable String roleName) {
        userService.removeRoleFromUser(username, roleName);
    }

    /**
     * Assigns a direct permission to a user.
     *
     * @param username       the username
     * @param permissionName the permission name to assign
     */
    @PutMapping("/{username}/permissions/{permissionName}")
    public void assignPermissionToUser(@PathVariable String username, @PathVariable String permissionName) {
        userService.assignPermissionToUser(username, permissionName);
    }

    /**
     * Removes a direct permission from a user.
     *
     * @param username       the username
     * @param permissionName the permission name to remove
     */
    @DeleteMapping("/{username}/permissions/{permissionName}")
    public void removePermissionFromUser(@PathVariable String username, @PathVariable String permissionName) {
        userService.removePermissionFromUser(username, permissionName);
    }
}
