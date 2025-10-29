package de.nak.iaa.sundenbock.dto;

import de.nak.iaa.sundenbock.model.user.User;

import java.time.LocalDateTime;

public record ProjectDTO(
     Long id,
     String title,
     String description,
     LocalDateTime createdOn,
     User createdBy
) {}
