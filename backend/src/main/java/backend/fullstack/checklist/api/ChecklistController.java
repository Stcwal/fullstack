package backend.fullstack.checklist.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.checklist.api.dto.ChecklistInstanceItemUpdateRequest;
import backend.fullstack.checklist.api.dto.ChecklistInstanceResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateUpsertRequest;
import backend.fullstack.checklist.application.ChecklistService;
import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;
import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/checklists")
@Tag(name = "Checklists", description = "Checklist templates and instances")
@SecurityRequirement(name = "Bearer Auth")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping("/templates")
    @Operation(summary = "Get checklist templates", description = "Returns all checklist templates for the authenticated organization")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Templates retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<ChecklistTemplateResponse>>> listTemplates(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        List<ChecklistTemplateResponse> templates = checklistService.listTemplates(principal.organizationId());
        return ResponseEntity.ok(ApiResponse.success("Templates retrieved", templates));
    }

    @PostMapping("/templates")
    @Operation(summary = "Create checklist template", description = "Creates a template and a mock instance for today")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Template created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<ChecklistTemplateResponse>> createTemplate(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ChecklistTemplateUpsertRequest request
    ) {
        ChecklistTemplateResponse template = checklistService.createTemplate(principal.organizationId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success("Template created", template));
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "Update checklist template")
    public ResponseEntity<ApiResponse<ChecklistTemplateResponse>> updateTemplate(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ChecklistTemplateUpsertRequest request
    ) {
        ChecklistTemplateResponse template = checklistService.updateTemplate(principal.organizationId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated", template));
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "Delete checklist template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        checklistService.deleteTemplate(principal.organizationId(), id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
    }

    @GetMapping("/instances")
    @Operation(summary = "Get checklist instances", description = "Returns checklist instances with optional filters")
    public ResponseEntity<ApiResponse<List<ChecklistInstanceResponse>>> listInstances(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) ChecklistFrequency frequency,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            @RequestParam(required = false) ChecklistInstanceStatus status
    ) {
        List<ChecklistInstanceResponse> instances = checklistService.listInstances(
                principal.organizationId(),
                frequency,
                date,
                status
        );
        return ResponseEntity.ok(ApiResponse.success("Instances retrieved", instances));
    }

    @GetMapping("/instances/{id}")
    @Operation(summary = "Get checklist instance by id")
    public ResponseEntity<ApiResponse<ChecklistInstanceResponse>> getInstance(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        ChecklistInstanceResponse instance = checklistService.getInstance(principal.organizationId(), id);
        return ResponseEntity.ok(ApiResponse.success("Instance retrieved", instance));
    }

    @PatchMapping("/instances/{id}/items/{itemId}")
    @Operation(summary = "Update checklist instance item", description = "Checks or unchecks one instance item")
    public ResponseEntity<ApiResponse<ChecklistInstanceResponse>> updateInstanceItem(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistInstanceItemUpdateRequest request
    ) {
        ChecklistInstanceResponse response = checklistService.updateInstanceItem(
                principal.organizationId(),
                id,
                itemId,
                principal.userId(),
                request
        );
        return ResponseEntity.ok(ApiResponse.success("Instance item updated", response));
    }
}
