package backend.fullstack.training;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.fullstack.config.GlobalExceptionHandler;
import backend.fullstack.training.dto.TrainingRecordRequest;
import backend.fullstack.training.dto.TrainingRecordResponse;

class TrainingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = Mockito.mock(TrainingService.class);
        TrainingController controller = new TrainingController(trainingService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createReturnsCreatedApiResponse() throws Exception {
        TrainingRecordRequest request = request();
        TrainingRecordResponse response = response(9L, 42L);

        when(trainingService.create(org.mockito.ArgumentMatchers.any(TrainingRecordRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/training/records")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Training record created"))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.userId").value(42))
                .andExpect(jsonPath("$.data.trainingType").value("GENERAL"));
    }

    @Test
    void getVisibleRecordsReturnsWrappedList() throws Exception {
        when(trainingService.getVisibleRecords(42L, TrainingType.GENERAL, TrainingStatus.COMPLETED))
                .thenReturn(List.of(response(9L, 42L), response(10L, 42L)));

        mockMvc.perform(get("/api/training/records")
                        .param("userId", "42")
                        .param("trainingType", "GENERAL")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Training records fetched"))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(trainingService).getVisibleRecords(42L, TrainingType.GENERAL, TrainingStatus.COMPLETED);
    }

    @Test
    void getByIdReturnsWrappedResponse() throws Exception {
        when(trainingService.getById(9L)).thenReturn(response(9L, 42L));

        mockMvc.perform(get("/api/training/records/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Training record fetched"))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.userId").value(42));

        verify(trainingService).getById(9L);
    }

    @Test
    void updateReturnsWrappedResponse() throws Exception {
        TrainingRecordRequest request = request();
        TrainingRecordResponse response = response(9L, 42L);

        when(trainingService.update(eq(9L), org.mockito.ArgumentMatchers.any(TrainingRecordRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/training/records/9")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Training record updated"))
                .andExpect(jsonPath("$.data.id").value(9));
    }

    @Test
    void createReturnsValidationErrorForMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/training/records")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.userId").value("User id is required"))
                .andExpect(jsonPath("$.fieldErrors.trainingType").value("Training type is required"))
                .andExpect(jsonPath("$.fieldErrors.status").value("Training status is required"));
    }

    private static TrainingRecordRequest request() {
        TrainingRecordRequest request = new TrainingRecordRequest();
        request.setUserId(42L);
        request.setTrainingType(TrainingType.GENERAL);
        request.setStatus(TrainingStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.of(2026, 4, 4, 10, 0));
        return request;
    }

    private static TrainingRecordResponse response(Long id, Long userId) {
        return TrainingRecordResponse.builder()
                .id(id)
                .userId(userId)
                .userEmail("staff@everest.no")
                .userName("Staff User")
                .organizationId(100L)
                .trainingType(TrainingType.GENERAL)
                .status(TrainingStatus.COMPLETED)
                .completedAt(LocalDateTime.of(2026, 4, 4, 10, 0))
                .build();
    }
}
