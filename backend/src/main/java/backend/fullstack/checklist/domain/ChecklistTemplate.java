package backend.fullstack.checklist.domain;

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
@Table(name = "checklist_templates")
public class ChecklistTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChecklistFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false, length = 20)
    private ChecklistModuleType moduleType = ChecklistModuleType.IK_MAT;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
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

    public ChecklistModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ChecklistModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public List<ChecklistTemplateItem> getItems() {
        return items;
    }

    public void setItems(List<ChecklistTemplateItem> items) {
        this.items.clear();
        if (items == null) {
            return;
        }
        for (ChecklistTemplateItem item : items) {
            addItem(item);
        }
    }

    public void addItem(ChecklistTemplateItem item) {
        item.setTemplate(this);
        this.items.add(item);
    }
}
