package edu.udistrital.sig.application.service;

import edu.udistrital.sig.domain.exception.EntityNotFoundException;
import edu.udistrital.sig.domain.exception.UnauthorizedOperationException;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.model.Role;
import edu.udistrital.sig.domain.repository.EntityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntityService {

    private final EntityRepository repository;

    public EntityService(EntityRepository repository) {
        this.repository = repository;
    }

    public List<GeographicEntity> findAll() {
        return repository.findAll();
    }

    public Optional<GeographicEntity> findById(String id) {
        return repository.findById(id);
    }

    public GeographicEntity create(GeographicEntity entity, Role actor) {
        requireWritePermission(actor);
        return repository.save(entity);
    }

    public GeographicEntity update(String id, GeographicEntity draft, Role actor) {
        requireWritePermission(actor);
        GeographicEntity existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        GeographicEntity updated = existing.updatedWith(
                draft.name(),
                draft.description(),
                draft.nature(),
                draft.category(),
                draft.attributes(),
                draft.geometry());
        return repository.save(updated);
    }

    public void delete(String id, Role actor) {
        requireWritePermission(actor);
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
        repository.delete(id);
    }

    private void requireWritePermission(Role actor) {
        if (!actor.canModifyEntities()) {
            throw new UnauthorizedOperationException();
        }
    }
}
