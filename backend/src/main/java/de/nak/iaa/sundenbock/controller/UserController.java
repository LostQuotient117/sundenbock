package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint: GET /api/users/all-users um liste aller user zu bekommen -> test with http://localhost:8080/api/users/all-users
    @GetMapping("/all-users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

}
