package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
