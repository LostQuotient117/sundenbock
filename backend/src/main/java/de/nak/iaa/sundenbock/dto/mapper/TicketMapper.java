package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.TicketDTO;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketDTO toTicketDTO(Ticket ticket);
    Ticket toTicket(TicketDTO ticketDTO);

    void updateTicketFromDTO(TicketDTO ticketDTO, @MappingTarget Ticket existingTicket);
}
