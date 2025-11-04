package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByTitle(String Title);

    boolean existsByTitle(String title);
}
