package edu.udistrital.sig.application.support;

import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.repository.EntityRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryEntityRepository implements EntityRepository {

    private final Map<String, GeographicEntity> entities = new LinkedHashMap<>();

    @Override
    public List<GeographicEntity> findAll() {
        return List.copyOf(entities.values());
    }

    @Override
    public Optional<GeographicEntity> findById(String id) {
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public GeographicEntity save(GeographicEntity entity) {
        entities.put(entity.id(), entity);
        return entity;
    }

    @Override
    public void delete(String id) {
        entities.remove(id);
    }
}
