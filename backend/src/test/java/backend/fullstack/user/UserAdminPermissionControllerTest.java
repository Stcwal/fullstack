package backend.fullstack.user;

import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.fullstack.config.GlobalExceptionHandler;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionEffect;
import backend.fullstack.permission.model.PermissionScope;
import backend.fullstack.user.dto.AssignProfilesRequest;
import backend.fullstack.user.dto.TemporaryLocationScopeRequest;
import backend.fullstack.user.dto.UserPermissionOverrideRequest;

class UserAdminPermissionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        UserAdminPermissionController controller = new UserAdminPermissionController(userService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void assignProfilesDelegatesToService() throws Exception {
        AssignProfilesRequest request = new AssignProfilesRequest();
        request.setProfileIds(List.of(1L, 2L));
        request.setLocationId(5L);
        request.setStartsAt(LocalDateTime.of(2026, 4, 4, 8, 0));
        request.setEndsAt(LocalDateTime.of(2026, 4, 4, 16, 0));
        request.setReplaceScopeAssignments(true);

        mockMvc.perform(put("/api/admin/users/50/profiles")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profiles assigned"));

        verify(userService).assignProfiles(50L, List.of(1L, 2L), 5L, request.getStartsAt(), request.getEndsAt(), true);
    }

    @Test
    void assignPermissionOverrideDelegatesToService() throws Exception {
        UserPermissionOverrideRequest request = new UserPermissionOverrideRequest();
        request.setPermission(Permission.USERS_UPDATE);
        request.setEffect(PermissionEffect.ALLOW);
        request.setScope(PermissionScope.LOCATION);
        request.setLocationId(5L);
        request.setStartsAt(LocalDateTime.of(2026, 4, 4, 8, 0));
        request.setEndsAt(LocalDateTime.of(2026, 4, 4, 16, 0));
        request.setReason("Shift coverage");

        mockMvc.perform(post("/api/admin/users/50/permissions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permission override assigned"));

        verify(userService).assignTemporaryPermission(
                50L,
                Permission.USERS_UPDATE,
                PermissionEffect.ALLOW,
                PermissionScope.LOCATION,
                5L,
                request.getStartsAt(),
                request.getEndsAt(),
                "Shift coverage"
        );
    }

    @Test
    void assignTemporaryLocationDelegatesToService() throws Exception {
        TemporaryLocationScopeRequest request = new TemporaryLocationScopeRequest();
        request.setLocationId(7L);
        request.setStartsAt(LocalDateTime.of(2026, 4, 4, 8, 0));
        request.setEndsAt(LocalDateTime.of(2026, 4, 4, 16, 0));
        request.setMode(TemporaryAssignmentMode.ACTING);
        request.setReason("Cover absence");

        mockMvc.perform(post("/api/admin/users/50/locations/temporary")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Temporary location scope assigned"));

        verify(userService).assignTemporaryLocationScope(
                50L,
                7L,
                request.getStartsAt(),
                request.getEndsAt(),
                TemporaryAssignmentMode.ACTING,
                "Cover absence"
        );
    }

    @Test
    void assignProfilesValidatesRequiredProfileIds() throws Exception {
        mockMvc.perform(put("/api/admin/users/50/profiles")
                        .contentType(APPLICATION_JSON)
                        .content("{\"profileIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.profileIds").value("At least one profile id is required"));
    }

    @Test
    void completeTemporaryLocationDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/admin/users/50/locations/temporary/11/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Temporary location assignment completed"));

        verify(userService).completeTemporaryLocationScope(50L, 11L);
    }

    @Test
    void confirmTemporaryLocationDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/admin/users/50/locations/temporary/11/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Temporary location assignment confirmed"));

        verify(userService).confirmTemporaryLocationScope(50L, 11L);
    }
}
