package de.nak.iaa.sundenbock.dto.projectDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object used to create a new project.
 * <p>
 * This immutable record captures the minimal information required by the API to create a project
 * and relies on Jakarta Bean Validation to enforce basic constraints on incoming requests.
 * Instances are typically produced by the web layer when deserializing a request body and
 * consumed by the service layer to create the corresponding domain entity.
 * <p>
 */
public record CreateProjectDTO(
        @NotBlank(message = "Title must not be empty")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        @NotBlank(message = "Description must not be empty")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,
        @NotBlank(message = "Abbreviation must not be empty")
        @Size(min = 3, max = 3)
        String abbreviation
) {}
