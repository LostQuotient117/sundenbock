package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.roleDTO.CreateRoleDTO;
import de.nak.iaa.sundenbock.dto.roleDTO.RoleDTO;
import de.nak.iaa.sundenbock.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Ruft eine Liste aller verfügbaren Rollen ab.
     * HTTP-Methode: GET
     * Endpunkt: /api/roles
     *
     * @return Eine Liste von RoleDTOs, die alle Rollen repräsentieren.
     */
    @GetMapping
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

    /**
     * Erstellt eine neue Rolle.
     * HTTP-Methode: POST
     * Endpunkt: /api/roles
     *
     * @param createRoleDTO Das CreateRoleDTO mit den Daten für die neue Rolle (Name und Berechtigungen) im Request Body.
     * @return Das erstellte RoleDTO.
     */
    @PostMapping
    public RoleDTO createRole(@Valid @RequestBody CreateRoleDTO createRoleDTO) {
        return roleService.createRole(createRoleDTO);
    }

    /**
     * Aktualisiert die Berechtigungen einer bestehenden Rolle.
     * HTTP-Methode: PUT
     * Endpunkt: /api/roles/{roleId}/permissions
     *
     * @param roleId          Die ID der zu aktualisierenden Rolle.
     * @param permissionNames Ein Set von Berechtigungsnamen, die der Rolle zugewiesen werden sollen.
     */
    @PutMapping("/{roleId}/update-permissions")
    public void updateRolePermissions(@PathVariable Long roleId, @RequestBody Set<String> permissionNames) {
        roleService.updateRolePermissions(roleId, permissionNames);
    }
}
