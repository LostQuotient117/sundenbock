package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper responsible for converting between {@link Ticket} entities and their DTO
 * representations.
 * <p>
 * Component model is Spring, so the generated implementation is a Spring bean and can be injected
 * where needed. This mapper delegates user conversions to {@link UserMapper} when applicable.
 * </p>
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TicketMapper {

    /**
     * Maps a {@link Ticket} entity to a {@link TicketDTO}.
     *
     * @param ticket the entity to map; may be {@code null}
     * @return the mapped DTO or {@code null} if the input was {@code null}
     */
    TicketDTO toTicketDTO(Ticket ticket);

    /**
     * Maps a {@link CreateTicketDTO} to a new {@link Ticket} entity instance.
     * Fields not provided by the DTO (e.g. generated identifiers, audit fields) are left to be
     * handled by the persistence layer or calling service.
     *
     * @param createTicketDTO the DTO carrying the data for a new ticket; may be {@code null}
     * @return a new {@link Ticket} populated from the DTO or {@code null} if the input was {@code null}
     */
    Ticket toTicketFromCreate(CreateTicketDTO createTicketDTO);

    /**
     * Updates an existing {@link Ticket} entity with values from a {@link TicketDTO}.
     * <p>
     * MapStruct will copy non-null values from the DTO into the target entity. Fields absent or
     * {@code null} in the DTO will not override the existing values unless configured otherwise.
     * </p>
     *
     * @param ticketDTO      the source DTO (may be {@code null})
     * @param existingTicket the target entity to update; must not be {@code null}
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    void updateTicketFromDTO(TicketDTO ticketDTO, @MappingTarget Ticket existingTicket);

    List<TicketDTO> toTicketDTOs(List<Ticket> ticket);
}
