package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TicketMapper {
    TicketDTO toTicketDTO(Ticket ticket);
    Ticket toTicketFromCreate(CreateTicketDTO createTicketDTO);

    void updateTicketFromDTO(TicketDTO ticketDTO, @MappingTarget Ticket existingTicket);
}
