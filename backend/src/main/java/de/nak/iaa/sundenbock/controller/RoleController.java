package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.RoleDTO;
import de.nak.iaa.sundenbock.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
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
     * @param roleDTO Das RoleDTO mit den Daten für die neue Rolle (Name und Berechtigungen) im Request Body.
     * @return Das erstellte RoleDTO.
     */
    @PostMapping
    public RoleDTO createRole(@RequestBody RoleDTO roleDTO) {
        return roleService.createRole(roleDTO);
    }

    /**
     * Aktualisiert die Berechtigungen einer bestehenden Rolle.
     * HTTP-Methode: PUT
     * Endpunkt: /api/roles/{roleId}/permissions
     *
     * @param roleId          Die ID der zu aktualisierenden Rolle.
     * @param permissionNames Ein Set von Berechtigungsnamen, die der Rolle zugewiesen werden sollen.
     */
    @PutMapping("/{roleId}/permissions")
    public void updateRolePermissions(@PathVariable Long roleId, @RequestBody Set<String> permissionNames) {
        roleService.updateRolePermissions(roleId, permissionNames);
    }
}
