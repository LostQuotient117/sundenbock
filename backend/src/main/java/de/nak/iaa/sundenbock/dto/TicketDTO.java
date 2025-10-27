package de.nak.iaa.sundenbock.dto;

import de.nak.iaa.sundenbock.model.ticket.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TicketDTO(
        Long id,
        String title,
        String description,
        TicketStatus status,
        LocalDateTime lastChange,
        LocalDateTime createdOn,
        String responsiblePerson,
        String author,
        String project,
        List<CommentDTO> comments // Liste der Hauptkommentare
) {}
