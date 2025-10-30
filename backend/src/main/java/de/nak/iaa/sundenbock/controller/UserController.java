package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.dto.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.auth.RegistrationRequest;
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

    /**
     * Controller method to create a new user.
     *
     * @param request A RegistrationRequest object containing the details of the new user.
     * @return A UserDetailDTO object representing the newly created user.
     */
    @PostMapping
    public UserDetailDTO createUser(@RequestBody RegistrationRequest request) {
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




    // Not in use rn
    @PutMapping("/{username}/roles/{roleId}")
    public void assignRoleToUser(@PathVariable String username, @PathVariable Long roleId) {
        userService.assignRoleToUser(username, roleId);
    }

    @PutMapping("/{username}/permissions/{permissionName}")
    public void assignPermissionToUser(@PathVariable String username, @PathVariable String permissionName) {
        userService.assignPermissionToUser(username, permissionName);
    }
}
