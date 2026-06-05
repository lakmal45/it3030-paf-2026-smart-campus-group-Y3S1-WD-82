package com.project.paf.modules.resource.service;

import com.project.paf.modules.resource.dto.ResourceRequestDTO;
import com.project.paf.modules.resource.dto.ResourceResponseDTO;
import com.project.paf.modules.resource.exception.ResourceNotFoundException;
import com.project.paf.modules.resource.model.Resource;
import com.project.paf.modules.resource.model.ResourceStatus;
import com.project.paf.modules.resource.repository.ResourceRepository;
import com.project.paf.modules.auditlog.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceService Unit Tests")
@SuppressWarnings("null")
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;
    
    @Mock
    private AuditLogService auditLogService;

    @Mock
    private com.project.paf.modules.user.repository.UserRepository userRepository;

    @Mock
    private com.project.paf.modules.notification.service.EmailService emailService;

    @InjectMocks
    private ResourceService resourceService;

    private Resource testResource;
    private ResourceRequestDTO testRequestDTO;
    private ResourceResponseDTO expectedResponseDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testResource = new Resource();
        testResource.setId(1L);
        testResource.setName("Conference Room A");
        testResource.setType("Room");
        testResource.setLocation("Building 1 - Floor 3");
        testResource.setCapacity(20);
        testResource.setAvailable(true);
        testResource.setStatus(ResourceStatus.ACTIVE);
        testResource.setDescription("Spacious conference room with AV equipment");
        testResource.setAvailabilityWindows("09:00-17:00");

        testRequestDTO = new ResourceRequestDTO();
        testRequestDTO.setName("Conference Room A");
        testRequestDTO.setType("Room");
        testRequestDTO.setLocation("Building 1 - Floor 3");
        testRequestDTO.setCapacity(20);
        testRequestDTO.setAvailable(true);
        testRequestDTO.setStatus(ResourceStatus.ACTIVE);
        testRequestDTO.setDescription("Spacious conference room with AV equipment");
        testRequestDTO.setAvailabilityWindows("09:00-17:00");

        expectedResponseDTO = new ResourceResponseDTO();
        expectedResponseDTO.setId(1L);
        expectedResponseDTO.setName("Conference Room A");
        expectedResponseDTO.setType("Room");
        expectedResponseDTO.setLocation("Building 1 - Floor 3");
        expectedResponseDTO.setCapacity(20);
        expectedResponseDTO.setAvailable(true);
        expectedResponseDTO.setStatus(ResourceStatus.ACTIVE);
        expectedResponseDTO.setDescription("Spacious conference room with AV equipment");
        expectedResponseDTO.setAvailabilityWindows("09:00-17:00");
    }

    @Test
    @DisplayName("Should retrieve all resources successfully")
    void testGetAllResources() {
        // Arrange
        Resource resource2 = new Resource();
        resource2.setId(2L);
        resource2.setName("Lab Room B");
        resource2.setType("Lab");
        resource2.setLocation("Building 2 - Floor 1");
        resource2.setCapacity(30);
        resource2.setAvailable(false);
        resource2.setStatus(ResourceStatus.IN_MAINTENANCE);
        resource2.setDescription("Advanced science lab");
        resource2.setAvailabilityWindows("10:00-16:00");

        List<Resource> resourceList = Arrays.asList(testResource, resource2);
        when(resourceRepository.findAll()).thenReturn(resourceList);

        // Act
        List<ResourceResponseDTO> result = resourceService.getAllResources();

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Conference Room A");
        assertThat(result.get(1).getName()).isEqualTo("Lab Room B");
        verify(resourceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no resources exist")
    void testGetAllResourcesEmpty() {
        // Arrange
        when(resourceRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ResourceResponseDTO> result = resourceService.getAllResources();

        // Assert
        assertThat(result).isEmpty();
        verify(resourceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should create a new resource successfully")
    void testCreateResource() {
        // Arrange
        when(resourceRepository.save(any(Resource.class))).thenReturn(testResource);

        // Act
        ResourceResponseDTO result = resourceService.createResource(testRequestDTO);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getId()).isEqualTo(1L);
                    assertThat(dto.getName()).isEqualTo("Conference Room A");
                    assertThat(dto.getType()).isEqualTo("Room");
                    assertThat(dto.getLocation()).isEqualTo("Building 1 - Floor 3");
                    assertThat(dto.getCapacity()).isEqualTo(20);
                    assertThat(dto.getAvailable()).isTrue();
                    assertThat(dto.getStatus()).isEqualTo(ResourceStatus.ACTIVE);
                });
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should create resource with minimal required fields")
    void testCreateResourceMinimal() {
        // Arrange
        ResourceRequestDTO minimalDTO = new ResourceRequestDTO();
        minimalDTO.setName("Simple Room");
        minimalDTO.setType("Room");
        minimalDTO.setLocation("Building 1");
        minimalDTO.setCapacity(5);
        minimalDTO.setAvailable(true);
        minimalDTO.setStatus(ResourceStatus.ACTIVE);

        Resource savedResource = new Resource();
        savedResource.setId(3L);
        savedResource.setName("Simple Room");
        savedResource.setType("Room");
        savedResource.setLocation("Building 1");
        savedResource.setCapacity(5);
        savedResource.setAvailable(true);
        savedResource.setStatus(ResourceStatus.ACTIVE);

        when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);

        // Act
        ResourceResponseDTO result = resourceService.createResource(minimalDTO);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getId()).isEqualTo(3L);
                    assertThat(dto.getName()).isEqualTo("Simple Room");
                });
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should update an existing resource successfully")
    void testUpdateResource() {
        // Arrange
        Long resourceId = 1L;
        ResourceRequestDTO updateDTO = new ResourceRequestDTO();
        updateDTO.setName("Updated Conference Room");
        updateDTO.setType("Room");
        updateDTO.setLocation("Building 1 - Floor 3");
        updateDTO.setCapacity(25);
        updateDTO.setAvailable(true);
        updateDTO.setStatus(ResourceStatus.ACTIVE);
        updateDTO.setDescription("Updated description");
        updateDTO.setAvailabilityWindows("08:00-18:00");

        Resource updatedResource = new Resource();
        updatedResource.setId(resourceId);
        updatedResource.setName("Updated Conference Room");
        updatedResource.setType("Room");
        updatedResource.setLocation("Building 1 - Floor 3");
        updatedResource.setCapacity(25);
        updatedResource.setAvailable(true);
        updatedResource.setStatus(ResourceStatus.ACTIVE);
        updatedResource.setDescription("Updated description");
        updatedResource.setAvailabilityWindows("08:00-18:00");

        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(testResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);

        // Act
        ResourceResponseDTO result = resourceService.updateResource(resourceId, updateDTO);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getName()).isEqualTo("Updated Conference Room");
                    assertThat(dto.getCapacity()).isEqualTo(25);
                    assertThat(dto.getDescription()).isEqualTo("Updated description");
                });
        verify(resourceRepository, times(1)).findById(resourceId);
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent resource")
    void testUpdateResourceNotFound() {
        // Arrange
        Long resourceId = 999L;
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.updateResource(resourceId, testRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found with id: " + resourceId);
        verify(resourceRepository, times(1)).findById(resourceId);
        verify(resourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update only specific fields while preserving others")
    void testUpdateResourcePartial() {
        // Arrange
        Long resourceId = 1L;
        ResourceRequestDTO partialDTO = new ResourceRequestDTO();
        partialDTO.setName("Modified Name");
        partialDTO.setType("Room");
        partialDTO.setLocation("Building 1 - Floor 3");
        partialDTO.setCapacity(20);
        partialDTO.setAvailable(true);
        partialDTO.setStatus(ResourceStatus.ACTIVE);

        Resource existingResource = new Resource();
        existingResource.setId(resourceId);
        existingResource.setName("Original Name");
        existingResource.setType("Room");
        existingResource.setLocation("Building 1 - Floor 3");
        existingResource.setCapacity(20);
        existingResource.setAvailable(true);
        existingResource.setStatus(ResourceStatus.ACTIVE);
        existingResource.setDescription("Original description");

        Resource updatedResource = new Resource();
        updatedResource.setId(resourceId);
        updatedResource.setName("Modified Name");
        updatedResource.setType("Room");
        updatedResource.setLocation("Building 1 - Floor 3");
        updatedResource.setCapacity(20);
        updatedResource.setAvailable(true);
        updatedResource.setStatus(ResourceStatus.ACTIVE);
        updatedResource.setDescription("Original description");

        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(existingResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);

        // Act
        ResourceResponseDTO result = resourceService.updateResource(resourceId, partialDTO);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getName()).isEqualTo("Modified Name");
                    assertThat(dto.getDescription()).isEqualTo("Original description");
                });
        verify(resourceRepository, times(1)).findById(resourceId);
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should get resource by ID successfully")
    void testGetResourceById() {
        // Arrange
        Long resourceId = 1L;
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(testResource));

        // Act
        ResourceResponseDTO result = resourceService.getResourceById(resourceId);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getId()).isEqualTo(1L);
                    assertThat(dto.getName()).isEqualTo("Conference Room A");
                });
        verify(resourceRepository, times(1)).findById(resourceId);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent resource by ID")
    void testGetResourceByIdNotFound() {
        // Arrange
        Long resourceId = 999L;
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.getResourceById(resourceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found with id: " + resourceId);
        verify(resourceRepository, times(1)).findById(resourceId);
    }

    @Test
    @DisplayName("Should delete a resource successfully")
    void testDeleteResource() {
        // Arrange
        Long resourceId = 1L;
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(testResource));
        doNothing().when(resourceRepository).delete(testResource);

        // Act
        resourceService.deleteResource(resourceId);

        // Assert
        verify(resourceRepository, times(1)).findById(resourceId);
        verify(resourceRepository, times(1)).delete(testResource);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent resource")
    void testDeleteResourceNotFound() {
        // Arrange
        Long resourceId = 999L;
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.deleteResource(resourceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found with id: " + resourceId);
        verify(resourceRepository, times(1)).findById(resourceId);
        verify(resourceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should update resource status successfully")
    void testUpdateResourceStatus() {
        // Arrange
        Long resourceId = 1L;
        Resource existingResource = new Resource();
        existingResource.setId(resourceId);
        existingResource.setName("Conference Room A");
        existingResource.setStatus(ResourceStatus.ACTIVE);
        existingResource.setAvailable(true);

        Resource updatedResource = new Resource();
        updatedResource.setId(resourceId);
        updatedResource.setName("Conference Room A");
        updatedResource.setStatus(ResourceStatus.IN_MAINTENANCE);
        updatedResource.setAvailable(true);

        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(existingResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);

        // Act
        ResourceResponseDTO result = resourceService.updateResourceStatus(resourceId, ResourceStatus.IN_MAINTENANCE);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getStatus()).isEqualTo(ResourceStatus.IN_MAINTENANCE);
                });
        verify(resourceRepository, times(1)).findById(resourceId);
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should retrieve filtered resources successfully")
    void testGetFilteredResources() {
        // Arrange
        Resource room = new Resource();
        room.setId(1L);
        room.setName("Room 101");
        room.setType("Room");
        room.setLocation("Building 1");
        room.setCapacity(20);
        room.setAvailable(true);
        room.setStatus(ResourceStatus.ACTIVE);

        List<Resource> filteredList = Arrays.asList(room);
        when(resourceRepository.findByFilters("Room", "Room", "Building 1", null, true))
                .thenReturn(filteredList);

        // Act
        List<ResourceResponseDTO> result = resourceService.getFilteredResources("Room", "Room", "Building 1", null, true);

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .satisfies(dtos -> {
                    assertThat(dtos.get(0).getType()).isEqualTo("Room");
                    assertThat(dtos.get(0).getLocation()).isEqualTo("Building 1");
                });
        verify(resourceRepository, times(1)).findByFilters("Room", "Room", "Building 1", null, true);
    }

}