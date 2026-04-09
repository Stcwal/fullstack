package backend.fullstack.alcohol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.alcohol.api.dto.AlcoholIncidentRequest;
import backend.fullstack.alcohol.api.dto.AlcoholIncidentResponse;
import backend.fullstack.alcohol.api.dto.ResolveIncidentRequest;
import backend.fullstack.alcohol.application.AlcoholIncidentService;
import backend.fullstack.alcohol.domain.AlcoholServingIncident;
import backend.fullstack.alcohol.domain.IncidentSeverity;
import backend.fullstack.alcohol.domain.IncidentStatus;
import backend.fullstack.alcohol.domain.IncidentType;
import backend.fullstack.alcohol.infrastructure.AlcoholIncidentRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class AlcoholIncidentServiceTest {

    @Mock
    private AlcoholIncidentRepository incidentRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private AccessContextService accessContext;

    private AlcoholIncidentService incidentService;

    private static final Long ORG_ID = 100L;
    private static final Long LOCATION_ID = 1L;

    @BeforeEach
    void setUp() {
        incidentService = new AlcoholIncidentService(
                incidentRepository, organizationRepository, locationRepository, accessContext
        );
    }

    @Test
    void createIncidentSavesWithOpenStatus() {
        Organization org = organization();
        Location location = location(org);
        User reporter = user(5L, org, Role.STAFF);
        AlcoholIncidentRequest request = incidentRequest();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(accessContext.getCurrentUser()).thenReturn(reporter);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        when(incidentRepository.save(any(AlcoholServingIncident.class))).thenAnswer(invocation -> {
            AlcoholServingIncident incident = invocation.getArgument(0);
            incident.setId(1L);
            return incident;
        });

        AlcoholIncidentResponse response = incidentService.create(request);

        ArgumentCaptor<AlcoholServingIncident> captor = ArgumentCaptor.forClass(AlcoholServingIncident.class);
        verify(incidentRepository).save(captor.capture());
        assertEquals(IncidentStatus.OPEN, captor.getValue().getStatus());
        assertEquals(IncidentType.REFUSED_SERVICE, captor.getValue().getIncidentType());
        assertEquals(1L, response.getId());
    }

    @Test
    void resolveIncidentSetsResolverAndTimestamp() {
        Organization org = organization();
        Location location = location(org);
        User reporter = user(5L, org, Role.STAFF);
        User resolver = user(8L, org, Role.MANAGER);

        AlcoholServingIncident existing = AlcoholServingIncident.builder()
                .id(1L)
                .organization(org)
                .location(location)
                .reportedBy(reporter)
                .incidentType(IncidentType.REFUSED_SERVICE)
                .severity(IncidentSeverity.MEDIUM)
                .status(IncidentStatus.OPEN)
                .description("Guest was intoxicated")
                .occurredAt(LocalDateTime.of(2026, 4, 1, 22, 0))
                .build();

        ResolveIncidentRequest request = new ResolveIncidentRequest();
        request.setCorrectiveAction("Staff received additional training");

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(accessContext.getCurrentUser()).thenReturn(resolver);
        when(incidentRepository.findByIdAndOrganization_Id(1L, ORG_ID)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(AlcoholServingIncident.class))).thenAnswer(i -> i.getArgument(0));

        AlcoholIncidentResponse response = incidentService.resolve(1L, request);

        assertEquals(IncidentStatus.RESOLVED, response.getStatus());
        assertEquals("Staff received additional training", response.getCorrectiveAction());
        assertNotNull(response.getResolvedAt());
        assertEquals(resolver.getId(), response.getResolvedByUserId());
    }

    @Test
    void listIncidentsReturnsFilteredResults() {
        Organization org = organization();
        Location location = location(org);
        User reporter = user(5L, org, Role.STAFF);

        AlcoholServingIncident incident = AlcoholServingIncident.builder()
                .id(1L)
                .organization(org)
                .location(location)
                .reportedBy(reporter)
                .incidentType(IncidentType.INTOXICATED_PERSON)
                .severity(IncidentSeverity.HIGH)
                .status(IncidentStatus.OPEN)
                .description("Heavily intoxicated guest")
                .occurredAt(LocalDateTime.of(2026, 4, 1, 23, 0))
                .build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(incidentRepository.findFiltered(ORG_ID, null, IncidentStatus.OPEN, null, null, null))
                .thenReturn(List.of(incident));

        List<AlcoholIncidentResponse> result = incidentService.list(null, IncidentStatus.OPEN, null, null, null);

        assertEquals(1, result.size());
        assertEquals(IncidentType.INTOXICATED_PERSON, result.get(0).getIncidentType());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(incidentRepository.findByIdAndOrganization_Id(999L, ORG_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> incidentService.getById(999L));
    }

    @Test
    void updateStatusToClosedSetsResolverIfMissing() {
        Organization org = organization();
        Location location = location(org);
        User reporter = user(5L, org, Role.STAFF);
        User admin = user(1L, org, Role.ADMIN);

        AlcoholServingIncident existing = AlcoholServingIncident.builder()
                .id(1L)
                .organization(org)
                .location(location)
                .reportedBy(reporter)
                .incidentType(IncidentType.DISTURBANCE)
                .severity(IncidentSeverity.LOW)
                .status(IncidentStatus.OPEN)
                .description("Minor disturbance")
                .occurredAt(LocalDateTime.of(2026, 4, 2, 21, 0))
                .build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(accessContext.getCurrentUser()).thenReturn(admin);
        when(incidentRepository.findByIdAndOrganization_Id(1L, ORG_ID)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(AlcoholServingIncident.class))).thenAnswer(i -> i.getArgument(0));

        AlcoholIncidentResponse response = incidentService.updateStatus(1L, IncidentStatus.CLOSED);

        assertEquals(IncidentStatus.CLOSED, response.getStatus());
        assertNotNull(response.getResolvedAt());
        assertEquals(admin.getId(), response.getResolvedByUserId());
    }

    private static AlcoholIncidentRequest incidentRequest() {
        AlcoholIncidentRequest request = new AlcoholIncidentRequest();
        request.setLocationId(LOCATION_ID);
        request.setIncidentType(IncidentType.REFUSED_SERVICE);
        request.setSeverity(IncidentSeverity.MEDIUM);
        request.setDescription("Guest appeared intoxicated, refused further service");
        request.setOccurredAt(LocalDateTime.of(2026, 4, 1, 22, 0));
        return request;
    }

    private static Organization organization() {
        return Organization.builder()
                .id(ORG_ID)
                .name("Everest Sushi & Fusion")
                .organizationNumber("937219997")
                .build();
    }

    private static Location location(Organization org) {
        return Location.builder()
                .id(LOCATION_ID)
                .organization(org)
                .name("Bar")
                .build();
    }

    private static User user(Long id, Organization org, Role role) {
        return User.builder()
                .id(id)
                .organization(org)
                .email("user" + id + "@everest.no")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hash")
                .role(role)
                .build();
    }
}
