package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all-users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{userId}/roles/{roleId}")
    public void assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        userService.assignRoleToUser(userId, roleId);
    }

    @PutMapping("/{userId}/permissions/{permissionName}")
    public void assignPermissionToUser(@PathVariable Long userId, @PathVariable String permissionName) {
        userService.assignPermissionToUser(userId, permissionName);
    }

}
