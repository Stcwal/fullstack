package backend.fullstack.checklist.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "checklist_instances")
public class ChecklistInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChecklistFrequency frequency;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChecklistInstanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false, length = 20)
    private ChecklistModuleType moduleType = ChecklistModuleType.IK_MAT;

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
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

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
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

    public ChecklistModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ChecklistModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public List<ChecklistInstanceItem> getItems() {
        return items;
    }

    public void setItems(List<ChecklistInstanceItem> items) {
        this.items.clear();
        if (items == null) {
            return;
        }
        for (ChecklistInstanceItem item : items) {
            addItem(item);
        }
    }

    public void addItem(ChecklistInstanceItem item) {
        item.setInstance(this);
        this.items.add(item);
    }
}
