package com.project.paf.modules.resource.repository;

import com.project.paf.modules.resource.model.Resource;
import com.project.paf.modules.resource.model.ResourceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("ResourceRepository Integration Tests with JPA")
@SuppressWarnings("null")
class ResourceRepositoryTest {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Resource testResource1;
    private Resource testResource2;
    private Resource testResource3;

    @BeforeEach
    void setUp() {
        // Create test resources
        testResource1 = new Resource();
        testResource1.setName("Conference Room A");
        testResource1.setType("Room");
        testResource1.setLocation("Building 1 - Floor 3");
        testResource1.setCapacity(20);
        testResource1.setAvailable(true);
        testResource1.setStatus(ResourceStatus.ACTIVE);
        testResource1.setDescription("Spacious conference room");
        testResource1.setAvailabilityWindows("09:00-17:00");

        testResource2 = new Resource();
        testResource2.setName("Science Lab B");
        testResource2.setType("Lab");
        testResource2.setLocation("Building 2 - Floor 1");
        testResource2.setCapacity(30);
        testResource2.setAvailable(false);
        testResource2.setStatus(ResourceStatus.IN_MAINTENANCE);
        testResource2.setDescription("Advanced science laboratory");
        testResource2.setAvailabilityWindows("10:00-16:00");

        testResource3 = new Resource();
        testResource3.setName("Computer Lab C");
        testResource3.setType("Lab");
        testResource3.setLocation("Building 1 - Floor 2");
        testResource3.setCapacity(50);
        testResource3.setAvailable(true);
        testResource3.setStatus(ResourceStatus.ACTIVE);
        testResource3.setDescription("Computer lab with 50 workstations");
        testResource3.setAvailabilityWindows("08:00-18:00");

        // Persist resources using TestEntityManager
        entityManager.persistAndFlush(testResource1);
        entityManager.persistAndFlush(testResource2);
        entityManager.persistAndFlush(testResource3);
    }

    @Test
    @DisplayName("Should save and retrieve a resource")
    void testSaveAndFindResource() {
        // Arrange
        Resource newResource = new Resource();
        newResource.setName("New Meeting Room");
        newResource.setType("Room");
        newResource.setLocation("Building 3");
        newResource.setCapacity(15);
        newResource.setAvailable(true);
        newResource.setStatus(ResourceStatus.ACTIVE);

        // Act
        Resource savedResource = resourceRepository.save(newResource);

        // Assert
        assertThat(savedResource.getId()).isNotNull();
        assertThat(savedResource.getName()).isEqualTo("New Meeting Room");

        Optional<Resource> retrievedResource = resourceRepository.findById(savedResource.getId());
        assertThat(retrievedResource)
                .isPresent()
                .contains(savedResource);
    }

    @Test
    @DisplayName("Should find resources by exact type")
    void testFindByType() {
        // Act
        List<Resource> labResources = resourceRepository.findByType("Lab");

        // Assert
        assertThat(labResources)
                .isNotNull()
                .hasSize(2)
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("Science Lab B", "Computer Lab C");

        assertThat(labResources)
                .allMatch(r -> r.getType().equals("Lab"));
    }

    @Test
    @DisplayName("Should find resources by type case-insensitive")
    void testFindByTypeContainingIgnoreCase() {
        // Act
        List<Resource> roomResources = resourceRepository.findByTypeContainingIgnoreCase("room");

        // Assert
        assertThat(roomResources)
                .isNotNull()
                .hasSize(1)
                .extracting(Resource::getName)
                .contains("Conference Room A");
    }

    @Test
    @DisplayName("Should find multiple resources by type")
    void testFindByTypeMultiple() {
        // Act
        List<Resource> labResources = resourceRepository.findByType("Lab");

        // Assert
        assertThat(labResources)
                .isNotNull()
                .hasSize(2)
                .satisfies(resources -> {
                    assertThat(resources).extracting(Resource::getType).allMatch("Lab"::equals);
                    assertThat(resources).extracting(Resource::getCapacity).contains(30, 50);
                });
    }

    @Test
    @DisplayName("Should return empty list when type not found")
    void testFindByTypeNotFound() {
        // Act
        List<Resource> nonExistentResources = resourceRepository.findByType("NonExistentType");

        // Assert
        assertThat(nonExistentResources).isEmpty();
    }

    @Test
    @DisplayName("Should find resources by exact location")
    void testFindByLocation() {
        // Act
        List<Resource> building1Floor3Resources = resourceRepository.findByLocation("Building 1 - Floor 3");

        // Assert
        assertThat(building1Floor3Resources)
                .isNotNull()
                .hasSize(1)
                .extracting(Resource::getName)
                .contains("Conference Room A");
    }

    @Test
    @DisplayName("Should find resources by location containing text")
    void testFindByLocationContainingIgnoreCase() {
        // Act
        List<Resource> building1Resources = resourceRepository.findByLocationContainingIgnoreCase("Building 1");

        // Assert
        assertThat(building1Resources)
                .isNotNull()
                .hasSize(2)
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("Conference Room A", "Computer Lab C");
    }

    @Test
    @DisplayName("Should find multiple resources by location containing")
    void testFindByLocationContainingMultiple() {
        // Act
        List<Resource> resources = resourceRepository.findByLocationContainingIgnoreCase("Building");

        // Assert
        assertThat(resources)
                .isNotNull()
                .hasSize(3)
                .allMatch(r -> r.getLocation().contains("Building"));
    }

    @Test
    @DisplayName("Should return empty list when location not found")
    void testFindByLocationNotFound() {
        // Act
        List<Resource> nonExistentResources = resourceRepository.findByLocation("NonExistent Building");

        // Assert
        assertThat(nonExistentResources).isEmpty();
    }

