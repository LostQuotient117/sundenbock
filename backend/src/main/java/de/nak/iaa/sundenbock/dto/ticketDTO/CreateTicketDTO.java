package de.nak.iaa.sundenbock.dto.ticketDTO;

import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object used to create a new ticket.
 * <p>
 * This immutable record captures the minimal information required by the API
 * to create a ticket and relies on Jakarta Bean Validation to validate incoming
 * requests (for example, non-blank title/description, maximum lengths, and required
 * status, responsible user, and project identifier).
 * <p>
 * Instances are typically produced by the web layer when deserializing a request body
 * and consumed by the service layer to create the corresponding domain entity.
 */
public record CreateTicketDTO(
        @NotBlank(message = "Title must not be empty")
        @Size(max = 200, message = "The title must not exceed 200 characters.")
        String title,
        @NotBlank(message = "Description must not be empty")
        @Size(max = 2000, message = "The description must not exceed 2000 characters")
        String description,
        @NotNull(message = "A status must be selected")
        TicketStatus status,
        String responsiblePersonUserName,
        @NotNull(message = "A associated project must be selected")
        Long projectId
) {}
