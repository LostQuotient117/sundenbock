package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.TicketDTO;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketDTO toTicketDTO(Ticket ticket);
    Ticket toTicket(TicketDTO ticketDTO);
}
