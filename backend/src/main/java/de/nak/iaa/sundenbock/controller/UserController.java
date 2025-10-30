package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.dto.UserDetailDTO;
import de.nak.iaa.sundenbock.dto.auth.AuthenticationResponse;
import de.nak.iaa.sundenbock.dto.auth.RegistrationRequest;
import de.nak.iaa.sundenbock.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all-users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{username}")
    public UserDTO getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/{username}/details")
    public UserDetailDTO getDetailedUserByUsername(@PathVariable String username) {
        return userService.getDetailedUserByUsername(username);
    }

    @PutMapping("/{username}/update")
    public UserDetailDTO updateUser(@PathVariable String username, @RequestBody UserDetailDTO userDetailDTO) {
        return userService.updateUser(username, userDetailDTO);
    }

    @PostMapping
    public UserDetailDTO createUser(@RequestBody RegistrationRequest request) {
        return userService.createUser(request);
    }

    @DeleteMapping("/{username}/delete")
    public void deleteUserByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
    }





    // Not in use rn
    /**
     * Weist einem Benutzer eine Rolle zu.
     * HTTP-Methode: PUT
     * Endpunkt: /api/users/{userId}/roles/{roleId}
     *
     * @param username Username des Benutzers, dem die Rolle zugewiesen werden soll.
     * @param roleId Die ID der zuzuweisenden Rolle.
     */
    @PutMapping("/{username}/roles/{roleId}")
    public void assignRoleToUser(@PathVariable String username, @PathVariable Long roleId) {
        userService.assignRoleToUser(username, roleId);
    }

    /**
     * Weist einem Benutzer eine einzelne Berechtigung zu.
     * HTTP-Methode: PUT
     * Endpunkt: /api/users/{userId}/permissions/{permissionName}
     *
     * @param username Username des Benutzers, dem die Berechtigung zugewiesen werden soll.
     * @param permissionName Der Name der zuzuweisenden Berechtigung.
     */
    @PutMapping("/{username}/permissions/{permissionName}")
    public void assignPermissionToUser(@PathVariable String username, @PathVariable String permissionName) {
        userService.assignPermissionToUser(username, permissionName);
    }
}
