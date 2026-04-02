package backend.fullstack.checklist.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import backend.fullstack.checklist.domain.ChecklistInstance;

@Repository
public class ChecklistInstanceRepository {

    private final Map<Long, ChecklistInstance> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<ChecklistInstance> findAllByOrganizationId(Long organizationId) {
        return storage.values().stream()
                .filter(instance -> organizationId.equals(instance.getOrganizationId()))
                .map(this::copyInstance)
                .toList();
    }

    public Optional<ChecklistInstance> findByIdAndOrganizationId(Long id, Long organizationId) {
        ChecklistInstance instance = storage.get(id);
        if (instance == null || !organizationId.equals(instance.getOrganizationId())) {
            return Optional.empty();
        }
        return Optional.of(copyInstance(instance));
    }

    public ChecklistInstance save(ChecklistInstance instance) {
        ChecklistInstance toStore = copyInstance(instance);
        if (toStore.getId() == null) {
            toStore.setId(idGenerator.incrementAndGet());
        }
        storage.put(toStore.getId(), toStore);
        return copyInstance(toStore);
    }

    private ChecklistInstance copyInstance(ChecklistInstance source) {
        ChecklistInstance copy = new ChecklistInstance();
        copy.setId(source.getId());
        copy.setTemplateId(source.getTemplateId());
        copy.setOrganizationId(source.getOrganizationId());
        copy.setTitle(source.getTitle());
        copy.setFrequency(source.getFrequency());
        copy.setDate(source.getDate());
        copy.setStatus(source.getStatus());
        copy.setItems(new ArrayList<>(source.getItems()));
        return copy;
    }
}
