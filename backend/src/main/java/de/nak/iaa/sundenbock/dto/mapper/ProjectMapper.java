package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.model.project.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for converting between {@link Project} entities and their DTO representations.
 * <p>
 * Component model is Spring, exposing the generated implementation as an injectable bean. Any
 * user-related nested mappings are delegated to {@link UserMapper}.
 * </p>
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ProjectMapper {
    /**
     * Maps a {@link Project} entity to a {@link ProjectDTO}.
     *
     * @param project the entity to map; may be {@code null}
     * @return the mapped DTO or {@code null} if the input was {@code null}
     */
    ProjectDTO toProjectDTO(Project project);

    /**
     * Maps a {@link CreateProjectDTO} to a new {@link Project} entity instance.
     * Fields that are not supplied by the DTO (e.g. generated ids, audit fields) are expected to be
     * handled by the persistence layer or calling service.
     *
     * @param createProjectDTO the DTO carrying the data for a new project; may be {@code null}
     * @return a new {@link Project} populated from the DTO or {@code null} if the input was {@code null}
     */
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    Project toProjectForCreate(CreateProjectDTO createProjectDTO);

    /**
     * Converts a list of {@link Project} entities into a list of {@link ProjectDTO} objects.
     * <p>
     * MapStruct will automatically implement this method by iterating over the source
     * list and applying the corresponding single-object mapping method (e.g.,
     * {@code toProjectDTO(Project)}) to each element.
     *
     * @param projects The list of {@link Project} entities to be converted.
     * @return A list containing the corresponding {@link ProjectDTO} objects.
     */
    List<ProjectDTO> toProjectDTOs(List<Project> projects);

    /**
     * Converts a {@link Project} entity to a {@link ProjectDTO}, specifically
     * <strong>omitting</strong> the list of associated tickets.
     * <p>
     * This method is registered as a MapStruct qualifier via {@code @Named}
     * with the name "toProjectWithoutTicketsDTO".
     * <p>
     * Its primary purpose is to be used by other mappers (e.g., {@code TicketMapper})
     * when mapping a ticket's associated project reference. This prevents
     * circular mapping dependencies and infinite recursion (e.g., Ticket -> Project -> Tickets).
     *
     * @param project The source {@link Project} entity to convert.
     * @return The {@link ProjectDTO}, intentionally without the {@code tickets} collection.
     */
    @Named("toProjectWithoutTicketsDTO")
    ProjectDTO toProjectWithoutTickets(Project project);
}
