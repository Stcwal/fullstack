package backend.fullstack.alcohol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import backend.fullstack.alcohol.api.dto.AgeVerificationRequest;
import backend.fullstack.alcohol.api.dto.AgeVerificationResponse;
import backend.fullstack.alcohol.application.AgeVerificationService;
import backend.fullstack.alcohol.domain.AgeVerificationLog;
import backend.fullstack.alcohol.domain.VerificationMethod;
import backend.fullstack.alcohol.infrastructure.AgeVerificationRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class AgeVerificationServiceTest {

    @Mock
    private AgeVerificationRepository verificationRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private AccessContextService accessContext;

    private AgeVerificationService verificationService;

    private static final Long ORG_ID = 100L;
    private static final Long LOCATION_ID = 1L;

    @BeforeEach
    void setUp() {
        verificationService = new AgeVerificationService(
                verificationRepository, organizationRepository, locationRepository, accessContext
        );
    }

    @Test
    void createVerificationLogSavesCorrectFields() {
        Organization org = organization();
        Location location = location(org);
        User user = user(5L, org);
        AgeVerificationRequest request = verificationRequest();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(accessContext.getCurrentUser()).thenReturn(user);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        when(verificationRepository.save(any(AgeVerificationLog.class))).thenAnswer(invocation -> {
            AgeVerificationLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        AgeVerificationResponse response = verificationService.create(request);

        ArgumentCaptor<AgeVerificationLog> captor = ArgumentCaptor.forClass(AgeVerificationLog.class);
        verify(verificationRepository).save(captor.capture());
        assertEquals(VerificationMethod.ID_CHECKED, captor.getValue().getVerificationMethod());
        assertTrue(captor.getValue().isGuestAppearedUnderage());
        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getVerifiedByName());
    }

    @Test
    void createVerificationDefaultsVerifiedAtToNow() {
        Organization org = organization();
        Location location = location(org);
        User user = user(5L, org);
        AgeVerificationRequest request = verificationRequest();
        request.setVerifiedAt(null);

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(accessContext.getCurrentUser()).thenReturn(user);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        when(verificationRepository.save(any(AgeVerificationLog.class))).thenAnswer(invocation -> {
            AgeVerificationLog log = invocation.getArgument(0);
            log.setId(2L);
            return log;
        });

        verificationService.create(request);

        ArgumentCaptor<AgeVerificationLog> captor = ArgumentCaptor.forClass(AgeVerificationLog.class);
        verify(verificationRepository).save(captor.capture());
        assertNotNull(captor.getValue().getVerifiedAt());
    }

    @Test
    void listReturnsFilteredResults() {
        Organization org = organization();
        Location location = location(org);
        User user = user(5L, org);
        AgeVerificationLog log = AgeVerificationLog.builder()
                .id(1L)
                .organization(org)
                .location(location)
                .verifiedBy(user)
                .verificationMethod(VerificationMethod.ID_CHECKED)
                .guestAppearedUnderage(true)
                .wasRefused(false)
                .verifiedAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                .build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(verificationRepository.findFiltered(ORG_ID, null, null, null)).thenReturn(List.of(log));

        List<AgeVerificationResponse> result = verificationService.list(null, null, null);

        assertEquals(1, result.size());
        assertEquals(VerificationMethod.ID_CHECKED, result.get(0).getVerificationMethod());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(verificationRepository.findByIdAndOrganization_Id(999L, ORG_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> verificationService.getById(999L));
    }

    private static AgeVerificationRequest verificationRequest() {
        AgeVerificationRequest request = new AgeVerificationRequest();
        request.setLocationId(LOCATION_ID);
        request.setVerificationMethod(VerificationMethod.ID_CHECKED);
        request.setGuestAppearedUnderage(true);
        request.setIdWasValid(true);
        request.setWasRefused(false);
        request.setVerifiedAt(LocalDateTime.of(2026, 4, 1, 20, 30));
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

    private static User user(Long id, Organization org) {
        return User.builder()
                .id(id)
                .organization(org)
                .email("staff@everest.no")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hash")
                .role(Role.STAFF)
                .build();
    }
}
