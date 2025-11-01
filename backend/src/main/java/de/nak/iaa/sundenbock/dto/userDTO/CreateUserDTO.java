package de.nak.iaa.sundenbock.dto.userDTO;

public record CreateUserDTO(
        String username,
        String email,
        String password
) {}
