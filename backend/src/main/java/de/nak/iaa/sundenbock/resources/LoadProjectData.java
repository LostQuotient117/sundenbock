package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Component responsible for loading example project data into the database.
 * <p>
 * Checks if any projects exist and, if none are found, creates and saves
 * a set of example projects with predefined titles and descriptions.
 * </p>
 */
@Component
public class LoadProjectData {

    private final ProjectRepository projectRepository;

    public LoadProjectData(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    @Transactional
    public void run() {
        if (projectRepository.count() == 0) {

            Project p1 = new Project();
            p1.setTitle("Ticket System");
            p1.setDescription("Internes Tool zur Verwaltung und Nachverfolgung von Fehlern und Feature-Requests.");

            Project p2 = new Project();
            p2.setTitle("Customer Portal");
            p2.setDescription("Web-Portal, das Kunden erlaubt, Bestellungen und Supportanfragen einzusehen und zu verwalten.");

            Project p3 = new Project();
            p3.setTitle("Data Analytics Platform");
            p3.setDescription("Interne Plattform zur Analyse und Visualisierung von Geschäftsdaten für das Management.");

            projectRepository.save(p1);
            projectRepository.save(p2);
            projectRepository.save(p3);

            System.out.println("Example projects created!");
        }
    }
}
