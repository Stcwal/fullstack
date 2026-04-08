package backend.fullstack.training;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.training.dto.TrainingRecordRequest;
import backend.fullstack.training.dto.TrainingRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

/**
 * REST endpoints for training records.
 */
@RestController
@RequestMapping("/api/training/records")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    @Operation(summary = "Create training record")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Training record created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<TrainingRecordResponse>> create(
            @Valid @RequestBody TrainingRecordRequest request
    ) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Training record created", trainingService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List visible training records")
    public ApiResponse<List<TrainingRecordResponse>> getVisibleRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TrainingType trainingType,
            @RequestParam(required = false) TrainingStatus status
    ) {
        return ApiResponse.success(
                "Training records fetched",
                trainingService.getVisibleRecords(userId, trainingType, status)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get training record by id")
    public ApiResponse<TrainingRecordResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Training record fetched", trainingService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update training record")
    public ApiResponse<TrainingRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TrainingRecordRequest request
    ) {
        return ApiResponse.success("Training record updated", trainingService.update(id, request));
    }
}