    @Test
    @DisplayName("Should find resources by availability")
    void testFindByAvailable() {
        // Act
        List<Resource> availableResources = resourceRepository.findByAvailable(true);

        // Assert
        assertThat(availableResources)
                .isNotNull()
                .hasSize(2)
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("Conference Room A", "Computer Lab C")
                .allMatch(name -> !name.equals("Science Lab B"));

        assertThat(availableResources)
                .allMatch(r -> r.getAvailable().equals(true));
    }

    @Test
    @DisplayName("Should find unavailable resources")
    void testFindByNotAvailable() {
        // Act
        List<Resource> unavailableResources = resourceRepository.findByAvailable(false);

        // Assert
        assertThat(unavailableResources)
                .isNotNull()
                .hasSize(1)
                .extracting(Resource::getName)
                .contains("Science Lab B");

        assertThat(unavailableResources)
                .allMatch(r -> !r.getAvailable());
    }

    @Test
    @DisplayName("Should find resources by minimum capacity")
    void testFindByCapacityGreaterThanEqual() {
        // Act
        List<Resource> largeResources = resourceRepository.findByCapacityGreaterThanEqual(30);

        // Assert
        assertThat(largeResources)
                .isNotNull()
                .hasSize(2)
                .extracting(Resource::getCapacity)
                .containsExactlyInAnyOrder(30, 50);

        assertThat(largeResources)
                .allMatch(r -> r.getCapacity() >= 30);
    }

    @Test
    @DisplayName("Should find all resources with capacity constraint")
    void testFindByCapacitySmall() {
        // Act
        List<Resource> allResources = resourceRepository.findByCapacityGreaterThanEqual(1);

        // Assert
        assertThat(allResources)
                .isNotNull()
                .hasSize(3);
    }

    @Test
    @DisplayName("Should find resources by type and location combined")
    void testFindByTypeAndLocationCombined() {
        // Act
        List<Resource> resources = resourceRepository
                .findByTypeContainingIgnoreCaseAndLocationContainingIgnoreCaseAndAvailable("Lab", "Building 1", true);

        // Assert
        assertThat(resources)
                .isNotNull()
                .hasSize(1)
                .extracting(Resource::getName)
                .contains("Computer Lab C");
    }

    @Test
    @DisplayName("Should return empty when combined filters don't match")
    void testFindByTypeAndLocationCombinedEmpty() {
        // Act
        List<Resource> resources = resourceRepository
                .findByTypeContainingIgnoreCaseAndLocationContainingIgnoreCaseAndAvailable("Room", "Building 2", true);

        // Assert
        assertThat(resources).isEmpty();
    }

    @Test
    @DisplayName("Should find all resources")
    void testFindAll() {
        // Act
        List<Resource> allResources = resourceRepository.findAll();

        // Assert
        assertThat(allResources)
                .isNotNull()
                .hasSize(3)
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("Conference Room A", "Science Lab B", "Computer Lab C");
    }

    @Test
    @DisplayName("Should filter resources with advanced query - all parameters")
    void testFindByFiltersAllParameters() {
        // Act
        List<Resource> filtered = resourceRepository.findByFilters("Lab", "Lab", "Building 1", 40, true);

        // Assert
        assertThat(filtered)
                .isNotNull()
                .hasSize(1)
                .extracting(Resource::getName)
                .contains("Computer Lab C");
    }

    @Test
    @DisplayName("Should filter resources with null parameters")
    void testFindByFiltersWithNullParameters() {
        // Act
        List<Resource> filtered = resourceRepository.findByFilters(null, "Lab", null, null, true);

        // Assert
        assertThat(filtered)
                .isNotNull()
                .hasSize(1)
                .extracting(Resource::getName)
                .contains("Computer Lab C");
    }

    @Test
    @DisplayName("Should filter resources with partial name match")
    void testFindByFiltersPartialName() {
        // Act
        List<Resource> filtered = resourceRepository.findByFilters("room", null, null, null, null);

        // Assert
        assertThat(filtered)
                .isNotNull()
                .hasSize(1)
                .extracting(Resource::getName)
                .contains("Conference Room A");
    }

    @Test
    @DisplayName("Should filter resources with capacity constraint")
    void testFindByFiltersCapacity() {
        // Act
        List<Resource> filtered = resourceRepository.findByFilters(null, null, null, 25, null);

        // Assert
        assertThat(filtered)
                .isNotNull()
                .hasSize(2)
                .extracting(Resource::getCapacity)
                .allMatch(capacity -> capacity >= 25);
    }

    @Test
    @DisplayName("Should update a resource")
    void testUpdateResource() {
        // Act
        testResource1.setName("Updated Conference Room A");
        testResource1.setCapacity(25);
        Resource updated = resourceRepository.save(testResource1);
        
        Optional<Resource> retrieved = resourceRepository.findById(updated.getId());

        // Assert
        assertThat(retrieved)
                .isPresent()
                .satisfies(resource -> {
                    assertThat(resource.get().getName()).isEqualTo("Updated Conference Room A");
                    assertThat(resource.get().getCapacity()).isEqualTo(25);
                });
    }

    @Test
    @DisplayName("Should delete a resource")
    void testDeleteResource() {
        // Arrange
        Long resourceId = testResource1.getId();

        // Act
        resourceRepository.delete(testResource1);

        // Assert
        Optional<Resource> deleted = resourceRepository.findById(resourceId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should count resources")
    void testCountResources() {
        // Act
        long count = resourceRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find resources by name containing")
    void testFindByNameContainingIgnoreCase() {
        // Act
        List<Resource> results = resourceRepository.findByNameContainingIgnoreCase("lab");

        // Assert
        assertThat(results)
                .isNotNull()
                .hasSize(2)
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("Science Lab B", "Computer Lab C");
    }
}
