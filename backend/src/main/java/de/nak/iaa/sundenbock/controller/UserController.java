package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.dto.UserDetailDTO;
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
     * Ruft eine Liste aller Benutzer ab.
     * HTTP-Methode: GET
     * Endpunkt: /api/users/all-users
     *
     * @return Eine Liste von UserDTOs, die alle Benutzer repräsentieren.
     */
    @GetMapping("/all-users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Ruft detaillierte Informationen zu einem Benutzer anhand seiner ID ab, einschließlich seiner Rollen und Berechtigungen.
     * HTTP-Methode: GET
     * Endpunkt: /api/users/{id}/details
     *
     * @param id Die ID des abzurufenden Benutzers.
     * @return Ein UserDetailDTO mit den vollständigen Informationen des Benutzers.
     */
    @GetMapping("/{id}/details")
    public UserDetailDTO getDetailedUserById(@PathVariable Long id) {
        return userService.getDetailedUserById(id);
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * Weist einem Benutzer eine Rolle zu.
     * HTTP-Methode: PUT
     * Endpunkt: /api/users/{userId}/roles/{roleId}
     *
     * @param userId Die ID des Benutzers, dem die Rolle zugewiesen werden soll.
     * @param roleId Die ID der zuzuweisenden Rolle.
     */
    @PutMapping("/{userId}/roles/{roleId}")
    public void assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        userService.assignRoleToUser(userId, roleId);
    }

    /**
     * Weist einem Benutzer eine einzelne Berechtigung zu.
     * HTTP-Methode: PUT
     * Endpunkt: /api/users/{userId}/permissions/{permissionName}
     *
     * @param userId         Die ID des Benutzers, dem die Berechtigung zugewiesen werden soll.
     * @param permissionName Der Name der zuzuweisenden Berechtigung.
     */
    @PutMapping("/{userId}/permissions/{permissionName}")
    public void assignPermissionToUser(@PathVariable Long userId, @PathVariable String permissionName) {
        userService.assignPermissionToUser(userId, permissionName);
    }
}
