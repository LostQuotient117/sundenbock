package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.ProjectDTO;
import de.nak.iaa.sundenbock.model.project.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectDTO toProjectDTO(Project project);
    Project toProject(ProjectDTO projectDTO);
}
