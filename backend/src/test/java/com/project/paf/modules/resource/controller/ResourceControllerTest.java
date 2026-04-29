package com.project.paf.modules.resource.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.paf.modules.resource.dto.ResourceRequestDTO;
import com.project.paf.modules.resource.dto.ResourceResponseDTO;
import com.project.paf.modules.resource.exception.ResourceNotFoundException;
import com.project.paf.modules.resource.model.ResourceStatus;
import com.project.paf.modules.resource.service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.project.paf.modules.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ResourceController Integration Tests")
// Suppress null-safety warnings for the entire class because the Spring MockMvc 
// and Hamcrest matcher DSLs are not fully null-annotated, causing numerous 
// false positives in a strict null-safety environment.
@SuppressWarnings("null")
@WithMockUser(roles = "ADMIN")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private UserRepository userRepository;

    private ResourceResponseDTO testResourceDTO;
    private ResourceRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testResourceDTO = new ResourceResponseDTO();
        testResourceDTO.setId(1L);
        testResourceDTO.setName("Conference Room A");
        testResourceDTO.setType("Room");
        testResourceDTO.setLocation("Building 1 - Floor 3");
        testResourceDTO.setCapacity(20);
        testResourceDTO.setAvailable(true);
        testResourceDTO.setStatus(ResourceStatus.ACTIVE);
        testResourceDTO.setDescription("Spacious conference room with AV equipment");
        testResourceDTO.setAvailabilityWindows("09:00-17:00");

        testRequestDTO = new ResourceRequestDTO();
        testRequestDTO.setName("Conference Room A");
        testRequestDTO.setType("Room");
        testRequestDTO.setLocation("Building 1 - Floor 3");
        testRequestDTO.setCapacity(20);
        testRequestDTO.setAvailable(true);
        testRequestDTO.setStatus(ResourceStatus.ACTIVE);
        testRequestDTO.setDescription("Spacious conference room with AV equipment");
        testRequestDTO.setAvailabilityWindows("09:00-17:00");
    }

    @Test
    @DisplayName("GET /api/resources - should return 200 with list of resources")
    void testGetAllResourcesReturns200() throws Exception {
        // Arrange
        ResourceResponseDTO resource2 = new ResourceResponseDTO();
        resource2.setId(2L);
        resource2.setName("Lab Room B");
        resource2.setType("Lab");
        resource2.setLocation("Building 2");
        resource2.setCapacity(30);
        resource2.setAvailable(false);
        resource2.setStatus(ResourceStatus.IN_MAINTENANCE);

        List<ResourceResponseDTO> resources = Arrays.asList(testResourceDTO, resource2);
        when(resourceService.getAllResources()).thenReturn(resources);

        // Act & Assert
        mockMvc.perform(get("/api/resources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Conference Room A")))
                .andExpect(jsonPath("$[0].type", is("Room")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Lab Room B")))
                .andExpect(jsonPath("$[1].status", is("IN_MAINTENANCE")));

        verify(resourceService, times(1)).getAllResources();
    }

    @Test
    @DisplayName("GET /api/resources - should return 200 with empty list")
    void testGetAllResourcesEmptyReturns200() throws Exception {
        // Arrange
        when(resourceService.getAllResources()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/resources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(resourceService, times(1)).getAllResources();
    }

    @Test
    @DisplayName("GET /api/resources/{id} - should return 200 with resource details")
    void testGetResourceByIdReturns200() throws Exception {
        // Arrange
        Long resourceId = 1L;
        when(resourceService.getResourceById(resourceId)).thenReturn(testResourceDTO);

        // Act & Assert
        mockMvc.perform(get("/api/resources/{id}", resourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Conference Room A")))
                .andExpect(jsonPath("$.capacity", is(20)))
                .andExpect(jsonPath("$.available", is(true)));

        verify(resourceService, times(1)).getResourceById(resourceId);
    }

    @Test
    @DisplayName("GET /api/resources/{id} - should return 404 when resource not found")
    void testGetResourceByIdReturns404() throws Exception {
        // Arrange
        Long resourceId = 999L;
        when(resourceService.getResourceById(resourceId))
                .thenThrow(new ResourceNotFoundException("Resource not found with id: " + resourceId));

        // Act & Assert
        mockMvc.perform(get("/api/resources/{id}", resourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(resourceService, times(1)).getResourceById(resourceId);
    }

    @Test
    @DisplayName("POST /api/resources - should return 201 with created resource")
    void testCreateResourceReturns201() throws Exception {
        // Arrange
        when(resourceService.createResource(any(ResourceRequestDTO.class))).thenReturn(testResourceDTO);

        // Act & Assert
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Conference Room A")))
                .andExpect(jsonPath("$.type", is("Room")))
                .andExpect(jsonPath("$.capacity", is(20)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(resourceService, times(1)).createResource(any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/resources - should return 400 with invalid payload (missing name)")
    void testCreateResourceInvalidMissingNameReturns400() throws Exception {
        // Arrange
        ResourceRequestDTO invalidDTO = new ResourceRequestDTO();
        invalidDTO.setType("Room");
        invalidDTO.setLocation("Building 1");
        invalidDTO.setCapacity(20);
        invalidDTO.setAvailable(true);
        invalidDTO.setStatus(ResourceStatus.ACTIVE);
        // name is missing

        // Act & Assert
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(resourceService, never()).createResource(any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/resources - should return 400 with invalid payload (missing type)")
    void testCreateResourceInvalidMissingTypeReturns400() throws Exception {
        // Arrange
        ResourceRequestDTO invalidDTO = new ResourceRequestDTO();
        invalidDTO.setName("Conference Room");
        invalidDTO.setLocation("Building 1");
        invalidDTO.setCapacity(20);
        invalidDTO.setAvailable(true);
        invalidDTO.setStatus(ResourceStatus.ACTIVE);
        // type is missing

        // Act & Assert
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(resourceService, never()).createResource(any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/resources - should return 400 with invalid payload (missing location)")
    void testCreateResourceInvalidMissingLocationReturns400() throws Exception {
        // Arrange
        ResourceRequestDTO invalidDTO = new ResourceRequestDTO();
        invalidDTO.setName("Conference Room");
        invalidDTO.setType("Room");
        invalidDTO.setCapacity(20);
        invalidDTO.setAvailable(true);
        invalidDTO.setStatus(ResourceStatus.ACTIVE);
        // location is missing

        // Act & Assert
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(resourceService, never()).createResource(any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/resources - should return 400 with invalid payload (invalid capacity)")
    void testCreateResourceInvalidCapacityReturns400() throws Exception {
        // Arrange
        ResourceRequestDTO invalidDTO = new ResourceRequestDTO();
        invalidDTO.setName("Conference Room");
        invalidDTO.setType("Room");
        invalidDTO.setLocation("Building 1");
        invalidDTO.setCapacity(0); // Invalid: capacity must be at least 1
        invalidDTO.setAvailable(true);
        invalidDTO.setStatus(ResourceStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(resourceService, never()).createResource(any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/resources - should return 400 with null capacity")
    void testCreateResourceNullCapacityReturns400() throws Exception {
        // Arrange
        String invalidJson = """
                {
                    "name": "Conference Room",
                    "type": "Room",
                    "location": "Building 1",
                    "available": true,
                    "status": "ACTIVE"
                }""";

        // Act & Assert
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(resourceService, never()).createResource(any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/resources - should return 400 with missing status")
    void testCreateResourceMissingStatusReturns400() throws Exception {
        // Arrange
        String invalidJson = """
                {
                    "name": "Conference Room",
                    "type": "Room",
                    "location": "Building 1",
                    "capacity": 20,
                    "available": true
                }""";

        // Act & Assert
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(resourceService, never()).createResource(any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/resources/{id} - should return 200 with updated resource")
    void testUpdateResourceReturns200() throws Exception {
        // Arrange
        Long resourceId = 1L;
        ResourceResponseDTO updatedDTO = new ResourceResponseDTO();
        updatedDTO.setId(resourceId);
        updatedDTO.setName("Updated Conference Room");
        updatedDTO.setType("Room");
        updatedDTO.setLocation("Building 1 - Floor 3");
        updatedDTO.setCapacity(25);
        updatedDTO.setAvailable(true);
        updatedDTO.setStatus(ResourceStatus.ACTIVE);

        when(resourceService.updateResource(eq(resourceId), any(ResourceRequestDTO.class)))
                .thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/resources/{id}", resourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Conference Room")))
                .andExpect(jsonPath("$.capacity", is(25)));

        verify(resourceService, times(1)).updateResource(eq(resourceId), any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/resources/{id} - should return 404 when resource not found")
    void testUpdateResourceNotFoundReturns404() throws Exception {
        // Arrange
        Long resourceId = 999L;
        when(resourceService.updateResource(eq(resourceId), any(ResourceRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Resource not found with id: " + resourceId));

        // Act & Assert
        mockMvc.perform(put("/api/resources/{id}", resourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isNotFound());

        verify(resourceService, times(1)).updateResource(eq(resourceId), any(ResourceRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/resources/{id} - should return 204 when deleted")
    void testDeleteResourceReturns204() throws Exception {
        // Arrange
        Long resourceId = 1L;
        doNothing().when(resourceService).deleteResource(resourceId);

        // Act & Assert
        mockMvc.perform(delete("/api/resources/{id}", resourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(resourceService, times(1)).deleteResource(resourceId);
    }

    @Test
    @DisplayName("DELETE /api/resources/{id} - should return 404 when resource not found")
    void testDeleteResourceNotFoundReturns404() throws Exception {
        // Arrange
        Long resourceId = 999L;
        doThrow(new ResourceNotFoundException("Resource not found with id: " + resourceId))
                .when(resourceService).deleteResource(resourceId);

        // Act & Assert
        mockMvc.perform(delete("/api/resources/{id}", resourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(resourceService, times(1)).deleteResource(resourceId);
    }

    @Test
    @DisplayName("GET /api/resources/search - should return filtered resources with status 200")
    void testSearchResourcesReturns200() throws Exception {
        // Arrange
        ResourceResponseDTO room = new ResourceResponseDTO();
        room.setId(1L);
        room.setName("Room 101");
        room.setType("Room");
        room.setLocation("Building 1");
        room.setCapacity(20);
        room.setAvailable(true);
        room.setStatus(ResourceStatus.ACTIVE);

        List<ResourceResponseDTO> resources = Arrays.asList(room);
        when(resourceService.getFilteredResources("Room", "Room", "Building 1", null, true))
                .thenReturn(resources);

        // Act & Assert
        mockMvc.perform(get("/api/resources/search")
                .param("name", "Room")
                .param("type", "Room")
                .param("location", "Building 1")
                .param("available", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("Room")))
                .andExpect(jsonPath("$[0].location", is("Building 1")));

        verify(resourceService, times(1)).getFilteredResources("Room", "Room", "Building 1", null, true);
    }

    @Test
    @DisplayName("PATCH /api/resources/{id}/status - should return 200 with updated status")
    void testUpdateStatusReturns200() throws Exception {
        // Arrange
        Long resourceId = 1L;
        ResourceResponseDTO statusUpdatedDTO = new ResourceResponseDTO();
        statusUpdatedDTO.setId(resourceId);
        statusUpdatedDTO.setName("Conference Room A");
        statusUpdatedDTO.setStatus(ResourceStatus.IN_MAINTENANCE);
        statusUpdatedDTO.setAvailable(true);

        when(resourceService.updateResourceStatus(resourceId, ResourceStatus.IN_MAINTENANCE))
                .thenReturn(statusUpdatedDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/resources/{id}/status", resourceId)
                .param("status", "IN_MAINTENANCE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_MAINTENANCE")));

        verify(resourceService, times(1)).updateResourceStatus(resourceId, ResourceStatus.IN_MAINTENANCE);
    }
}
