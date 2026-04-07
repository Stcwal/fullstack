package backend.fullstack.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class AccessContextServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository;

    private AccessContextService accessContextService;

    @BeforeEach
    void setUp() {
        accessContextService = new AccessContextService(userRepository, locationRepository, userLocationScopeAssignmentRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminGetsAllOrgLocationsPlusActiveTemporaryLocations() {
        User admin = user(1L, 100L, Role.ADMIN, "admin@everest.no");
        setAuthenticatedEmail(admin.getEmail());

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(locationRepository.findIdsByOrganizationId(100L)).thenReturn(List.of(1L, 2L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(1L), any()))
                .thenReturn(List.of(3L));

        List<Long> allowed = accessContextService.getAllowedLocationIds();

        assertEquals(List.of(1L, 2L, 3L), allowed);
    }

    @Test
    void managerGetsEffectiveScopePlusTemporaryLocations() {
        User manager = user(2L, 100L, Role.MANAGER, "manager@everest.no");
        setAuthenticatedEmail(manager.getEmail());

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
        when(userRepository.findEffectiveLocationScopeByUserId(2L)).thenReturn(List.of(4L, 5L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(2L), any()))
                .thenReturn(List.of(6L));

        List<Long> allowed = accessContextService.getAllowedLocationIds();

        assertEquals(List.of(4L, 5L, 6L), allowed);
    }

    @Test
    void assertCanAccessThrowsWhenLocationIsMissing() {
        User staff = user(3L, 100L, Role.STAFF, "staff@everest.no");
        setAuthenticatedEmail(staff.getEmail());

        when(userRepository.findByEmail(staff.getEmail())).thenReturn(Optional.of(staff));
        when(userRepository.findEffectiveLocationScopeByUserId(3L)).thenReturn(List.of(10L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(3L), any()))
                .thenReturn(List.of());

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> accessContextService.assertCanAccess(11L));
    }

    private static void setAuthenticatedEmail(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static User user(Long id, Long organizationId, Role role, String email) {
        Organization organization = Organization.builder()
                .id(organizationId)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return User.builder()
                .id(id)
                .organization(organization)
                .email(email)
                .firstName("First")
                .lastName("Last")
                .passwordHash("hash")
                .role(role)
                .build();
    }
}
