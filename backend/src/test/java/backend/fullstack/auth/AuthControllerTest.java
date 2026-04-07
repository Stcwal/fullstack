package backend.fullstack.auth;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.fullstack.config.JwtProperties;
import backend.fullstack.config.JwtUtil;
import backend.fullstack.organization.Organization;
import backend.fullstack.user.User;
import backend.fullstack.user.role.Role;

class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        TestAuthService authService = new TestAuthService();
        TestJwtUtil jwtUtil = new TestJwtUtil();
        AuthenticationManager authenticationManager = authentication -> {
            User principal = buildUser(42L, "admin@everest.no", 10L);
            return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        };

        AuthController controller = new AuthController(authService, jwtUtil, authenticationManager);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginReturnsApiResponseAndJwtCookie() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@everest.no");
        request.setPassword("Admin123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("jwt=test-token")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.userId").value(42))
                .andExpect(jsonPath("$.data.organizationId").value(10))
                .andExpect(jsonPath("$.data.allowedLocationIds[0]").value(1))
                .andExpect(jsonPath("$.data.token").value("test-token"));
    }

    @Test
    void registerBootstrapReturnsCreatedResponse() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@everest.no");
        request.setPassword("Admin123!");
        request.setFirstName("Ola");
        request.setLastName("Nordmann");
        request.setOrganizationId(1L);
        request.setRole(Role.ADMIN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bootstrap admin created"))
                .andExpect(jsonPath("$.data.userId").value(7));
    }

    @Test
    void logoutClearsJwtCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out"));
    }

    private static User buildUser(Long userId, String email, Long orgId) {
        Organization organization = Organization.builder()
                .id(orgId)
                .name("Everest")
                .organizationNumber("123456789")
                .build();

        return User.builder()
                .id(userId)
                .email(email)
                .firstName("Admin")
                .lastName("User")
                .passwordHash("hash")
                .organization(organization)
                .role(Role.ADMIN)
                .build();
    }

    private static final class TestAuthService extends AuthService {

        private TestAuthService() {
            super(null, null, null, null);
        }

        @Override
        public User registerBootstrapAdmin(RegisterRequest request) {
            return buildUser(7L, request.getEmail(), request.getOrganizationId());
        }

        @Override
        public LoginResponse buildLoginResponse(User user) {
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole());
            response.setOrganizationId(user.getOrganizationId());
            response.setPrimaryLocationId(user.getHomeLocationId());
            response.setAllowedLocationIds(List.of(1L, 2L));
            return response;
        }
    }

    private static final class TestJwtUtil extends JwtUtil {

        private TestJwtUtil() {
            super(new JwtProperties());
        }

        @Override
        public String generateToken(String email, Long userId, String role, Long organizationId, List<Long> locationIds) {
            return "test-token";
        }

        @Override
        public ResponseCookie generateJwtCookieFromToken(String token) {
            return ResponseCookie.from("jwt", token).build();
        }

        @Override
        public ResponseCookie generateJwtCookie(
                String email,
                Long userId,
                String role,
                Long organizationId,
                List<Long> locationIds
        ) {
            return ResponseCookie.from("jwt", "test-token")
                    .httpOnly(true)
                    .path("/api")
                    .maxAge(3600)
                    .build();
        }

        @Override
        public ResponseCookie getCleanJwtCookie() {
            return ResponseCookie.from("jwt", "")
                    .httpOnly(true)
                    .path("/api")
                    .maxAge(0)
                    .build();
        }
    }
}
