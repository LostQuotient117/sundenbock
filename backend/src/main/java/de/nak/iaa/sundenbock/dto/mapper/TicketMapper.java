package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.UserRepository;
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
@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class})
public interface TicketMapper {

    /**
     * Maps a {@link Ticket} entity to a {@link TicketDTO}.
     *
     * @param ticket the entity to map; may be {@code null}
     * @return the mapped DTO or {@code null} if the input was {@code null}
     */
    @Mapping(target = "project", source = "project", qualifiedByName = "toProjectWithoutTicketsDTO")
    TicketDTO toTicketDTO(Ticket ticket);

    /**
     * Maps a {@link CreateTicketDTO} to a new {@link Ticket} entity instance.
     * Fields not provided by the DTO (e.g. generated identifiers, audit fields) are left to be
     * handled by the persistence layer or calling service.
     *
     * @param createTicketDTO the DTO carrying the data for a new ticket; may be {@code null}
     * @return a new {@link Ticket} populated from the DTO or {@code null} if the input was {@code null}
     */
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "responsiblePerson", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "ticketKey", ignore = true)
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
    @Mapping(target = "ticketKey", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "responsiblePerson", source = "responsiblePerson", qualifiedByName = "mapUser")
    void updateTicketFromDTO(TicketDTO ticketDTO, @MappingTarget Ticket existingTicket, @Context UserRepository userRepository);

    /**
     * Converts a list of {@link Ticket} entities into a list of {@link TicketDTO} objects.
     * <p>
     * MapStruct will automatically implement this method by iterating over the source
     * list and applying the {@code toTicketDTO(Ticket)} mapping (defined elsewhere
     * in this mapper) to each element.
     *
     * @param ticket The list of {@link Ticket} entities to be converted.
     * @return A list of the corresponding {@link TicketDTO} objects.
     */
    List<TicketDTO> toTicketDTOs(List<Ticket> ticket);

    /**
     * A custom MapStruct qualifier method to resolve a {@link UserDTO} into a
     * managed {@link User} entity by fetching it from the database.
     * <p>
     * This method is intended to be used via {@code qualifiedByName = "mapUser"}
     * in other {@code @Mapping} annotations. It allows the mapper to
     * convert a simple DTO reference (containing just a username) into a
     * complete entity that can be associated with another entity (e.g., setting
     * a {@code Ticket}'s responsible person).
     *
     * @param userDTO        The input DTO, used to retrieve the {@code username}.
     * @param userRepository The {@link UserRepository} injected by MapStruct via
     * {@code @Context} to perform the database lookup.
     * @return The found {@link User} entity, or {@code null} if the input {@code userDTO}
     * or its {@code username} was {@code null}.
     * @throws ResourceNotFoundException if the {@code username} is provided but no
     * matching user exists in the repository.
     */
    @Named("mapUser")
    default User mapUser(UserDTO userDTO, @Context UserRepository userRepository) {
        if (userDTO == null) return null;
        if (userDTO.username() != null) {
            return userRepository.findByUsername(userDTO.username())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + userDTO.username()));
        }
        return null;
    }
}
