package de.nak.iaa.sundenbock.dto.ticketDTO;

import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Data Transfer Object representing a ticket as exposed by the API.
 * <p>
 * This immutable record is used for reading and transferring ticket data across application layers.
 * It aggregates core ticket properties, the assigned user and associated project, as well as auditing
 * information (creation/modification timestamps and users).
 * <p>
 * Jakarta Bean Validation annotations document expected constraints for values handled by the API.
 */
public record TicketDTO(
        Long id,
        String ticketKey,
        @NotBlank(message = "Title must not be empty")
        @Size(max = 200, message = "The title must not exceed 200 characters.")
        String title,
        @NotBlank(message = "Description must not be empty")
        @Size(max = 2000, message = "The description must not exceed 2000 characters")
        String description,
        @NotNull(message = "A status must be selected")
        TicketStatus status,
        UserDTO responsiblePerson,
        @NotNull(message = "A associated project must be selected")
        ProjectDTO project,
        //!-- from AuditedEntity --!
        Instant createdDate,
        Instant lastModifiedDate,
        UserDTO createdBy,
        UserDTO lastModifiedBy
) {}
