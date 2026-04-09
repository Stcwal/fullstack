package backend.fullstack.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.domain.ChecklistInstanceItem;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.domain.DeviationModuleType;
import backend.fullstack.deviations.domain.DeviationSeverity;
import backend.fullstack.deviations.domain.DeviationStatus;
import backend.fullstack.export.application.PdfExportGenerator;
import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.user.User;

class PdfExportGeneratorTest {

    private PdfExportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PdfExportGenerator();
    }

    @Test
    void generateTemperatureLogsPdf_returnsValidPdf() {
        TemperatureUnit unit = new TemperatureUnit();
        unit.setName("Fryser 1");

        User user = new User();
        user.setFirstName("Ola");
        user.setLastName("Nordmann");

        TemperatureReading reading = TemperatureReading.builder()
                .id(1L)
                .temperature(-18.0)
                .unit(unit)
                .recordedBy(user)
                .recordedAt(LocalDateTime.of(2026, 4, 1, 8, 0))
                .build();

        byte[] result = generator.generateTemperatureLogsPdf(
                List.of(reading),
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 7)
        );

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void generateTemperatureLogsPdf_emptyList_producesValidPdf() {
        byte[] result = generator.generateTemperatureLogsPdf(List.of(), null, null);

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void generateDeviationsPdf_returnsValidPdf() {
        Deviation deviation = Deviation.builder()
                .id(5L)
                .title("Power outage")
                .description("Electricity was off for 2 hours")
                .status(DeviationStatus.OPEN)
                .severity(DeviationSeverity.CRITICAL)
                .moduleType(DeviationModuleType.IK_MAT)
                .createdAt(LocalDateTime.of(2026, 3, 20, 16, 45))
                .build();

        byte[] result = generator.generateDeviationsPdf(List.of(deviation));

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void generateDeviationsPdf_emptyList_producesValidPdf() {
        byte[] result = generator.generateDeviationsPdf(List.of());

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void generateChecklistsPdf_returnsValidPdf() {
        ChecklistInstance checklist = new ChecklistInstance();
        checklist.setId(1L);
        checklist.setTitle("Daglig sjekkliste");
        checklist.setFrequency(ChecklistFrequency.DAILY);
        checklist.setDate(LocalDate.of(2026, 4, 1));
        checklist.setStatus(ChecklistInstanceStatus.COMPLETED);

        ChecklistInstanceItem item = new ChecklistInstanceItem();
        item.setId(10L);
        item.setText("Sjekk temperatur");
        item.setCompleted(true);
        checklist.addItem(item);

        byte[] result = generator.generateChecklistsPdf(List.of(checklist));

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void generateChecklistsPdf_emptyList_producesValidPdf() {
        byte[] result = generator.generateChecklistsPdf(List.of());

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 5)).isEqualTo("%PDF-");
    }
}
