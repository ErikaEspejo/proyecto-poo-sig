package edu.udistrital.sig.domain.repository;

import edu.udistrital.sig.domain.model.GeographicEntity;

import java.util.List;
import java.util.Optional;

public interface EntityRepository {

    List<GeographicEntity> findAll();

    Optional<GeographicEntity> findById(String id);

    GeographicEntity save(GeographicEntity entity);

    void delete(String id);
}
