package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.auth.AdminResetPasswordDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
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
@Validated
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
    public UserDTO getUserByUsername(@PathVariable @NotBlank @Size(min = 3, max = 50) String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * Retrieves detailed information for a user by username.
     *
     * @param username the username to look up
     * @return a {@link UserDetailDTO} with detailed user information
     */
    @GetMapping("/{username}/details")
    public UserDetailDTO getDetailedUserByUsername(@PathVariable @NotBlank @Size(min = 3, max = 50) String username) {
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
    public UserDetailDTO updateUser(@PathVariable @NotBlank @Size(min = 3, max = 50) String username,
                                    @Valid  @RequestBody UserDetailDTO userDetailDTO) {
        return userService.updateUser(username, userDetailDTO);
    }

    /**
     * Administrative endpoint to create a new user.
     *
     * @param request the creation user DTO
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
    public void deleteUserByUsername(@PathVariable @NotBlank @Size(min = 3, max = 50) String username) {
        userService.deleteUserByUsername(username);
    }

    /**
     * Resets a user's password (administrative action).
     * Requires 'USER_MANAGE' authority.
     *
     * @param username the username of the user to update
     * @param request  the DTO containing the new password
     */
    @PutMapping("/{username}/reset-password")
    public void adminResetPassword(@PathVariable @NotBlank @Size(min = 3, max = 50) String username,
                                   @Valid @RequestBody AdminResetPasswordDTO request) {
        userService.adminResetPassword(username, request);
    }

    /**
     * Assigns a role to a user.
     *
     * @param username the username
     * @param roleName the role name to assign
     */
    @PutMapping("/{username}/roles/{roleName}")
    public void assignRoleToUser(@PathVariable @NotBlank @Size(min = 3, max = 50) String username,
                                 @PathVariable @NotBlank String roleName) {
        userService.assignRoleToUser(username, roleName);
    }

    /**
     * Removes a role from a user.
     *
     * @param username the username
     * @param roleName the role name to remove
     */
    @DeleteMapping("/{username}/roles/{roleName}")
    public void removeRoleFromUser(@PathVariable @NotBlank @Size(min = 3, max = 50) String username,
                                   @PathVariable @NotBlank String roleName) {
        userService.removeRoleFromUser(username, roleName);
    }

    /**
     * Assigns a direct permission to a user.
     *
     * @param username       the username
     * @param permissionName the permission name to assign
     */
    @PutMapping("/{username}/permissions/{permissionName}")
    public void assignPermissionToUser(@PathVariable @NotBlank @Size(min = 3, max = 50) String username,
                                       @PathVariable @NotBlank String permissionName) {
        userService.assignPermissionToUser(username, permissionName);
    }

    /**
     * Removes a direct permission from a user.
     *
     * @param username       the username
     * @param permissionName the permission name to remove
     */
    @DeleteMapping("/{username}/permissions/{permissionName}")
    public void removePermissionFromUser(@PathVariable @NotBlank @Size(min = 3, max = 50) String username,
                                         @PathVariable @NotBlank String permissionName) {
        userService.removePermissionFromUser(username, permissionName);
    }
}
