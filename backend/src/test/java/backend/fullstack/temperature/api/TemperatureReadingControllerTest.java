package backend.fullstack.temperature.api;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
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
import backend.fullstack.temperature.api.dto.TemperatureReadingDeviationResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsGroupBy;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsPointResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsSeriesResponse;
import backend.fullstack.temperature.application.TemperatureReadingService;
import backend.fullstack.user.role.Role;

class TemperatureReadingControllerTest {

    private static final JwtPrincipal PRINCIPAL = new JwtPrincipal(
            7L,
            "manager@everest.no",
            Role.MANAGER,
            99L,
            List.of(1L)
    );

    private MockMvc mockMvc;
    private TemperatureReadingService readingService;

    @BeforeEach
    void setUp() {
        readingService = Mockito.mock(TemperatureReadingService.class);
        TemperatureReadingController controller = new TemperatureReadingController(readingService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new FixedJwtPrincipalResolver(PRINCIPAL))
                .build();
    }

    @Test
    void getReadingStatsReturnsExpectedJsonContract() throws Exception {
        LocalDateTime from = LocalDateTime.of(2026, 3, 20, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 3, 21, 23, 59);

        TemperatureReadingStatsResponse response = new TemperatureReadingStatsResponse(
                List.of(new TemperatureReadingStatsSeriesResponse(
                        2L,
                        "Fryser #2",
                        List.of(new TemperatureReadingStatsPointResponse(
                                LocalDateTime.of(2026, 3, 20, 8, 0),
                                -17.4,
                                true
                        ))
                )),
                List.of(new TemperatureReadingDeviationResponse(
                        42L,
                        2L,
                        "Fryser #2",
                        -12.1,
                        -16.0,
                        LocalDateTime.of(2026, 3, 20, 8, 10)
                ))
        );

        when(readingService.getReadingStats(99L, List.of(1L, 2L), from, to, TemperatureReadingStatsGroupBy.DAY))
                .thenReturn(response);

        mockMvc.perform(get("/api/readings/stats")
                        .param("unitIds", "1", "2")
                        .param("from", "2026-03-20T00:00:00")
                        .param("to", "2026-03-21T23:59:00")
                        .param("groupBy", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reading statistics retrieved"))
                .andExpect(jsonPath("$.data.series", hasSize(1)))
                .andExpect(jsonPath("$.data.series[0].unitId").value(2))
                .andExpect(jsonPath("$.data.series[0].unitName").value("Fryser #2"))
                .andExpect(jsonPath("$.data.series[0].dataPoints", hasSize(1)))
                .andExpect(jsonPath("$.data.series[0].dataPoints[0].timestamp").value("2026-03-20T08:00:00"))
                .andExpect(jsonPath("$.data.series[0].dataPoints[0].avgTemperature").value(-17.4))
                .andExpect(jsonPath("$.data.series[0].dataPoints[0].isDeviation").value(true))
                .andExpect(jsonPath("$.data.deviations", hasSize(1)))
                .andExpect(jsonPath("$.data.deviations[0].id").value(42))
                .andExpect(jsonPath("$.data.deviations[0].threshold").value(-16.0));

        verify(readingService).getReadingStats(99L, List.of(1L, 2L), from, to, TemperatureReadingStatsGroupBy.DAY);
    }

    @Test
    void getReadingStatsParsesCommaSeparatedUnitIdsAndDefaultsGroupBy() throws Exception {
        when(readingService.getReadingStats(eq(99L), any(), any(), any(), any()))
                .thenReturn(new TemperatureReadingStatsResponse(List.of(), List.of()));

        mockMvc.perform(get("/api/readings/stats")
                        .param("unitIds", "3,4")
                        .param("from", "2026-03-01T00:00:00")
                        .param("to", "2026-03-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.series", hasSize(0)))
                .andExpect(jsonPath("$.data.deviations", hasSize(0)));

        verify(readingService).getReadingStats(
                eq(99L),
                eq(List.of(3L, 4L)),
                eq(LocalDateTime.of(2026, 3, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 3, 31, 23, 59)),
                eq(TemperatureReadingStatsGroupBy.DAY)
        );
        verify(readingService, never()).getReadingStats(
                eq(99L),
                eq(List.of(3L)),
                any(),
                any(),
                any()
        );
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