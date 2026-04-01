package backend.fullstack.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.exceptions.RoleException;
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.permission.AuthorizationService;
import backend.fullstack.permission.PermissionProfile;
import backend.fullstack.permission.PermissionProfileRepository;
import backend.fullstack.permission.UserPermissionOverrideRepository;
import backend.fullstack.permission.UserProfileAssignment;
import backend.fullstack.permission.UserProfileAssignmentRepository;
import backend.fullstack.user.dto.UserMapper;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class UserServicePermissionFlowTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PermissionProfileRepository permissionProfileRepository;
    @Mock
    private UserProfileAssignmentRepository userProfileAssignmentRepository;
    @Mock
    private UserPermissionOverrideRepository userPermissionOverrideRepository;
    @Mock
    private UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

        private AccessContextService accessContext;
        private AuthorizationService authorizationService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        accessContext = new AccessContextService(userRepository, locationRepository, userLocationScopeAssignmentRepository);
        authorizationService = new PermitAllAuthorizationService();
        userService = new UserService(
                userRepository,
                locationRepository,
                userMapper,
                accessContext,
                authorizationService,
                permissionProfileRepository,
                userProfileAssignmentRepository,
                userPermissionOverrideRepository,
                userLocationScopeAssignmentRepository,
                passwordEncoder
        );

        setAdminPrincipal();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void assignProfilesWithLocationReplacesOnlyThatLocationScope() {
        User target = targetUser();
        Location location = location(5L, 100L);

        when(userRepository.findById(50L)).thenReturn(Optional.of(target));
        when(locationRepository.findByIdAndOrganizationId(5L, 100L)).thenReturn(Optional.of(location));

        List<Long> profileIds = List.of(1L, 2L);
        List<PermissionProfile> profiles = List.of(profile(1L), profile(2L));
        when(permissionProfileRepository.findByIdInAndOrganization_IdAndIsActiveTrue(profileIds, 100L))
                .thenReturn(profiles);

        userService.assignProfiles(50L, profileIds, 5L, null, null, true);

        verify(userProfileAssignmentRepository).deleteAllByUserIdAndLocationId(50L, 5L);
        verify(userProfileAssignmentRepository, never()).deleteAllGlobalByUserId(any());

        ArgumentCaptor<UserProfileAssignment> captor = ArgumentCaptor.forClass(UserProfileAssignment.class);
        verify(userProfileAssignmentRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(5L, captor.getAllValues().get(0).getLocation().getId());
    }

    @Test
    void assignProfilesGlobalReplaceOnlyGlobalAssignments() {
        User target = targetUser();

        when(userRepository.findById(50L)).thenReturn(Optional.of(target));

        List<Long> profileIds = List.of(1L);
        when(permissionProfileRepository.findByIdInAndOrganization_IdAndIsActiveTrue(profileIds, 100L))
                .thenReturn(List.of(profile(1L)));

        userService.assignProfiles(50L, profileIds, null, null, null, true);

        verify(userProfileAssignmentRepository).deleteAllGlobalByUserId(50L);
        verify(userProfileAssignmentRepository, never()).deleteAllByUserIdAndLocationId(any(), any());
    }

    @Test
    void assignTemporaryLocationScopeSetsScheduledOrActiveStatus() {
        User target = targetUser();
        Location location = location(5L, 100L);

        when(userRepository.findById(50L)).thenReturn(Optional.of(target));
        when(locationRepository.findByIdAndOrganizationId(5L, 100L)).thenReturn(Optional.of(location));

        LocalDateTime future = LocalDateTime.now().plusHours(2);
        userService.assignTemporaryLocationScope(50L, 5L, future, future.plusHours(8), TemporaryAssignmentMode.INHERIT, "shift");

        ArgumentCaptor<UserLocationScopeAssignment> captor = ArgumentCaptor.forClass(UserLocationScopeAssignment.class);
        verify(userLocationScopeAssignmentRepository).save(captor.capture());
        assertEquals(TemporaryAssignmentStatus.SCHEDULED, captor.getValue().getStatus());

        userService.assignTemporaryLocationScope(50L, 5L, null, null, TemporaryAssignmentMode.ACTING, "cover");

        verify(userLocationScopeAssignmentRepository, org.mockito.Mockito.times(2)).save(captor.capture());
                List<UserLocationScopeAssignment> saved = captor.getAllValues();
                assertEquals(TemporaryAssignmentStatus.ACTIVE, saved.get(saved.size() - 1).getStatus());
    }

    @Test
    void completeAndConfirmTemporaryLocationLifecycleEnforced() {
        User target = targetUser();

        when(userRepository.findById(50L)).thenReturn(Optional.of(target));

        UserLocationScopeAssignment assignment = UserLocationScopeAssignment.builder()
                .id(900L)
                .user(target)
                .location(location(5L, 100L))
                .status(TemporaryAssignmentStatus.ACTIVE)
                .mode(TemporaryAssignmentMode.INHERIT)
                .build();

        when(userLocationScopeAssignmentRepository.findById(900L)).thenReturn(Optional.of(assignment));

        userService.completeTemporaryLocationScope(50L, 900L);

        assertEquals(TemporaryAssignmentStatus.COMPLETED, assignment.getStatus());
        assertNotNull(assignment.getCompletedAt());

        UserLocationScopeAssignment notCompleted = UserLocationScopeAssignment.builder()
                .id(901L)
                .user(target)
                .location(location(5L, 100L))
                .status(TemporaryAssignmentStatus.ACTIVE)
                .mode(TemporaryAssignmentMode.INHERIT)
                .build();

        when(userLocationScopeAssignmentRepository.findById(901L)).thenReturn(Optional.of(notCompleted));

        assertThrows(RoleException.class, () -> userService.confirmTemporaryLocationScope(50L, 901L));
    }

    @Test
    void validateWindowRejectsInvalidRange() {
        User target = targetUser();

        when(userRepository.findById(50L)).thenReturn(Optional.of(target));

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusMinutes(10);

        assertThrows(RoleException.class,
                () -> userService.assignTemporaryPermission(
                        50L,
                        backend.fullstack.permission.Permission.USERS_UPDATE,
                        backend.fullstack.permission.PermissionEffect.ALLOW,
                        backend.fullstack.permission.PermissionScope.ORGANIZATION,
                        null,
                        start,
                        end,
                        "invalid"
                ));
    }

    private static User targetUser() {
        Organization organization = Organization.builder()
                .id(100L)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return User.builder()
                .id(50L)
                .organization(organization)
                .email("target@everest.no")
                .firstName("Target")
                .lastName("User")
                .passwordHash("hash")
                .role(Role.STAFF)
                .build();
    }

    private static PermissionProfile profile(Long id) {
        Organization organization = Organization.builder()
                .id(100L)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return PermissionProfile.builder()
                .id(id)
                .organization(organization)
                .name("Profile-" + id)
                .description("Profile")
                .isActive(true)
                .build();
    }

    private static Location location(Long id, Long orgId) {
        Organization organization = Organization.builder()
                .id(orgId)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return Location.builder()
                .id(id)
                .organization(organization)
                .name("Loc")
                .address("Street")
                .build();
    }

        private static void setAdminPrincipal() {
                JwtPrincipal principal = new JwtPrincipal(
                                999L,
                                "admin@everest.no",
                                Role.ADMIN,
                                100L,
                                List.of()
                );

                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                );
        }

        private static final class PermitAllAuthorizationService extends AuthorizationService {

                private PermitAllAuthorizationService() {
                        super(null, null, null, null);
                }

                @Override
                public void assertCanManageUser(User targetUser) {
                        // Intentionally no-op for focused UserService unit tests.
                }
        }
}
