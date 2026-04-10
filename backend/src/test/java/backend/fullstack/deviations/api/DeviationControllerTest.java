package backend.fullstack.deviations.api;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.deviations.api.dto.DeviationCommentResponse;
import backend.fullstack.deviations.api.dto.DeviationDetailsResponse;
import backend.fullstack.deviations.application.DeviationService;
import backend.fullstack.deviations.domain.DeviationModuleType;
import backend.fullstack.deviations.domain.DeviationSeverity;
import backend.fullstack.deviations.domain.DeviationStatus;
import backend.fullstack.user.role.Role;

class DeviationControllerTest {

    private static final JwtPrincipal PRINCIPAL = new JwtPrincipal(
            7L,
            "manager@everest.no",
            Role.MANAGER,
            99L,
            List.of(1L)
    );

    private MockMvc mockMvc;
    private DeviationService deviationService;

    @BeforeEach
    void setUp() {
        deviationService = org.mockito.Mockito.mock(DeviationService.class);
        DeviationController controller = new DeviationController(deviationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new FixedJwtPrincipalResolver(PRINCIPAL))
                .build();
    }

    @Test
    void getDeviationByIdReturnsDetailsWithCommentLog() throws Exception {
        LocalDateTime reportedAt = LocalDateTime.of(2026, 4, 8, 10, 15);
        LocalDateTime commentAt = LocalDateTime.of(2026, 4, 8, 11, 0);

        DeviationDetailsResponse details = new DeviationDetailsResponse(
                42L,
                "Fryser #2 over grense",
                "Malt -12.1C",
                DeviationStatus.IN_PROGRESS,
                DeviationSeverity.CRITICAL,
                DeviationModuleType.IK_MAT,
                "Kari Larsen",
                reportedAt,
                null,
                null,
                null,
                "Location Name",
                12L,
                List.of(new DeviationCommentResponse(
                        501L,
                        "Har bestilt service",
                        3L,
                        "Ola Nordmann",
                        commentAt
                ))
        );

        when(deviationService.getDeviationById(99L, 42L)).thenReturn(details);

        mockMvc.perform(get("/api/deviations/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deviation retrieved"))
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.relatedReadingId").value(12))
                .andExpect(jsonPath("$.data.comments", hasSize(1)))
                .andExpect(jsonPath("$.data.comments[0].id").value(501))
                .andExpect(jsonPath("$.data.comments[0].comment").value("Har bestilt service"));

        verify(deviationService).getDeviationById(99L, 42L);
    }

    private static final class FixedJwtPrincipalResolver implements HandlerMethodArgumentResolver {
        private final JwtPrincipal principal;

        private FixedJwtPrincipalResolver(JwtPrincipal principal) {
            this.principal = principal;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return JwtPrincipal.class.equals(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return principal;
        }
    }
}
