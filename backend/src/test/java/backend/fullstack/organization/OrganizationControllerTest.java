package backend.fullstack.organization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.fullstack.config.GlobalExceptionHandler;
import backend.fullstack.organization.dto.OrganizationRequest;
import backend.fullstack.organization.dto.OrganizationResponse;

class OrganizationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TestOrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new TestOrganizationService();
        OrganizationController controller = new OrganizationController(organizationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createReturnsWrappedResponse() throws Exception {
        OrganizationRequest request = request();
        organizationService.createResponse = response(1L);

        mockMvc.perform(post("/api/organization")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Organization created"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Everest AS"));
    }

    @Test
    void getCurrentOrganizationReturnsWrappedResponse() throws Exception {
        organizationService.currentResponse = response(1L);

        mockMvc.perform(get("/api/organization/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Organization retrieved"))
                .andExpect(jsonPath("$.data.id").value(1));

        assert organizationService.getCurrentCalled;
    }

    @Test
    void getByIdReturnsWrappedResponse() throws Exception {
        organizationService.getByIdResponse = response(5L);

        mockMvc.perform(get("/api/organization/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Organization retrieved"))
                .andExpect(jsonPath("$.data.id").value(5));

        assert organizationService.lastId.equals(5L);
    }

    @Test
    void updateCurrentOrganizationReturnsWrappedResponse() throws Exception {
        OrganizationRequest request = request();
        organizationService.updateResponse = response(1L);

        mockMvc.perform(put("/api/organization/me")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Organization updated"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createReturnsValidationErrorForMissingFields() throws Exception {
        mockMvc.perform(post("/api/organization")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Organization name is required"))
                .andExpect(jsonPath("$.fieldErrors.organizationNumber").value("Organization number is required"));
    }

    private static OrganizationRequest request() {
        OrganizationRequest request = new OrganizationRequest();
        request.setName("Everest AS");
        request.setOrganizationNumber("123456789");
        return request;
    }

    private static OrganizationResponse response(Long id) {
        return OrganizationResponse.builder()
                .id(id)
                .name("Everest AS")
                .organizationNumber("123456789")
                .locationCount(2)
                .locationsList(java.util.List.of())
                .build();
    }

    private static final class TestOrganizationService extends OrganizationService {

        private OrganizationResponse createResponse;
        private OrganizationResponse currentResponse;
        private OrganizationResponse getByIdResponse;
        private OrganizationResponse updateResponse;
        private boolean getCurrentCalled;
        private Long lastId;

        private TestOrganizationService() {
            super(null, null, null, null);
        }

        @Override
        public OrganizationResponse create(OrganizationRequest request) {
            return createResponse;
        }

        @Override
        public OrganizationResponse getCurrentOrganization() {
            getCurrentCalled = true;
            return currentResponse;
        }

        @Override
        public OrganizationResponse getById(Long id) {
            lastId = id;
            return getByIdResponse;
        }

        @Override
        public OrganizationResponse updateCurrentOrganization(OrganizationRequest request) {
            return updateResponse;
        }
    }
}
