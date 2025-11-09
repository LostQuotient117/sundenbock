package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository for {@link Project} entities.
 * <p>
 * Provides CRUD operations and Specification support for filtering.
 * </p>
 */
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    /**
     * Finds a project by its unique title.
     * @param Title the exact project title
     * @return an {@link Optional} containing the project if found
     */
    Optional<Project> findByTitle(String Title);

    /**
     * Checks whether a project with the given title exists.
     * @param title the project title to check
     * @return {@code true} if a project exists with this title, otherwise {@code false}
     */
    boolean existsByTitle(String title);

    /**
     * Counts how many projects list this user as the creator.
     * @param user the user
     * @return the number of projects
     */
    long countByCreatedBy(User user);

    /**
     * Counts how many projects list this user as the last modifier.
     * @param user the user
     * @return the number of projects
     */
    long countByLastModifiedBy(User user);
}
