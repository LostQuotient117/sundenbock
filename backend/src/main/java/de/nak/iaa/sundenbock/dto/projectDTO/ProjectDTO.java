package de.nak.iaa.sundenbock.dto.projectDTO;

import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
     @NotBlank(message = "Abbreviation must not be empty")
     @Size(min = 3, max = 3, message = "Abbreviation must be exactly 3 characters long")
     @Pattern(regexp = "[a-zA-Z]{3}", message = "Abbreviation must only contain letters")
     String abbreviation,
     //!-- from AuditedEntity --!
     Instant createdDate,
     Instant lastModifiedDate,
     UserDTO createdBy,
     UserDTO lastModifiedBy
) {}
