package backend.fullstack.auth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.permission.dto.CapabilitiesResponse;
import backend.fullstack.user.role.Role;

class AuthCapabilitiesControllerTest {

    private MockMvc mockMvc;
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = Mockito.mock(AuthorizationService.class);
        AuthCapabilitiesController controller = new AuthCapabilitiesController(authorizationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCapabilitiesReturnsWrappedResponse() throws Exception {
        CapabilitiesResponse response = new CapabilitiesResponse();
        response.setRole(Role.SUPERVISOR);
        response.setOrganizationId(10L);
        response.setAllowedLocationIds(List.of(1L, 2L));
        response.setPermissions(List.of("users.read.location", "training.records.read"));
        response.setActiveProfileNames(List.of("Shift Leader"));

        when(authorizationService.getCurrentCapabilities()).thenReturn(response);

        mockMvc.perform(get("/api/auth/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Capabilities fetched"))
                .andExpect(jsonPath("$.data.role").value("SUPERVISOR"))
                .andExpect(jsonPath("$.data.organizationId").value(10))
                .andExpect(jsonPath("$.data.allowedLocationIds[0]").value(1))
                .andExpect(jsonPath("$.data.permissions[0]").value("users.read.location"))
                .andExpect(jsonPath("$.data.activeProfileNames[0]").value("Shift Leader"));

        verify(authorizationService).getCurrentCapabilities();
    }
}
