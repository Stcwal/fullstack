package backend.fullstack.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
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
import backend.fullstack.export.application.JsonExportGenerator;
import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.units.domain.TemperatureUnit;

class JsonExportGeneratorTest {

    private JsonExportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new JsonExportGenerator();
    }

    @Test
    void generateTemperatureLogsJson_returnsValidJsonWithData() {
        TemperatureUnit unit = new TemperatureUnit();
        unit.setName("Kjøleskap 1");

        TemperatureReading reading = TemperatureReading.builder()
                .id(1L)
                .temperature(4.5)
                .unit(unit)
                .recordedAt(LocalDateTime.of(2026, 4, 1, 10, 30))
                .build();

        byte[] result = generator.generateTemperatureLogsJson(List.of(reading));
        String json = new String(result, StandardCharsets.UTF_8);

        assertThat(json).contains("\"exportType\" : \"TEMPERATURE_LOGS\"");
        assertThat(json).contains("\"totalRecords\" : 1");
        assertThat(json).contains("\"temperature\" : 4.5");
        assertThat(json).contains("\"unitName\" : \"Kjøleskap 1\"");
    }

    @Test
    void generateTemperatureLogsJson_emptyList_returnsZeroRecords() {
        byte[] result = generator.generateTemperatureLogsJson(List.of());
        String json = new String(result, StandardCharsets.UTF_8);

        assertThat(json).contains("\"totalRecords\" : 0");
        assertThat(json).contains("\"data\" : [ ]");
    }

    @Test
    void generateDeviationsJson_returnsValidJson() {
        Deviation deviation = Deviation.builder()
                .id(10L)
                .title("Temperature too high")
                .description("Fridge was above 8°C")
                .status(DeviationStatus.OPEN)
                .severity(DeviationSeverity.HIGH)
                .moduleType(DeviationModuleType.IK_MAT)
                .createdAt(LocalDateTime.of(2026, 3, 15, 14, 0))
                .build();

        byte[] result = generator.generateDeviationsJson(List.of(deviation));
        String json = new String(result, StandardCharsets.UTF_8);

        assertThat(json).contains("\"exportType\" : \"DEVIATIONS\"");
        assertThat(json).contains("\"title\" : \"Temperature too high\"");
        assertThat(json).contains("\"status\" : \"OPEN\"");
        assertThat(json).contains("\"severity\" : \"HIGH\"");
    }

    @Test
    void generateChecklistsJson_includesItems() {
        ChecklistInstance checklist = new ChecklistInstance();
        checklist.setId(5L);
        checklist.setTitle("Morning checklist");
        checklist.setFrequency(ChecklistFrequency.DAILY);
        checklist.setDate(LocalDate.of(2026, 4, 1));
        checklist.setStatus(ChecklistInstanceStatus.COMPLETED);

        ChecklistInstanceItem item = new ChecklistInstanceItem();
        item.setId(100L);
        item.setText("Check fridge temperature");
        item.setCompleted(true);
        checklist.addItem(item);

        byte[] result = generator.generateChecklistsJson(List.of(checklist));
        String json = new String(result, StandardCharsets.UTF_8);

        assertThat(json).contains("\"exportType\" : \"CHECKLISTS\"");
        assertThat(json).contains("\"title\" : \"Morning checklist\"");
        assertThat(json).contains("\"text\" : \"Check fridge temperature\"");
        assertThat(json).contains("\"completed\" : true");
    }
}
