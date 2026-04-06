package backend.fullstack.permission;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.permission.catalog.RolePermissionBindingRepository;
import backend.fullstack.permission.catalog.RolePermissionCatalog;
import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.permission.core.ConditionEvaluator;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.override.UserPermissionOverrideRepository;
import backend.fullstack.permission.profile.PermissionProfileBindingRepository;
import backend.fullstack.permission.profile.UserProfileAssignmentRepository;
import backend.fullstack.permission.dto.CapabilitiesResponse;
import backend.fullstack.training.TrainingRecordRepository;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;
import backend.fullstack.user.UserLocationScopeAssignmentRepository;
import backend.fullstack.user.UserRepository;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository;

    @Mock
    private TrainingRecordRepository trainingRecordRepository;

    private AccessContextService accessContext;
    private TestRolePermissionCatalog rolePermissionCatalog;
    private ConditionEvaluator conditionEvaluator;
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        accessContext = new AccessContextService(userRepository, locationRepository, userLocationScopeAssignmentRepository);
        rolePermissionCatalog = new TestRolePermissionCatalog();
        conditionEvaluator = new ConditionEvaluator(trainingRecordRepository);
        authorizationService = new AuthorizationService(accessContext, rolePermissionCatalog, userRepository, conditionEvaluator);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasPermissionForLocationReturnsFalseWhenLocationNotAllowed() {
        User actor = user(1L, 100L, Role.MANAGER, "actor@everest.no");
        authenticate(actor);

        when(userRepository.findById(actor.getId())).thenReturn(java.util.Optional.of(actor));
        when(userRepository.findEffectiveLocationScopeByUserId(actor.getId())).thenReturn(List.of(10L, 20L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(actor.getId()), any()))
                .thenReturn(List.of());

        rolePermissionCatalog.setEffectivePermissions(null, Set.of(Permission.USERS_UPDATE));
        rolePermissionCatalog.setEffectivePermissions(99L, Set.of(Permission.USERS_UPDATE));

        boolean result = authorizationService.hasPermissionForLocation(Permission.USERS_UPDATE, 99L);

        assertFalse(result);
    }

    @Test
    void assertCanViewUserAllowsOwnUserAndDeniesCrossOrganization() {
        User actor = user(1L, 100L, Role.STAFF, "actor@everest.no");
        authenticate(actor);

        User sameUser = user(1L, 100L, Role.STAFF, "actor@everest.no");
        User otherOrgUser = user(2L, 200L, Role.STAFF, "other@everest.no");

        when(userRepository.findById(actor.getId())).thenReturn(java.util.Optional.of(actor));
        rolePermissionCatalog.setEffectivePermissions(null, Set.of(Permission.USERS_READ_LOCATION));

        assertDoesNotThrow(() -> authorizationService.assertCanViewUser(sameUser));

        backend.fullstack.exceptions.AccessDeniedException ex =
                assertThrows(backend.fullstack.exceptions.AccessDeniedException.class,
                        () -> authorizationService.assertCanViewUser(otherOrgUser));

        assertTrue(ex.getMessage().contains("Cross-organization"));
    }

    @Test
    void assertCanManageUserRoleMatrixEnforced() {
        User supervisor = user(10L, 100L, Role.SUPERVISOR, "supervisor@everest.no");
        User targetManagerInScope = user(12L, 100L, Role.MANAGER, "target-manager-in-scope@everest.no");
        User targetSupervisor = user(11L, 100L, Role.SUPERVISOR, "target-supervisor@everest.no");

        authenticate(supervisor);
        when(userRepository.findById(supervisor.getId())).thenReturn(java.util.Optional.of(supervisor));
        rolePermissionCatalog.setEffectivePermissions(null, Set.of(Permission.USERS_UPDATE));
        when(userRepository.findAdditionalLocationIdsByUserId(supervisor.getId())).thenReturn(List.of(7L));
        when(userRepository.findAdditionalLocationIdsByUserId(targetManagerInScope.getId())).thenReturn(List.of(7L));

        assertDoesNotThrow(() -> authorizationService.assertCanManageUser(targetManagerInScope));

        assertThrows(backend.fullstack.exceptions.AccessDeniedException.class,
                () -> authorizationService.assertCanManageUser(targetSupervisor));

        User manager = user(20L, 100L, Role.MANAGER, "manager@everest.no");
        User targetStaff = user(31L, 100L, Role.STAFF, "target-staff@everest.no");
        User targetManager = user(21L, 100L, Role.MANAGER, "target-manager@everest.no");

        authenticate(manager);
        when(userRepository.findById(manager.getId())).thenReturn(java.util.Optional.of(manager));
        when(userRepository.findAdditionalLocationIdsByUserId(manager.getId())).thenReturn(List.of(9L));
        when(userRepository.findAdditionalLocationIdsByUserId(targetStaff.getId())).thenReturn(List.of(9L));
        assertDoesNotThrow(() -> authorizationService.assertCanManageUser(targetStaff));

        assertThrows(backend.fullstack.exceptions.AccessDeniedException.class,
                () -> authorizationService.assertCanManageUser(targetManager));

        User staff = user(30L, 100L, Role.STAFF, "staff@everest.no");

        authenticate(staff);
        when(userRepository.findById(staff.getId())).thenReturn(java.util.Optional.of(staff));
        assertThrows(backend.fullstack.exceptions.AccessDeniedException.class,
                () -> authorizationService.assertCanManageUser(targetStaff));
    }

    @Test
    void assertCanCreateUserFollowsHierarchy() {
        User supervisor = user(10L, 100L, Role.SUPERVISOR, "supervisor@everest.no");
        authenticate(supervisor);
        when(userRepository.findById(supervisor.getId())).thenReturn(java.util.Optional.of(supervisor));
        when(userRepository.findEffectiveLocationScopeByUserId(supervisor.getId())).thenReturn(List.of(7L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(supervisor.getId()), any()))
                .thenReturn(List.of());
        rolePermissionCatalog.setEffectivePermissions(null, Set.of(Permission.USERS_CREATE));

        assertDoesNotThrow(() -> authorizationService.assertCanCreateUser(Role.STAFF, 7L));

        backend.fullstack.exceptions.AccessDeniedException supervisorEx =
                assertThrows(backend.fullstack.exceptions.AccessDeniedException.class,
                        () -> authorizationService.assertCanCreateUser(Role.SUPERVISOR, 7L));
        assertTrue(supervisorEx.getMessage().contains("Supervisors can only create"));

        User manager = user(20L, 100L, Role.MANAGER, "manager@everest.no");
        authenticate(manager);
        when(userRepository.findById(manager.getId())).thenReturn(java.util.Optional.of(manager));
        when(userRepository.findEffectiveLocationScopeByUserId(manager.getId())).thenReturn(List.of(7L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(manager.getId()), any()))
                .thenReturn(List.of());
        rolePermissionCatalog.setEffectivePermissions(null, Set.of(Permission.USERS_CREATE));

        assertDoesNotThrow(() -> authorizationService.assertCanCreateUser(Role.STAFF, 7L));

        backend.fullstack.exceptions.AccessDeniedException managerEx =
                assertThrows(backend.fullstack.exceptions.AccessDeniedException.class,
                        () -> authorizationService.assertCanCreateUser(Role.MANAGER, 7L));
        assertTrue(managerEx.getMessage().contains("Managers can only create"));

        User admin = user(1L, 100L, Role.ADMIN, "admin@everest.no");
        authenticate(admin);
        when(userRepository.findById(admin.getId())).thenReturn(java.util.Optional.of(admin));
        when(locationRepository.findIdsByOrganizationId(100L)).thenReturn(List.of(7L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(admin.getId()), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> authorizationService.assertCanCreateUser(Role.STAFF, 7L));
    }

    @Test
    void getCurrentCapabilitiesIncludesPerLocationCapabilities() {
        User actor = user(1L, 100L, Role.MANAGER, "actor@everest.no");
        authenticate(actor);

        when(userRepository.findById(actor.getId())).thenReturn(java.util.Optional.of(actor));
        when(userRepository.findEffectiveLocationScopeByUserId(actor.getId())).thenReturn(List.of(7L, 8L));
        when(userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(eq(actor.getId()), any()))
                .thenReturn(List.of());

        rolePermissionCatalog.setActiveProfileNames(List.of("Shift Leader"));
        rolePermissionCatalog.setEffectivePermissions(null, Set.of(Permission.CHECKLISTS_READ));
        rolePermissionCatalog.setEffectivePermissions(7L, Set.of(Permission.CHECKLISTS_READ, Permission.CHECKLISTS_COMPLETE));
        rolePermissionCatalog.setEffectivePermissions(8L, Set.of(Permission.CHECKLISTS_READ));

        CapabilitiesResponse response = authorizationService.getCurrentCapabilities();

        assertEquals(2, response.getLocations().size());
        assertTrue(response.getLocations().stream().anyMatch(l -> l.getLocationId().equals(7L)
                && l.getPermissions().contains("checklists.complete")));
        assertTrue(response.getLocations().stream().anyMatch(l -> l.getLocationId().equals(8L)
                && l.getPermissions().contains("checklists.read")));
        assertTrue(response.getPermissionScopeLocationIds().containsKey("checklists.read"));
    }

    private static void authenticate(User actor) {
        JwtPrincipal principal = new JwtPrincipal(
                actor.getId(),
                actor.getEmail(),
                actor.getRole(),
                actor.getOrganizationId(),
                List.of()
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, actor.getAuthorities())
        );
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

    private static final class TestRolePermissionCatalog extends RolePermissionCatalog {

        private final Map<Long, Set<Permission>> byLocation = new HashMap<>();
        private List<String> activeProfileNames = List.of();

        private TestRolePermissionCatalog() {
            super(
                    mock(RolePermissionBindingRepository.class),
                    mock(UserProfileAssignmentRepository.class),
                    mock(PermissionProfileBindingRepository.class),
                    mock(UserPermissionOverrideRepository.class),
                    mock(UserLocationScopeAssignmentRepository.class)
            );
        }

        void setEffectivePermissions(Long locationId, Set<Permission> permissions) {
            byLocation.put(locationId, permissions);
        }

        void setActiveProfileNames(List<String> names) {
            this.activeProfileNames = names;
        }

        @Override
        public Set<Permission> getEffectivePermissions(User user, Long locationId) {
            return byLocation.getOrDefault(locationId, Set.of());
        }

        @Override
        public List<String> getActiveProfileNames(User user) {
            return activeProfileNames;
        }
    }
}
