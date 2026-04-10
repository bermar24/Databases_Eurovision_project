package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.AdminRequestDTO;
import com.dhbw.eurovision.dto.response.AdminResponseDTO;
import com.dhbw.eurovision.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Admin.
 * Base path: /api/admins
 */
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** GET /api/admins */
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    /** GET /api/admins/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    /** POST /api/admins */
    @PostMapping
    public ResponseEntity<AdminResponseDTO> createAdmin(@RequestBody AdminRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(dto));
    }

    /**
     * POST /api/admins/{adminId}/shows/{showId}
     * Assigns an Admin to manage a Show — the EERM "Manage" M:N relationship.
     */
    @PostMapping("/{adminId}/shows/{showId}")
    public ResponseEntity<AdminResponseDTO> assignShow(
            @PathVariable Long adminId,
            @PathVariable Long showId) {
        return ResponseEntity.ok(adminService.assignShowToAdmin(adminId, showId));
    }

    /** DELETE /api/admins/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
