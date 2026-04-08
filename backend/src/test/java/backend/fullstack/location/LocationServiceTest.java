package backend.fullstack.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.location.dto.LocationMapper;
import backend.fullstack.location.dto.LocationRequest;
import backend.fullstack.location.dto.LocationResponse;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private AccessContextService accessContext;
    @Mock
    private LocationMapper locationMapper;

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(locationRepository, organizationRepository, accessContext, locationMapper);
    }

    @Test
    void createStoresLocationUnderCurrentOrganization() {
        LocationRequest request = new LocationRequest();
        request.setName("Oslo");
        request.setAddress("Street 1");

        Organization organization = Organization.builder()
                .id(100L)
                .name("Everest")
                .organizationNumber("937219997")
                .build();
        Location mapped = Location.builder().name("Oslo").address("Street 1").build();
        Location saved = Location.builder().id(7L).organization(organization).name("Oslo").address("Street 1").build();
        LocationResponse response = LocationResponse.builder().id(7L).name("Oslo").address("Street 1").build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);
        when(organizationRepository.findById(100L)).thenReturn(Optional.of(organization));
        when(locationMapper.toEntity(request)).thenReturn(mapped);
        when(locationRepository.save(any(Location.class))).thenReturn(saved);
        when(locationMapper.toResponse(saved)).thenReturn(response);

        LocationResponse result = locationService.create(request);

        verify(accessContext).assertHasRole(Role.ADMIN);
        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());
        assertEquals(organization.getId(), captor.getValue().getOrganizationId());
        assertEquals(7L, result.getId());
    }

    @Test
    void getAllAccessibleReturnsMappedLocationsForAllowedIds() {
        Location oslo = Location.builder().id(1L).name("Oslo").address("A").build();
        Location bergen = Location.builder().id(2L).name("Bergen").address("B").build();

        when(accessContext.getAllowedLocationIds()).thenReturn(List.of(1L, 2L));
        when(locationRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(oslo, bergen));
        when(locationMapper.toResponse(oslo)).thenReturn(LocationResponse.builder().id(1L).name("Oslo").address("A").build());
        when(locationMapper.toResponse(bergen)).thenReturn(LocationResponse.builder().id(2L).name("Bergen").address("B").build());

        List<LocationResponse> result = locationService.getAllAccessible();

        assertEquals(2, result.size());
        assertEquals("Oslo", result.get(0).getName());
        assertEquals("Bergen", result.get(1).getName());
    }

    @Test
    void getByIdThrowsWhenLocationDoesNotExistInCurrentOrganization() {
        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);
        when(locationRepository.findByIdAndOrganization_Id(9L, 100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationService.getById(9L));
        verify(accessContext).getCurrentOrganizationId();
    }
}
