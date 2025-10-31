package de.nak.iaa.sundenbock.dto.projectDTO;

import de.nak.iaa.sundenbock.model.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ProjectDTO(
     Long id,
     @NotBlank(message = "Title must not be empty")
     @Size(max = 200, message = "Title must not exceed 200 characters")
     String title,
     @NotBlank(message = "Description must not be empty")
     @Size(max = 2000, message = "Description must not exceed 2000 characters")
     String description,
     //!-- from AuditedEntity --!
     Instant createdDate,
     Instant lastModifiedDate,
     User createdBy,
     User lastModifiedBy
) {}
