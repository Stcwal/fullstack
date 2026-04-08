package backend.fullstack.location;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.fullstack.config.GlobalExceptionHandler;
import backend.fullstack.location.dto.LocationRequest;
import backend.fullstack.location.dto.LocationResponse;

class LocationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TestLocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new TestLocationService();
        LocationController controller = new LocationController(locationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createReturnsWrappedResponse() throws Exception {
        LocationRequest request = request();
        locationService.createResponse = response(1L);

        mockMvc.perform(post("/api/locations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Location created"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Trondheim Office"));
    }

    @Test
    void getAllAccessibleReturnsWrappedList() throws Exception {
        locationService.allAccessibleResponse = List.of(response(1L), response(2L));

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Accessible locations retrieved"))
                .andExpect(jsonPath("$.data", hasSize(2)));

        assert locationService.getAllAccessibleCalled;
    }

    @Test
    void getByIdReturnsWrappedResponse() throws Exception {
        locationService.getByIdResponse = response(1L);

        mockMvc.perform(get("/api/locations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Location retrieved"))
                .andExpect(jsonPath("$.data.id").value(1));

        assert locationService.lastId.equals(1L);
    }

    @Test
    void updateReturnsWrappedResponse() throws Exception {
        LocationRequest request = request();
        locationService.updateResponse = response(1L);

        mockMvc.perform(put("/api/locations/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Location updated"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void deleteDelegatesToService() throws Exception {
        mockMvc.perform(delete("/api/locations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Location deleted"));

        assert locationService.deletedId.equals(1L);
    }

    @Test
    void createReturnsValidationErrorForMissingFields() throws Exception {
        mockMvc.perform(post("/api/locations")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Location name is required"))
                .andExpect(jsonPath("$.fieldErrors.address").value("Location address is required"));
    }

    private static LocationRequest request() {
        LocationRequest request = new LocationRequest();
        request.setName("Trondheim Office");
        request.setAddress("Kongens gate 1, 7011 Trondheim");
        return request;
    }

    private static LocationResponse response(Long id) {
        return LocationResponse.builder()
                .id(id)
                .name("Trondheim Office")
                .address("Kongens gate 1, 7011 Trondheim")
                .build();
    }

    private static final class TestLocationService extends LocationService {

        private LocationResponse createResponse;
        private List<LocationResponse> allAccessibleResponse = List.of();
        private LocationResponse getByIdResponse;
        private LocationResponse updateResponse;
        private boolean getAllAccessibleCalled;
        private Long lastId;
        private Long deletedId;

        private TestLocationService() {
            super(null, null, null, null);
        }

        @Override
        public LocationResponse create(LocationRequest request) {
            return createResponse;
        }

        @Override
        public List<LocationResponse> getAllAccessible() {
            getAllAccessibleCalled = true;
            return allAccessibleResponse;
        }

        @Override
        public LocationResponse getById(Long id) {
            lastId = id;
            return getByIdResponse;
        }

        @Override
        public LocationResponse update(Long id, LocationRequest request) {
            lastId = id;
            return updateResponse;
        }

        @Override
        public void delete(Long id) {
            deletedId = id;
        }
    }
}
