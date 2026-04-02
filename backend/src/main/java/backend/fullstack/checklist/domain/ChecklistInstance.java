package backend.fullstack.checklist.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChecklistInstance {

    private Long id;
    private Long templateId;
    private Long organizationId;
    private String title;
    private ChecklistFrequency frequency;
    private LocalDate date;
    private ChecklistInstanceStatus status;
    private List<ChecklistInstanceItem> items = new ArrayList<>();

    public ChecklistInstance() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ChecklistFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(ChecklistFrequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ChecklistInstanceStatus getStatus() {
        return status;
    }

    public void setStatus(ChecklistInstanceStatus status) {
        this.status = status;
    }

    public List<ChecklistInstanceItem> getItems() {
        return items;
    }

    public void setItems(List<ChecklistInstanceItem> items) {
        this.items = items;
    }
}
