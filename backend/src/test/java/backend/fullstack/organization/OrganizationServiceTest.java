package backend.fullstack.organization;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import backend.fullstack.exceptions.OrganizationConflictException;
import backend.fullstack.organization.dto.OrganizationMapper;
import backend.fullstack.organization.dto.OrganizationRequest;
import backend.fullstack.organization.dto.OrganizationResponse;
import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccessContextService accessContext;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private OrganizationMapper organizationMapper;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository,
                userRepository,
                accessContext,
                authorizationService,
                organizationMapper
        );
    }

    @Test
    void createRejectsDuplicateOrganizationNumber() {
        OrganizationRequest request = new OrganizationRequest();
        request.setName("Everest");
        request.setOrganizationNumber("937219997");

        when(organizationRepository.existsByOrganizationNumber("937219997")).thenReturn(true);

        assertThrows(OrganizationConflictException.class, () -> organizationService.create(request));
    }

    @Test
    void createSavesOrganizationAndMapsResponse() {
        OrganizationRequest request = new OrganizationRequest();
        request.setName("Everest");
        request.setOrganizationNumber("937219997");

        Organization mapped = Organization.builder()
                .name("Everest")
                .organizationNumber("937219997")
                .build();
        Organization saved = Organization.builder()
                .id(1L)
                .name("Everest")
                .organizationNumber("937219997")
                .build();
        OrganizationResponse response = OrganizationResponse.builder()
                .id(1L)
                .name("Everest")
                .organizationNumber("937219997")
                .locationCount(0)
                .build();

        when(organizationRepository.existsByOrganizationNumber("937219997")).thenReturn(false);
        when(organizationMapper.toEntity(request)).thenReturn(mapped);
        when(organizationRepository.save(any(Organization.class))).thenReturn(saved);
        when(organizationMapper.toResponse(saved)).thenReturn(response);

        OrganizationResponse result = organizationService.create(request);

        assertEquals(1L, result.getId());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void updateCurrentOrganizationSavesChangesBeforeMappingResponse() {
        OrganizationRequest request = new OrganizationRequest();
        request.setName("Updated Everest");
        request.setOrganizationNumber("987654321");

        Organization existing = Organization.builder()
                .id(100L)
                .name("Everest")
                .organizationNumber("123456789")
                .build();
        Organization saved = Organization.builder()
                .id(100L)
                .name("Updated Everest")
                .organizationNumber("987654321")
                .build();
        OrganizationResponse response = OrganizationResponse.builder()
                .id(100L)
                .name("Updated Everest")
                .organizationNumber("987654321")
                .build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);
        when(organizationRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(organizationRepository.existsByOrganizationNumberAndIdNot("987654321", 100L)).thenReturn(false);
        when(organizationRepository.save(existing)).thenReturn(existing);
        when(organizationMapper.toResponse(existing)).thenReturn(response);

        OrganizationResponse result = organizationService.updateCurrentOrganization(request);

        assertEquals(100L, result.getId());
        assertEquals("Updated Everest", existing.getName());
        assertEquals("987654321", existing.getOrganizationNumber());
        verify(authorizationService).assertPermission(Permission.ORGANIZATION_SETTINGS_UPDATE);
    }

    @Test
    void getByIdDeniesAccessToOtherOrganizations() {
        Organization organization = Organization.builder()
                .id(200L)
                .name("Other")
                .organizationNumber("123456789")
                .build();

        when(organizationRepository.findById(200L)).thenReturn(Optional.of(organization));
        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);

        assertThrows(AccessDeniedException.class, () -> organizationService.getById(200L));
    }
}
