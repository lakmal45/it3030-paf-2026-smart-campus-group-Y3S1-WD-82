package com.project.paf.modules.resource.service;

import com.project.paf.modules.auditlog.AuditAction;
import com.project.paf.modules.auditlog.AuditLogService;
import com.project.paf.modules.resource.dto.ResourceRequestDTO;
import com.project.paf.modules.resource.dto.ResourceResponseDTO;
import com.project.paf.modules.resource.exception.ResourceNotFoundException;
import com.project.paf.modules.resource.model.Resource;
import com.project.paf.modules.resource.model.ResourceStatus;
import com.project.paf.modules.resource.repository.ResourceRepository;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final AuditLogService auditLogService;
    private final com.project.paf.modules.user.repository.UserRepository userRepository;
    private final com.project.paf.modules.notification.service.EmailService emailService;

    public ResourceService(ResourceRepository resourceRepository, AuditLogService auditLogService, com.project.paf.modules.user.repository.UserRepository userRepository, com.project.paf.modules.notification.service.EmailService emailService) {
        this.resourceRepository = resourceRepository;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        // Temporary fix: Ensure Kasun and master admin have correct roles in the current DB
        userRepository.findAll().forEach(u -> {
            if (u.getEmail().equalsIgnoreCase("admin@campus.com") || u.getName().toLowerCase().contains("kasun")) {
                if (u.getRole() != com.project.paf.modules.user.model.Role.ADMIN) {
                    u.setRole(com.project.paf.modules.user.model.Role.ADMIN);
                    userRepository.save(u);
                }
            }
        });
    }

    public List<ResourceResponseDTO> getAllResources() {
        return resourceRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<ResourceResponseDTO> getFilteredResources(
            String name,
            String type,
            String location,
            Integer minCapacity,
            Boolean available
    ) {
        // Updated service to use repository filtering
        return resourceRepository.findByFilters(name, type, location, minCapacity, available)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ResourceResponseDTO getResourceById(@NonNull Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return toResponseDTO(resource);
    }

    public ResourceResponseDTO createResource(@Valid ResourceRequestDTO requestDTO) {
        Resource resource = toEntity(requestDTO);
        Resource savedResource = resourceRepository.save(java.util.Objects.requireNonNull(resource));

        auditLogService.log(AuditAction.RESOURCE_CREATED, null,
                "Resource '" + savedResource.getName() + "' (" + savedResource.getType() + ") created at " + savedResource.getLocation(),
                "Resource", savedResource.getId());

        ResourceResponseDTO response = toResponseDTO(savedResource);

        // Get currently authenticated user to send them the confirmation email
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof com.project.paf.modules.user.model.User) {
            com.project.paf.modules.user.model.User adminUser = (com.project.paf.modules.user.model.User) auth.getPrincipal();
            emailService.notifyResourceCreated(adminUser, response);
        }

        return response;
    }

    public ResourceResponseDTO updateResource(@NonNull Long id, @Valid ResourceRequestDTO requestDTO) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        existingResource.setName(requestDTO.getName());
        existingResource.setType(requestDTO.getType());
        existingResource.setLocation(requestDTO.getLocation());
        existingResource.setCapacity(requestDTO.getCapacity());
        existingResource.setAvailable(requestDTO.getAvailable());
        existingResource.setStatus(requestDTO.getStatus());
        existingResource.setDescription(requestDTO.getDescription());
        existingResource.setAvailabilityWindows(requestDTO.getAvailabilityWindows());

        Resource updatedResource = resourceRepository.save(java.util.Objects.requireNonNull(existingResource));

        auditLogService.log(AuditAction.RESOURCE_UPDATED, null,
                "Resource '" + updatedResource.getName() + "' (#" + id + ") updated",
                "Resource", id);

        return toResponseDTO(updatedResource);
    }

    public ResourceResponseDTO updateResourceStatus(@NonNull Long id, ResourceStatus status) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        
        existingResource.setStatus(status);
        // If status is BOOKED or IN_MAINTENANCE, we might want to mark available=false, 
        // but for now, we'll keep it simple as a status toggle.
        if (status == ResourceStatus.ACTIVE) {
            existingResource.setAvailable(true);
        } else if (status == ResourceStatus.RETIRED) {
            existingResource.setAvailable(false);
        }

        Resource updatedResource = resourceRepository.save(java.util.Objects.requireNonNull(existingResource));
        auditLogService.log(AuditAction.RESOURCE_STATUS_UPDATED, null,
                "Resource '" + updatedResource.getName() + "' (#" + id + ") status changed to " + status,
                "Resource", id);
        return toResponseDTO(updatedResource);
    }

    public void deleteResource(@NonNull Long id) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        String name = existingResource.getName();
        resourceRepository.delete(java.util.Objects.requireNonNull(existingResource));

        auditLogService.log(AuditAction.RESOURCE_DELETED, null,
                "Resource '" + name + "' (#" + id + ") deleted",
                "Resource", id);
    }

    private Resource toEntity(ResourceRequestDTO requestDTO) {
        Resource resource = new Resource();
        resource.setName(requestDTO.getName());
        resource.setType(requestDTO.getType());
        resource.setLocation(requestDTO.getLocation());
        resource.setCapacity(requestDTO.getCapacity());
        resource.setAvailable(requestDTO.getAvailable());
        resource.setStatus(requestDTO.getStatus());
        resource.setDescription(requestDTO.getDescription());
        resource.setAvailabilityWindows(requestDTO.getAvailabilityWindows());
        return resource;
    }

    private ResourceResponseDTO toResponseDTO(Resource resource) {
        ResourceResponseDTO responseDTO = new ResourceResponseDTO();
        responseDTO.setId(resource.getId());
        responseDTO.setName(resource.getName());
        responseDTO.setType(resource.getType());
        responseDTO.setLocation(resource.getLocation());
        responseDTO.setCapacity(resource.getCapacity());
        responseDTO.setAvailable(resource.getAvailable());
        responseDTO.setStatus(resource.getStatus());
        responseDTO.setDescription(resource.getDescription());
        responseDTO.setAvailabilityWindows(resource.getAvailabilityWindows());
        return responseDTO;
    }
}
