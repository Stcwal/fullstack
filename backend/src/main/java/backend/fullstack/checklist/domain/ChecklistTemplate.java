package backend.fullstack.checklist.domain;

import java.util.ArrayList;
import java.util.List;

public class ChecklistTemplate {

    private Long id;
    private Long organizationId;
    private String title;
    private ChecklistFrequency frequency;
    private List<ChecklistTemplateItem> items = new ArrayList<>();

    public ChecklistTemplate() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<ChecklistTemplateItem> getItems() {
        return items;
    }

    public void setItems(List<ChecklistTemplateItem> items) {
        this.items = items;
    }
}
