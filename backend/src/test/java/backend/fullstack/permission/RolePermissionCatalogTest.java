package backend.fullstack.permission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.organization.Organization;
import backend.fullstack.permission.catalog.RolePermissionBindingRepository;
import backend.fullstack.permission.catalog.RolePermissionCatalog;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionEffect;
import backend.fullstack.permission.model.PermissionScope;
import backend.fullstack.permission.override.UserPermissionOverride;
import backend.fullstack.permission.override.UserPermissionOverrideRepository;
import backend.fullstack.permission.profile.PermissionProfile;
import backend.fullstack.permission.profile.PermissionProfileBinding;
import backend.fullstack.permission.profile.PermissionProfileBindingRepository;
import backend.fullstack.permission.profile.UserProfileAssignment;
import backend.fullstack.permission.profile.UserProfileAssignmentRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserLocationScopeAssignmentRepository;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class RolePermissionCatalogTest {

    @Mock
    private RolePermissionBindingRepository rolePermissionBindingRepository;

    @Mock
    private UserProfileAssignmentRepository userProfileAssignmentRepository;

    @Mock
    private PermissionProfileBindingRepository permissionProfileBindingRepository;

    @Mock
    private UserPermissionOverrideRepository userPermissionOverrideRepository;

    @Mock
    private UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository;

    private RolePermissionCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new RolePermissionCatalog(
                rolePermissionBindingRepository,
                userProfileAssignmentRepository,
                permissionProfileBindingRepository,
                userPermissionOverrideRepository,
                userLocationScopeAssignmentRepository
        );

                lenient().when(userProfileAssignmentRepository.findActiveByUserId(any(), any(LocalDateTime.class))).thenReturn(List.of());
                lenient().when(permissionProfileBindingRepository.findByProfile_IdIn(anyList())).thenReturn(List.of());
                lenient().when(userPermissionOverrideRepository.findActiveByUserId(any(), any(LocalDateTime.class))).thenReturn(List.of());
                lenient().when(userLocationScopeAssignmentRepository.existsActiveActingAssignment(any(), any(), any(LocalDateTime.class))).thenReturn(false);
    }

    @Test
    void roleBaselineReturnedWhenNoProfilesOrOverrides() {
        when(rolePermissionBindingRepository.existsByRole(Role.STAFF)).thenReturn(false);

        User staff = user(1L, Role.STAFF);
        Set<Permission> effective = catalog.getEffectivePermissions(staff, null);

        assertTrue(effective.contains(Permission.LOGS_TEMPERATURE_CREATE));
        assertTrue(effective.contains(Permission.CHECKLISTS_COMPLETE));
    }

    @Test
    void multipleProfilesMergeAdditively() {
        when(rolePermissionBindingRepository.existsByRole(Role.STAFF)).thenReturn(false);

        User staff = user(2L, Role.STAFF);
        PermissionProfile profile1 = profile(101L, "Shift Leader");
        PermissionProfile profile2 = profile(102L, "Kitchen Supervisor");

        UserProfileAssignment assignment1 = UserProfileAssignment.builder().user(staff).profile(profile1).build();
        UserProfileAssignment assignment2 = UserProfileAssignment.builder().user(staff).profile(profile2).build();

        when(userProfileAssignmentRepository.findActiveByUserId(eq(staff.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(assignment1, assignment2));

        PermissionProfileBinding binding1 = PermissionProfileBinding.builder()
                .profile(profile1)
                .permission(Permission.CHECKLISTS_READ)
                .scope(PermissionScope.ORGANIZATION)
                .build();

        PermissionProfileBinding binding2 = PermissionProfileBinding.builder()
                .profile(profile2)
                .permission(Permission.REPORTS_READ)
                .scope(PermissionScope.ORGANIZATION)
                .build();

        when(permissionProfileBindingRepository.findByProfile_IdIn(anyList()))
                .thenReturn(List.of(binding1, binding2));

        Set<Permission> effective = catalog.getEffectivePermissions(staff, null);

        assertTrue(effective.contains(Permission.CHECKLISTS_READ));
        assertTrue(effective.contains(Permission.REPORTS_READ));
    }

    @Test
    void denyOverrideRemovesGrantedPermission() {
        when(rolePermissionBindingRepository.existsByRole(Role.MANAGER)).thenReturn(false);

        User manager = user(3L, Role.MANAGER);

        UserPermissionOverride deny = UserPermissionOverride.builder()
                .user(manager)
                .permission(Permission.LOGS_TEMPERATURE_CREATE)
                .effect(PermissionEffect.DENY)
                .scope(PermissionScope.ORGANIZATION)
                .build();

        when(userPermissionOverrideRepository.findActiveByUserId(eq(manager.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(deny));

        Set<Permission> effective = catalog.getEffectivePermissions(manager, null);

        assertFalse(effective.contains(Permission.LOGS_TEMPERATURE_CREATE));
    }

    @Test
    void locationScopedProfileAppliesOnlyOnMatchingLocation() {
        when(rolePermissionBindingRepository.existsByRole(Role.STAFF)).thenReturn(false);

        User staff = user(4L, Role.STAFF);
        PermissionProfile profile = profile(103L, "Temp Reports");

        UserProfileAssignment assignment = UserProfileAssignment.builder().user(staff).profile(profile).build();
        when(userProfileAssignmentRepository.findActiveByUserId(eq(staff.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(assignment));

        PermissionProfileBinding locationBinding = PermissionProfileBinding.builder()
                .profile(profile)
                .permission(Permission.REPORTS_EXPORT)
                .scope(PermissionScope.LOCATION)
                .locationId(5L)
                .build();

        when(permissionProfileBindingRepository.findByProfile_IdIn(anyList())).thenReturn(List.of(locationBinding));

        Set<Permission> locationFive = catalog.getEffectivePermissions(staff, 5L);
        Set<Permission> locationSix = catalog.getEffectivePermissions(staff, 6L);

        assertTrue(locationFive.contains(Permission.REPORTS_EXPORT));
        assertFalse(locationSix.contains(Permission.REPORTS_EXPORT));
    }

    @Test
    void actingModeDisablesRoleBaselineAtLocation() {
        when(rolePermissionBindingRepository.existsByRole(Role.MANAGER)).thenReturn(false);

        User manager = user(5L, Role.MANAGER);

        when(userLocationScopeAssignmentRepository.existsActiveActingAssignment(eq(manager.getId()), eq(7L), any(LocalDateTime.class)))
                .thenReturn(true);

        Set<Permission> actingAtLocation = catalog.getEffectivePermissions(manager, 7L);
        Set<Permission> notActingAtLocation = catalog.getEffectivePermissions(manager, 8L);

        assertFalse(actingAtLocation.contains(Permission.USERS_CREATE));
        assertTrue(notActingAtLocation.contains(Permission.USERS_CREATE));
    }

    private static User user(Long id, Role role) {
        Organization organization = Organization.builder()
                .id(100L)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return User.builder()
                .id(id)
                .organization(organization)
                .email("user" + id + "@everest.no")
                .firstName("First")
                .lastName("Last")
                .passwordHash("hash")
                .role(role)
                .build();
    }

    private static PermissionProfile profile(Long id, String name) {
        Organization organization = Organization.builder()
                .id(100L)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return PermissionProfile.builder()
                .id(id)
                .organization(organization)
                .name(name)
                .description(name)
                .isActive(true)
                .build();
    }
}
