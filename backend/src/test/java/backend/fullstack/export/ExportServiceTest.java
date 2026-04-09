package backend.fullstack.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.infrastructure.ChecklistInstanceRepository;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.infrastructure.DeviationRepository;
import backend.fullstack.export.application.ExportService;
import backend.fullstack.export.application.JsonExportGenerator;
import backend.fullstack.export.application.PdfExportGenerator;
import backend.fullstack.export.domain.ExportFormat;
import backend.fullstack.export.domain.ExportModule;
import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.temperature.infrastructure.TemperatureReadingRepository;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private TemperatureReadingRepository temperatureReadingRepository;
    @Mock private DeviationRepository deviationRepository;
    @Mock private ChecklistInstanceRepository checklistInstanceRepository;
    @Mock private PdfExportGenerator pdfGenerator;
    @Mock private JsonExportGenerator jsonGenerator;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportService(
                temperatureReadingRepository,
                deviationRepository,
                checklistInstanceRepository,
                pdfGenerator,
                jsonGenerator
        );
    }

    @Test
    void export_temperatureLogs_json_delegatesToJsonGenerator() {
        Long orgId = 1L;
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        List<TemperatureReading> readings = List.of(new TemperatureReading());
        byte[] expected = "{\"test\": true}".getBytes();

        when(temperatureReadingRepository.findForStatsByOrganizationAndRange(
                eq(orgId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(readings);
        when(jsonGenerator.generateTemperatureLogsJson(readings)).thenReturn(expected);

        byte[] result = exportService.export(orgId, ExportModule.TEMPERATURE_LOGS, ExportFormat.JSON, from, to);

        assertThat(result).isEqualTo(expected);
        verify(jsonGenerator).generateTemperatureLogsJson(readings);
    }

    @Test
    void export_temperatureLogs_pdf_delegatesToPdfGenerator() {
        Long orgId = 1L;
        List<TemperatureReading> readings = List.of();
        byte[] expected = new byte[]{0x25, 0x50, 0x44, 0x46};

        when(temperatureReadingRepository.findForStatsByOrganizationAndRange(eq(orgId), any(), any()))
                .thenReturn(readings);
        when(pdfGenerator.generateTemperatureLogsPdf(eq(readings), any(), any())).thenReturn(expected);

        byte[] result = exportService.export(orgId, ExportModule.TEMPERATURE_LOGS, ExportFormat.PDF, null, null);

        assertThat(result).isEqualTo(expected);
        verify(pdfGenerator).generateTemperatureLogsPdf(eq(readings), any(), any());
    }

    @Test
    void export_deviations_json_delegatesToJsonGenerator() {
        Long orgId = 2L;
        List<Deviation> deviations = List.of();
        byte[] expected = "[]".getBytes();

        when(deviationRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId)).thenReturn(deviations);
        when(jsonGenerator.generateDeviationsJson(deviations)).thenReturn(expected);

        byte[] result = exportService.export(orgId, ExportModule.DEVIATIONS, ExportFormat.JSON, null, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void export_deviations_pdf_delegatesToPdfGenerator() {
        Long orgId = 2L;
        List<Deviation> deviations = List.of();
        byte[] expected = new byte[]{1, 2, 3};

        when(deviationRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId)).thenReturn(deviations);
        when(pdfGenerator.generateDeviationsPdf(deviations)).thenReturn(expected);

        byte[] result = exportService.export(orgId, ExportModule.DEVIATIONS, ExportFormat.PDF, null, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void export_checklists_json_delegatesToJsonGenerator() {
        Long orgId = 3L;
        List<ChecklistInstance> checklists = List.of();
        byte[] expected = "{}".getBytes();

        when(checklistInstanceRepository.findAllByOrganizationId(orgId)).thenReturn(checklists);
        when(jsonGenerator.generateChecklistsJson(checklists)).thenReturn(expected);

        byte[] result = exportService.export(orgId, ExportModule.CHECKLISTS, ExportFormat.JSON, null, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void export_checklists_pdf_delegatesToPdfGenerator() {
        Long orgId = 3L;
        List<ChecklistInstance> checklists = List.of();
        byte[] expected = new byte[]{4, 5, 6};

        when(checklistInstanceRepository.findAllByOrganizationId(orgId)).thenReturn(checklists);
        when(pdfGenerator.generateChecklistsPdf(checklists)).thenReturn(expected);

        byte[] result = exportService.export(orgId, ExportModule.CHECKLISTS, ExportFormat.PDF, null, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildFilename_pdf_format() {
        String filename = exportService.buildFilename(ExportModule.TEMPERATURE_LOGS, ExportFormat.PDF);

        assertThat(filename).startsWith("temperature-logs-export-");
        assertThat(filename).endsWith(".pdf");
    }

    @Test
    void buildFilename_json_format() {
        String filename = exportService.buildFilename(ExportModule.DEVIATIONS, ExportFormat.JSON);

        assertThat(filename).startsWith("deviations-export-");
        assertThat(filename).endsWith(".json");
    }
}
