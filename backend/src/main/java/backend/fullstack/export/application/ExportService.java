package backend.fullstack.export.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.infrastructure.ChecklistInstanceRepository;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.infrastructure.DeviationRepository;
import backend.fullstack.export.domain.ExportFormat;
import backend.fullstack.export.domain.ExportModule;
import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.temperature.infrastructure.TemperatureReadingRepository;

/**
 * Service responsible for orchestrating data exports.
 * Fetches data from the relevant module and delegates to format-specific generators.
 */
@Service
@Transactional(readOnly = true)
public class ExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TemperatureReadingRepository temperatureReadingRepository;
    private final DeviationRepository deviationRepository;
    private final ChecklistInstanceRepository checklistInstanceRepository;
    private final PdfExportGenerator pdfGenerator;
    private final JsonExportGenerator jsonGenerator;

    public ExportService(
            TemperatureReadingRepository temperatureReadingRepository,
            DeviationRepository deviationRepository,
            ChecklistInstanceRepository checklistInstanceRepository,
            PdfExportGenerator pdfGenerator,
            JsonExportGenerator jsonGenerator
    ) {
        this.temperatureReadingRepository = temperatureReadingRepository;
        this.deviationRepository = deviationRepository;
        this.checklistInstanceRepository = checklistInstanceRepository;
        this.pdfGenerator = pdfGenerator;
        this.jsonGenerator = jsonGenerator;
    }

    /**
     * Exports data for the given module and format.
     *
     * @param organizationId the organization scope
     * @param module         the data module to export
     * @param format         desired output format
     * @param from           optional start date (inclusive)
     * @param to             optional end date (inclusive)
     * @return byte array containing the exported file
     */
    public byte[] export(Long organizationId, ExportModule module, ExportFormat format,
                         LocalDate from, LocalDate to) {
        return switch (module) {
            case TEMPERATURE_LOGS -> exportTemperatureLogs(organizationId, format, from, to);
            case DEVIATIONS -> exportDeviations(organizationId, format);
            case CHECKLISTS -> exportChecklists(organizationId, format);
        };
    }

    /**
     * Builds a descriptive filename for the export.
     */
    public String buildFilename(ExportModule module, ExportFormat format) {
        String moduleName = module.name().toLowerCase().replace('_', '-');
        String date = LocalDate.now().format(DATE_FMT);
        String extension = format == ExportFormat.PDF ? "pdf" : "json";
        return moduleName + "-export-" + date + "." + extension;
    }

    private byte[] exportTemperatureLogs(Long organizationId, ExportFormat format,
                                         LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(23, 59, 59) : null;

        List<TemperatureReading> readings = temperatureReadingRepository
                .findForStatsByOrganizationAndRange(organizationId, fromDt, toDt);

        return format == ExportFormat.PDF
                ? pdfGenerator.generateTemperatureLogsPdf(readings, from, to)
                : jsonGenerator.generateTemperatureLogsJson(readings);
    }

    private byte[] exportDeviations(Long organizationId, ExportFormat format) {
        List<Deviation> deviations = deviationRepository
                .findByOrganization_IdOrderByCreatedAtDesc(organizationId);

        return format == ExportFormat.PDF
                ? pdfGenerator.generateDeviationsPdf(deviations)
                : jsonGenerator.generateDeviationsJson(deviations);
    }

    private byte[] exportChecklists(Long organizationId, ExportFormat format) {
        List<ChecklistInstance> checklists = checklistInstanceRepository
                .findAllByOrganizationId(organizationId);

        return format == ExportFormat.PDF
                ? pdfGenerator.generateChecklistsPdf(checklists)
                : jsonGenerator.generateChecklistsJson(checklists);
    }
}
