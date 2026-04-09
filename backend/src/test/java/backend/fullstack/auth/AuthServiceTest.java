package backend.fullstack.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import backend.fullstack.exceptions.LocationException;
import backend.fullstack.exceptions.RoleException;
import backend.fullstack.exceptions.UserConflictException;
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, organizationRepository, locationRepository, passwordEncoder);
    }

    @Test
    void registerBootstrapAdminRejectsWhenUsersAlreadyExist() {
        RegisterRequest request = registerRequest(Role.ADMIN, 1L);
        when(userRepository.count()).thenReturn(1L);

        assertThrows(AccessDeniedException.class, () -> authService.registerBootstrapAdmin(request));
    }

    @Test
    void registerBootstrapAdminRejectsNonAdminRole() {
        RegisterRequest request = registerRequest(Role.MANAGER, 1L);
        when(userRepository.count()).thenReturn(0L);

        assertThrows(RoleException.class, () -> authService.registerBootstrapAdmin(request));
    }

    @Test
    void registerBootstrapAdminRejectsDuplicateEmail() {
        RegisterRequest request = registerRequest(Role.ADMIN, 1L);
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.existsByEmail("admin@everest.no")).thenReturn(true);

        assertThrows(UserConflictException.class, () -> authService.registerBootstrapAdmin(request));
    }

    @Test
    void registerBootstrapAdminRejectsLocationFromAnotherOrganization() {
        RegisterRequest request = registerRequest(Role.ADMIN, 2L);
        Organization organization = organization(1L);
        Location otherOrgLocation = location(2L, 999L);

        when(userRepository.count()).thenReturn(0L);
        when(userRepository.existsByEmail("admin@everest.no")).thenReturn(false);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(otherOrgLocation));

        assertThrows(LocationException.class, () -> authService.registerBootstrapAdmin(request));
    }

    @Test
    void registerBootstrapAdminPersistsAdminWithEncodedPasswordAndLocation() {
        RegisterRequest request = registerRequest(Role.ADMIN, 2L);
        Organization organization = organization(1L);
        Location location = location(2L, 1L);

        when(userRepository.count()).thenReturn(0L);
        when(userRepository.existsByEmail("admin@everest.no")).thenReturn(false);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(passwordEncoder.encode("Admin123!")).thenReturn("encoded");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User saved = authService.registerBootstrapAdmin(request);

        assertEquals("admin@everest.no", saved.getEmail());
        assertEquals(Role.ADMIN, saved.getRole());
        assertEquals("encoded", saved.getPasswordHash());
        assertEquals(1L, saved.getOrganizationId());
        assertEquals(2L, saved.getHomeLocationId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void buildLoginResponseReturnsAllOrgLocationsForAdmin() {
        User admin = User.builder()
                .id(10L)
                .email("admin@everest.no")
                .firstName("Kari")
                .lastName("Larsen")
                .role(Role.ADMIN)
                .organization(organization(1L))
                .build();

        when(locationRepository.findIdsByOrganizationId(1L)).thenReturn(List.of(1L, 2L, 3L));

        LoginResponse response = authService.buildLoginResponse(admin);

        assertEquals(10L, response.getUserId());
        assertEquals(Role.ADMIN, response.getRole());
        assertEquals(List.of(1L, 2L, 3L), response.getAllowedLocationIds());
    }

    @Test
    void buildLoginResponseMergesHomeAndAdditionalLocationsForNonAdmin() {
        User manager = User.builder()
                .id(11L)
                .email("manager@everest.no")
                .firstName("Ola")
                .lastName("Nordmann")
                .role(Role.MANAGER)
                .organization(organization(1L))
                .homeLocation(location(7L, 1L))
                .build();

        when(userRepository.findAdditionalLocationIdsByUserId(11L)).thenReturn(List.of(7L, 8L, 9L));

        LoginResponse response = authService.buildLoginResponse(manager);

        assertEquals(7L, response.getPrimaryLocationId());
        assertEquals(List.of(7L, 8L, 9L), response.getAllowedLocationIds());
    }

    private static RegisterRequest registerRequest(Role role, Long primaryLocationId) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@everest.no");
        request.setPassword("Admin123!");
        request.setFirstName("Ola");
        request.setLastName("Nordmann");
        request.setOrganizationId(1L);
        request.setPrimaryLocationId(primaryLocationId);
        request.setRole(role);
        return request;
    }

    private static Organization organization(Long id) {
        return Organization.builder()
                .id(id)
                .name("Everest")
                .organizationNumber("937219997")
                .build();
    }

    private static Location location(Long id, Long orgId) {
        return Location.builder()
                .id(id)
                .organization(organization(orgId))
                .name("Loc")
                .address("Street")
                .build();
    }
}