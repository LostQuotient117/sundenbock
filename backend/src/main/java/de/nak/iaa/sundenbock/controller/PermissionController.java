package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.PermissionDTO;
import de.nak.iaa.sundenbock.service.PermissionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Ruft eine Liste aller verfügbaren Berechtigungen ab.
     * HTTP-Methode: GET
     * Endpunkt: /api/permissions
     *
     * @return Eine Liste von PermissionDTOs, die alle Berechtigungen repräsentieren.
     */
    @GetMapping
    public List<PermissionDTO> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    /**
     * Erstellt eine neue Berechtigung.
     * HTTP-Methode: POST
     * Endpunkt: /api/permissions
     */
    @PostMapping("/create")
    public PermissionDTO createPermission(@RequestBody PermissionDTO permissionDTO) {
        return permissionService.createPermission(permissionDTO);
    }
}
