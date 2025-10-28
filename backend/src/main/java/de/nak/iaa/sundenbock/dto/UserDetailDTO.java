package de.nak.iaa.sundenbock.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDetailDTO(
        Long id,
        String username,
        String email,
        boolean enabled,
        LocalDateTime createdAt,
        Set<String> roles,
        Set<String> permissions
) {
}
