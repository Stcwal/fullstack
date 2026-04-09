package backend.fullstack.user;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import backend.fullstack.config.JwtPrincipal;
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
    void usesJwtLocationScopeBeforeRepositoryLookup() {
        JwtPrincipal principal = new JwtPrincipal(
                2L,
                "manager@everest.no",
                Role.MANAGER,
                100L,
                List.of(4L, 5L)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

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

    @Test
    void assertCanAccessThrowsWhenLocationIsNull() {
        assertThrows(AccessDeniedException.class, () -> accessContextService.assertCanAccess(null));
    }

    @Test
    void getCurrentOrganizationIdAndRolePreferJwtClaims() {
        JwtPrincipal principal = new JwtPrincipal(
                77L,
                "jwt@everest.no",
                Role.SUPERVISOR,
                555L,
                List.of(9L)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        assertEquals(555L, accessContextService.getCurrentOrganizationId());
        assertEquals(Role.SUPERVISOR, accessContextService.getCurrentRole());
    }

    @Test
    void assertHasRoleAllowsMatchingRoleAndRejectsOthers() {
        JwtPrincipal principal = new JwtPrincipal(
                88L,
                "manager@everest.no",
                Role.MANAGER,
                100L,
                List.of(1L)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        accessContextService.assertHasRole(Role.MANAGER, Role.ADMIN);

        assertThrows(AccessDeniedException.class, () -> accessContextService.assertHasRole(Role.ADMIN));
    }

    @Test
    void getCurrentUserResolvesFromJwtUserIdWhenPrincipalHasNoEntity() {
        JwtPrincipal principal = new JwtPrincipal(
                123L,
                "jwt-user@everest.no",
                Role.STAFF,
                100L,
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        User stored = user(123L, 100L, Role.STAFF, "jwt-user@everest.no");
        when(userRepository.findById(123L)).thenReturn(Optional.of(stored));

        User resolved = accessContextService.getCurrentUser();

        assertEquals(123L, resolved.getId());
        assertEquals("jwt-user@everest.no", resolved.getEmail());
    }

    @Test
    void getCurrentUserThrowsWhenUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> accessContextService.getCurrentUser());
    }

    @Test
    void getAllowedLocationIdsDeduplicatesOverlappingScopes() {
        JwtPrincipal principal = new JwtPrincipal(
                2L,
                "manager@everest.no",
                Role.MANAGER,
                100L,
                List.of(4L, 5L)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(2L), any()))
                .thenReturn(List.of(5L, 6L));

        List<Long> allowed = accessContextService.getAllowedLocationIds();

        assertEquals(List.of(4L, 5L, 6L), allowed);
        assertTrue(allowed.stream().distinct().count() == allowed.size());
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
