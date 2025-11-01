package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.dto.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.auth.ChangePasswordRequest;
import de.nak.iaa.sundenbock.dto.userDTO.CreateUserDTO;
import de.nak.iaa.sundenbock.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Controller method to retrieve all users.
     *
     * @return A list of UserDTO objects representing all users.
     */
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Controller method to retrieve a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return A UserDTO object representing the user with the specified username.
     */
    @GetMapping("/{username}")
    public UserDTO getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * Controller method to retrieve detailed information about a user by their username.
     *
     * @param username The username of the user to retrieve details for.
     * @return A UserDetailDTO object containing detailed information about the user.
     */
    @GetMapping("/{username}/details")
    public UserDetailDTO getDetailedUserByUsername(@PathVariable String username) {
        return userService.getDetailedUserByUsername(username);
    }

    /**
     * Controller method to update a user's details.
     *
     * @param username The username of the user to update.
     * @param userDetailDTO A UserDetailDTO object containing the updated user details.
     * @return A UserDetailDTO object representing the updated user.
     */
    @PutMapping("/{username}/update")
    public UserDetailDTO updateUser(@PathVariable String username, @RequestBody UserDetailDTO userDetailDTO) {
        return userService.updateUser(username, userDetailDTO);
    }

    // administrative user creation
    /**
     * Controller method to create a new user.
     *
     * @param request A RegistrationRequest object containing the details of the new user.
     * @return A UserDetailDTO object representing the newly created user.
     */
    @PostMapping("/create")
    public UserDetailDTO createUser(@RequestBody CreateUserDTO request) {
        return userService.createUser(request);
    }

    /**
     * Controller method to delete a user by their username.
     *
     * @param username The username of the user to delete.
     */
    @DeleteMapping("/{username}/delete")
    public void deleteUserByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
    }

    /**
     * Allows the currently authenticated user to change their own password.
     * HTTP Method: PUT
     * Endpoint: /api/users/change-password
     *
     * @param request The request body containing the old and new passwords.
     */
    @PutMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
    }

    /**
     * Assigns a role to a user.
     * HTTP Method: PUT
     * Endpoint: /api/v1/users/{username}/roles/{roleName}
     *
     * @param username The username of the user to whom the role will be assigned.
     * @param roleName Role name of the role to assign to the user.
     */
    @PutMapping("/{username}/roles/{roleName}")
    public void assignRoleToUser(@PathVariable String username, @PathVariable String roleName) {
        userService.assignRoleToUser(username, roleName);
    }

    /**
     * Removes a role from a user.
     * HTTP Method: DELETE
     * Endpoint: /api/v1/users/{username}/roles/{roleName}
     */
    @DeleteMapping("/{username}/roles/{roleName}")
    public void removeRoleFromUser(@PathVariable String username, @PathVariable String roleName) {
        userService.removeRoleFromUser(username, roleName);
    }

    /**
     * Assigns a permission to a user.
     * HTTP Method: PUT
     * Endpoint: /api/v1/users/{username}/permissions/{permissionName}
     *
     * @param username The username of the user to whom the permission will be assigned.
     * @param permissionName The name of the permission to assign to the user.
     */
    @PutMapping("/{username}/permissions/{permissionName}")
    public void assignPermissionToUser(@PathVariable String username, @PathVariable String permissionName) {
        userService.assignPermissionToUser(username, permissionName);
    }

    /**
     * Removes a direct permission from a user.
     * HTTP Method: DELETE
     * Endpoint: /api/v1/users/{username}/permissions/{permissionName}
     */
    @DeleteMapping("/{username}/permissions/{permissionName}")
    public void removePermissionFromUser(@PathVariable String username, @PathVariable String permissionName) {
        userService.removePermissionFromUser(username, permissionName);
    }
}
