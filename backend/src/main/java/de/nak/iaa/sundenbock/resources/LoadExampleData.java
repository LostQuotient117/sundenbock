package de.nak.iaa.sundenbock.resources;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Component responsible for loading example data into the application.
 * <p>
 * This class implements CommandLineRunner and executes data loading
 * routines on application startup, but only when the datasource URL
 * is configured for an in-memory H2 database.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "spring.datasource.url", havingValue = "jdbc:h2:mem:mydb")
public class LoadExampleData implements CommandLineRunner {

    private final LoadBaselineSecurityData loadBaselineSecurityData;
    private final LoadSampleUsersData loadSampleUsersData;
    private final LoadProjectData loadProjectData;
    private final LoadTicketAndCommentData loadTicketAndCommentData;

    public LoadExampleData(LoadBaselineSecurityData loadBaselineSecurityData, LoadSampleUsersData loadSampleUsersData, LoadProjectData loadProjectData, LoadTicketAndCommentData loadTicketAndCommentData) {
        this.loadBaselineSecurityData = loadBaselineSecurityData;
        this.loadSampleUsersData = loadSampleUsersData;
        this.loadProjectData = loadProjectData;
        this.loadTicketAndCommentData = loadTicketAndCommentData;
    }

    @Override
    public void run(String... args) {
        loadBaselineSecurityData.run();
        loadSampleUsersData.run();
        loadProjectData.run();
        loadTicketAndCommentData.run();
    }
}
