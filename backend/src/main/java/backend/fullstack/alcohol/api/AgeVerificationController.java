package backend.fullstack.alcohol.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.alcohol.api.dto.AgeVerificationRequest;
import backend.fullstack.alcohol.api.dto.AgeVerificationResponse;
import backend.fullstack.alcohol.application.AgeVerificationService;
import backend.fullstack.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST endpoints for age verification logging under IK-Alkohol.
 */
@RestController
@RequestMapping("/api/alcohol/age-verifications")
@Tag(name = "Age Verifications", description = "Log and query age verification checks for alcohol service")
@SecurityRequirement(name = "Bearer Auth")
public class AgeVerificationController {

    private final AgeVerificationService verificationService;

    public AgeVerificationController(AgeVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Log a new age verification check")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Verification logged"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<AgeVerificationResponse>> create(
            @Valid @RequestBody AgeVerificationRequest request
    ) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Age verification logged", verificationService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "List age verification logs with optional filters")
    public ApiResponse<List<AgeVerificationResponse>> list(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ApiResponse.success("Verifications fetched", verificationService.list(locationId, from, to));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "Get age verification log by id")
    public ApiResponse<AgeVerificationResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Verification fetched", verificationService.getById(id));
    }
}
