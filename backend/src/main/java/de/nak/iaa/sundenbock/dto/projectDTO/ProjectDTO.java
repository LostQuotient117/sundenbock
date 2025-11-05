package de.nak.iaa.sundenbock.dto.projectDTO;

import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Data Transfer Object representing a project as exposed by the API.
 * <p>
 * This immutable record is used for reading and transferring project data across application layers
 * and includes auditing information (creation/modification timestamps and users).
 * <p>
 * Jakarta Bean Validation annotations document expected constraints for values handled by the API.
 */
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
     UserDTO createdBy,
     UserDTO lastModifiedBy
) {}
