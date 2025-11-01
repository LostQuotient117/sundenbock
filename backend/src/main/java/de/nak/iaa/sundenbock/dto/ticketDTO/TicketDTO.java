package de.nak.iaa.sundenbock.dto.ticketDTO;

import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record TicketDTO(
        Long id,
        @NotBlank(message = "Title must not be empty")
        @Size(max = 200, message = "The title must not exceed 200 characters.")
        String title,
        @NotBlank(message = "Description must not be empty")
        @Size(max = 2000, message = "The description must not exceed 2000 characters")
        String description,
        @NotNull(message = "A status must be selected")
        TicketStatus status,
        @NotNull(message = "A responsible person must be selected")
        UserDTO responsiblePerson,
        @NotNull(message = "A associated project must be selected")
        ProjectDTO project,
        //!-- from AuditedEntity --!
        Instant createdDate,
        Instant lastModifiedDate,
        UserDTO createdBy,
        UserDTO lastModifiedBy
) {}
