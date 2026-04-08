package backend.fullstack.user;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.fullstack.config.GlobalExceptionHandler;
import backend.fullstack.location.dto.AssignLocationsRequest;
import backend.fullstack.user.dto.ChangePasswordRequest;
import backend.fullstack.user.dto.CreateUserRequest;
import backend.fullstack.user.dto.UpdateUserProfileRequest;
import backend.fullstack.user.dto.UserResponse;
import backend.fullstack.user.role.Role;
import backend.fullstack.user.role.UpdateRoleRequest;

class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TestUserService userService;

    @BeforeEach
    void setUp() {
        userService = new TestUserService();
        UserController controller = new UserController(userService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void getAllInOrganizationReturnsWrappedList() throws Exception {
        userService.allInOrganizationResponse = List.of(response(1L), response(2L));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users retrieved"))
                .andExpect(jsonPath("$.data", hasSize(2)));

        assert userService.getAllCalled;
    }

    @Test
    void getMyProfileReturnsWrappedResponse() throws Exception {
        userService.myProfileResponse = response(1L);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile retrieved"))
                .andExpect(jsonPath("$.data.id").value(1));

        assert userService.getMyProfileCalled;
    }

    @Test
    void getByIdReturnsWrappedResponse() throws Exception {
        userService.getByIdResponse = response(2L);

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User retrieved"))
                .andExpect(jsonPath("$.data.id").value(2));

        assert userService.lastId.equals(2L);
    }

    @Test
    void createReturnsWrappedResponse() throws Exception {
        CreateUserRequest request = createRequest();
        userService.createResponse = response(9L);

        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.email").value("staff@everest.no"));
    }

    @Test
    void updateProfileReturnsWrappedResponse() throws Exception {
        UpdateUserProfileRequest request = updateProfileRequest();
        userService.updateProfileResponse = response(3L);

        mockMvc.perform(put("/api/users/3")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated"))
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void updateRoleReturnsWrappedResponse() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(Role.MANAGER);
        request.setLocationId(5L);

        userService.updateRoleResponse = response(3L);

        mockMvc.perform(put("/api/users/3/role")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User role updated"))
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void assignAdditionalLocationsReturnsWrappedResponse() throws Exception {
        AssignLocationsRequest request = new AssignLocationsRequest();
        request.setLocationIds(List.of(1L, 2L));

        userService.assignLocationsResponse = response(3L);

        mockMvc.perform(put("/api/users/3/locations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User locations updated"))
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void changePasswordDelegatesToService() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPassword1");
        request.setNewPassword("NewPassword1");

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed"));

        assert userService.changePasswordCalled;
    }

    @Test
    void deactivateDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/users/3/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deactivated"));

        assert userService.deactivatedId.equals(3L);
    }

    @Test
    void reactivateDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/users/3/reactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User reactivated"));

        assert userService.reactivatedId.equals(3L);
    }

    @Test
    void resendInviteDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/users/3/resend-invite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invite resent"));

        assert userService.resendInviteId.equals(3L);
    }

    @Test
    void createReturnsValidationErrorForMissingFields() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.firstName").value("First name is required"))
                .andExpect(jsonPath("$.fieldErrors.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email is required"))
                .andExpect(jsonPath("$.fieldErrors.role").value("Role is required"));
    }

    private static CreateUserRequest createRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Ola");
        request.setLastName("Nordmann");
        request.setEmail("staff@everest.no");
        request.setRole(Role.STAFF);
        request.setLocationId(1L);
        return request;
    }

    private static UpdateUserProfileRequest updateProfileRequest() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Ola");
        request.setLastName("Nordmann");
        return request;
    }

    private static UserResponse response(Long id) {
        return UserResponse.builder()
                .id(id)
                .firstName("Ola")
                .lastName("Nordmann")
                .email("staff@everest.no")
                .role(Role.STAFF)
                .homeLocationId(1L)
                .homeLocationName("Trondheim Office")
                .additionalLocationIds(List.of(2L, 3L))
                .organizationName("Everest AS")
                .organizationId(10L)
                .isActive(true)
                .createdAt(LocalDateTime.of(2026, 4, 4, 12, 0))
                .build();
    }

    private static final class TestUserService extends UserService {

        private List<UserResponse> allInOrganizationResponse = List.of();
        private UserResponse myProfileResponse;
        private UserResponse getByIdResponse;
        private UserResponse createResponse;
        private UserResponse updateProfileResponse;
        private UserResponse updateRoleResponse;
        private UserResponse assignLocationsResponse;
        private boolean getAllCalled;
        private boolean getMyProfileCalled;
        private boolean changePasswordCalled;
        private Long lastId;
        private Long deactivatedId;
        private Long reactivatedId;
        private Long resendInviteId;

        private TestUserService() {
            super(null, null, null, null, null, null, null, null, null, null, null);
        }

        @Override
        public List<UserResponse> getAllInOrganization() {
            getAllCalled = true;
            return allInOrganizationResponse;
        }

        @Override
        public UserResponse getMyProfile() {
            getMyProfileCalled = true;
            return myProfileResponse;
        }

        @Override
        public UserResponse getById(Long id) {
            lastId = id;
            return getByIdResponse;
        }

        @Override
        public UserResponse create(CreateUserRequest request) {
            return createResponse;
        }

        @Override
        public UserResponse updateProfile(Long id, UpdateUserProfileRequest request) {
            lastId = id;
            return updateProfileResponse;
        }

        @Override
        public UserResponse updateRole(Long id, UpdateRoleRequest request) {
            lastId = id;
            return updateRoleResponse;
        }

        @Override
        public UserResponse assignAdditionalLocations(Long id, AssignLocationsRequest request) {
            lastId = id;
            return assignLocationsResponse;
        }

        @Override
        public void changePassword(ChangePasswordRequest request) {
            changePasswordCalled = true;
        }

        @Override
        public void deactivate(Long id) {
            deactivatedId = id;
        }

        @Override
        public void reactivate(Long id) {
            reactivatedId = id;
        }

        @Override
        public void resendInvite(Long id) {
            resendInviteId = id;
        }
    }
}
