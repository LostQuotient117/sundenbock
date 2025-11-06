package de.nak.iaa.sundenbock.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating a User via PUT.
 * This DTO allows partial updates. Any field provided as null will be ignored.
 */
@Data
public class UpdateUserDTO {

    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    private Boolean enabled;
}