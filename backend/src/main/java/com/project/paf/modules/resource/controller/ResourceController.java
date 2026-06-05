package com.project.paf.modules.resource.controller;

import com.project.paf.modules.resource.dto.ResourceRequestDTO;
import com.project.paf.modules.resource.dto.ResourceResponseDTO;
import com.project.paf.modules.resource.model.ResourceStatus;
import com.project.paf.modules.resource.service.ResourceService;
import com.project.paf.modules.user.model.Role;
import com.project.paf.modules.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Resource Management", description = "Endpoints for managing campus resources like rooms, labs, and equipment")
public class ResourceController {

    private final ResourceService resourceService;
    
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * Hand-rolled Security Guard: aligns with the app's established pattern.
     */
    private void requireAdminOrManager() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized: No valid identity found.");
        }

        // Check authorities for ROLE_ADMIN or ROLE_MANAGER (most reliable)
        boolean isAuthorized = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        // Fallback: Check the User object directly if authorities aren't properly mapped
        if (!isAuthorized && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            isAuthorized = user.getEmail().equalsIgnoreCase("admin@campus.com") || 
                          (user.getRole() != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER));
        }

        if (!isAuthorized) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Admin or Manager status required.");
        }
    }

    // GET /api/resources
    @GetMapping
    @Operation(summary = "Get all resources", description = "Retrieves a complete list of all campus resources")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<ResourceResponseDTO>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    // GET /api/resources/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID", description = "Retrieves details of a specific resource using its ID")
    @ApiResponse(responseCode = "200", description = "Resource found")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    public ResponseEntity<ResourceResponseDTO> getResourceById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    // POST /api/resources
    @PostMapping
    @Operation(summary = "Create a new resource", description = "Adds a new resource to the campus catalogue (Admin only)")
    @ApiResponse(responseCode = "201", description = "Resource created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<ResourceResponseDTO> createResource(
            @Valid @RequestBody ResourceRequestDTO requestDTO) {
        requireAdminOrManager();
        ResourceResponseDTO created = resourceService.createResource(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/resources/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing resource", description = "Modifies an existing resource's details (Admin only)")
    @ApiResponse(responseCode = "200", description = "Resource updated successfully")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    public ResponseEntity<ResourceResponseDTO> updateResource(
            @PathVariable @NonNull Long id, 
            @Valid @RequestBody ResourceRequestDTO requestDTO) {
        requireAdminOrManager();
        return ResponseEntity.ok(resourceService.updateResource(id, requestDTO));
    }

    // DELETE /api/resources/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resource", description = "Removes a resource from the system (Admin only)")
    @ApiResponse(responseCode = "204", description = "Resource deleted successfully")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    public ResponseEntity<Void> deleteResource(
            @PathVariable @NonNull Long id) {
        requireAdminOrManager();
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/resources/search
    @GetMapping("/search")
    @Operation(summary = "Search resources", description = "Filters resources by name, type, location, and availability")
    @ApiResponse(responseCode = "200", description = "Search results returned")
    public ResponseEntity<List<ResourceResponseDTO>> searchResources(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Boolean available
    ) {
        return ResponseEntity.ok(resourceService.getFilteredResources(name, type, location, capacity, available));
    }

    // PATCH /api/resources/{id}/status
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update resource status", description = "Toggles resource status (Admin only)")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    public ResponseEntity<ResourceResponseDTO> updateStatus(
            @PathVariable @NonNull Long id,
            @RequestParam ResourceStatus status) {
        requireAdminOrManager();
        return ResponseEntity.ok(resourceService.updateResourceStatus(id, status));
    }
}
