package backend.fullstack.checklist.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import backend.fullstack.checklist.domain.ChecklistTemplate;

@Repository
public class ChecklistTemplateRepository {

    private final Map<Long, ChecklistTemplate> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<ChecklistTemplate> findAllByOrganizationId(Long organizationId) {
        return storage.values().stream()
                .filter(template -> organizationId.equals(template.getOrganizationId()))
                .map(this::copyTemplate)
                .toList();
    }

    public Optional<ChecklistTemplate> findByIdAndOrganizationId(Long id, Long organizationId) {
        ChecklistTemplate template = storage.get(id);
        if (template == null || !organizationId.equals(template.getOrganizationId())) {
            return Optional.empty();
        }
        return Optional.of(copyTemplate(template));
    }

    public ChecklistTemplate save(ChecklistTemplate template) {
        ChecklistTemplate toStore = copyTemplate(template);
        if (toStore.getId() == null) {
            toStore.setId(idGenerator.incrementAndGet());
        }
        storage.put(toStore.getId(), toStore);
        return copyTemplate(toStore);
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    private ChecklistTemplate copyTemplate(ChecklistTemplate source) {
        ChecklistTemplate copy = new ChecklistTemplate();
        copy.setId(source.getId());
        copy.setOrganizationId(source.getOrganizationId());
        copy.setTitle(source.getTitle());
        copy.setFrequency(source.getFrequency());
        copy.setItems(new ArrayList<>(source.getItems()));
        return copy;
    }
}
