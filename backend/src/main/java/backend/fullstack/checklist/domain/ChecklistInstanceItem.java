package backend.fullstack.checklist.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "checklist_instance_items")
public class ChecklistInstanceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private ChecklistInstance instance;

    @Column(name = "item_text", nullable = false, length = 250)
    private String text;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_by_user_id")
    private Long completedByUserId;

    @Column(name = "completed_at")
    private Instant completedAt;

    public ChecklistInstanceItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChecklistInstance getInstance() {
        return instance;
    }

    public void setInstance(ChecklistInstance instance) {
        this.instance = instance;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Long getCompletedByUserId() {
        return completedByUserId;
    }

    public void setCompletedByUserId(Long completedByUserId) {
        this.completedByUserId = completedByUserId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
