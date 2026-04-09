package backend.fullstack.export.application;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.domain.ChecklistInstanceItem;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.temperature.domain.TemperatureReading;

/**
 * Generates JSON export files from domain data.
 */
@Component
public class JsonExportGenerator {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ObjectMapper objectMapper;

    public JsonExportGenerator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public byte[] generateTemperatureLogsJson(List<TemperatureReading> readings) {
        List<Map<String, Object>> items = readings.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("temperature", r.getTemperature());
            map.put("unitName", r.getUnit() != null ? r.getUnit().getName() : null);
            map.put("recordedAt", formatDateTime(r.getRecordedAt()));
            map.put("recordedBy", r.getRecordedBy() != null
                    ? r.getRecordedBy().getFirstName() + " " + r.getRecordedBy().getLastName()
                    : null);
            map.put("note", r.getNote());
            map.put("isDeviation", r.isDeviation());
            return map;
        }).toList();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("exportType", "TEMPERATURE_LOGS");
        root.put("exportedAt", formatDateTime(LocalDateTime.now()));
        root.put("totalRecords", items.size());
        root.put("data", items);

        return toBytes(root);
    }

    public byte[] generateDeviationsJson(List<Deviation> deviations) {
        List<Map<String, Object>> items = deviations.stream().map(d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId());
            map.put("title", d.getTitle());
            map.put("description", d.getDescription());
            map.put("status", d.getStatus() != null ? d.getStatus().name() : null);
            map.put("severity", d.getSeverity() != null ? d.getSeverity().name() : null);
            map.put("moduleType", d.getModuleType() != null ? d.getModuleType().name() : null);
            map.put("reportedBy", d.getReportedByName());
            map.put("createdAt", formatDateTime(d.getCreatedAt()));
            map.put("resolvedBy", d.getResolvedByName());
            map.put("resolvedAt", formatDateTime(d.getResolvedAt()));
            map.put("resolution", d.getResolution());
            return map;
        }).toList();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("exportType", "DEVIATIONS");
        root.put("exportedAt", formatDateTime(LocalDateTime.now()));
        root.put("totalRecords", items.size());
        root.put("data", items);

        return toBytes(root);
    }

    public byte[] generateChecklistsJson(List<ChecklistInstance> checklists) {
        List<Map<String, Object>> items = checklists.stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("title", c.getTitle());
            map.put("frequency", c.getFrequency() != null ? c.getFrequency().name() : null);
            map.put("date", formatDate(c.getDate()));
            map.put("status", c.getStatus() != null ? c.getStatus().name() : null);
            map.put("items", c.getItems().stream().map(this::mapChecklistItem).toList());
            return map;
        }).toList();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("exportType", "CHECKLISTS");
        root.put("exportedAt", formatDateTime(LocalDateTime.now()));
        root.put("totalRecords", items.size());
        root.put("data", items);

        return toBytes(root);
    }

    private Map<String, Object> mapChecklistItem(ChecklistInstanceItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", item.getId());
        map.put("text", item.getText());
        map.put("completed", item.isCompleted());
        map.put("completedByUserId", item.getCompletedByUserId());
        map.put("completedAt", item.getCompletedAt() != null ? item.getCompletedAt().toString() : null);
        return map;
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : null;
    }

    private String formatDate(LocalDate d) {
        return d != null ? d.format(D_FMT) : null;
    }

    private byte[] toBytes(Object value) {
        try {
            return objectMapper.writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to generate JSON export", e);
        }
    }
}
