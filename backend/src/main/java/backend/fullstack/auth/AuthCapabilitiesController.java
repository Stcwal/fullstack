package backend.fullstack.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.permission.dto.CapabilitiesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/auth")
public class AuthCapabilitiesController {

    private final AuthorizationService authorizationService;

    public AuthCapabilitiesController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/capabilities")
    @Operation(summary = "Get current user capabilities", description = "Returns effective permissions, role and location scope for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Capabilities returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse<CapabilitiesResponse> getCapabilities() {
        return ApiResponse.success("Capabilities fetched", authorizationService.getCurrentCapabilities());
    }
}
