package backend.fullstack.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtUtil;
import backend.fullstack.auth.invite.UserInviteService;
import backend.fullstack.user.User;
import jakarta.validation.Valid;

/**
 * Controller for handling authentication-related endpoints: login, registration, and logout.
 *
 * @version 1.0
 * @since 29.03.26
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
        private final UserInviteService userInviteService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            AuthService authService,
                        UserInviteService userInviteService,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager
    ) {
        this.authService = authService;
                this.userInviteService = userInviteService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Authenticates the user and returns a JWT token in an HTTP-only cookie. The response body contains user details and allowed location IDs.
     * The JWT cookie is set with the following claims: email, userId, role, organizationId, and allowedLocationIds. The cookie is HTTP-only and has a max age of 24 hours.
     *
     * @param request The login request containing email and password.
     * @return A ResponseEntity containing an ApiResponse with a LoginResponse payload, and a Set-Cookie header with the JWT token if authentication is successful.
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        LoginResponse loginResponse = authService.buildLoginResponse(user);

        ResponseCookie jwtCookie = jwtUtil.generateJwtCookie(
                user.getEmail(),
                user.getId(),
                user.getRole().name(),
                user.getOrganizationId(),
                loginResponse.getAllowedLocationIds()
        );

        return ResponseEntity.ok()
                .header("Set-Cookie", jwtCookie.toString())
                .body(ApiResponse.success("Login successful", loginResponse));
    }

    /**
     * Registers a new bootstrap admin user. This endpoint is only available when no users exist in the system. The request must contain valid email, password, first name, last name, organization ID, and role (which must be ADMIN). If the registration is successful, a LoginResponse is returned in the response body with a 201 status code.
     *
     * @param request The registration request containing email, password, first name, last name, organization ID, primary location ID (optional), and role (must be ADMIN).
     * @return A ResponseEntity containing an ApiResponse with a LoginResponse payload if registration is successful, or an error message if the request is invalid or if users already exist in the system.
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Registers a new bootstrap admin user. This endpoint is only available when no users exist in the system.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registration successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registerBootstrapAdmin(request);
        LoginResponse response = authService.buildLoginResponse(user);
        return ResponseEntity.status(201).body(ApiResponse.success("Bootstrap admin created", response));
    }

    /**
     * Logs out the user by clearing the JWT cookie and security context. The response will include a Set-Cookie header with an expired JWT cookie to remove it from the client, and a success message in the response body.
     *
     * @return A ResponseEntity containing an ApiResponse with a success message, and a Set-Cookie header with an expired JWT cookie to clear it from the client.
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logs out the user by clearing the JWT cookie and security context")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")})
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie cleanCookie = jwtUtil.getCleanJwtCookie();
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok()
                .header("Set-Cookie", cleanCookie.toString())
                .body(ApiResponse.success("Logged out", null));
    }

    /**
     * Accepts a one-time invite token and sets the initial account password.
     */
    @PostMapping("/invite/accept")
    @Operation(summary = "Accept invite", description = "Sets initial password using one-time invite token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invite accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Invalid or expired invite token")
    })
    public ResponseEntity<ApiResponse<Void>> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        userInviteService.acceptInvite(request.getToken(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Password set successfully", null));
    }
}
