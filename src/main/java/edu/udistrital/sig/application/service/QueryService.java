package edu.udistrital.sig.application.service;

import edu.udistrital.sig.domain.exception.InvalidEntityException;
import edu.udistrital.sig.domain.model.Coordinate;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.repository.EntityRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class QueryService {

    public record QueryResult(List<GeographicEntity> entities, String matchedBy) {
    }

    private final EntityRepository repository;

    public QueryService(EntityRepository repository) {
        this.repository = repository;
    }

    public QueryResult query(String category, String attribute, String text,
                             Double latitude, Double longitude, Double radiusKm) {
        boolean proximityRequested = latitude != null || longitude != null || radiusKm != null;
        if (category == null && attribute == null && text == null && !proximityRequested) {
            throw new InvalidEntityException("Debe proporcionar al menos un criterio de búsqueda.");
        }
        if (proximityRequested && (latitude == null || longitude == null || radiusKm == null)) {
            throw new InvalidEntityException("Debe proporcionar lat, lon y radiusKm juntos.");
        }

        List<GeographicEntity> entities = repository.findAll();
        List<String> appliedCriteria = new ArrayList<>();

        if (category != null) {
            String criterion = category.toLowerCase(Locale.ROOT);
            entities = entities.stream()
                    .filter(e -> e.category().id().toLowerCase(Locale.ROOT).equals(criterion)
                            || e.category().name().toLowerCase(Locale.ROOT).equals(criterion))
                    .toList();
            appliedCriteria.add("CATEGORY");
        }
        if (attribute != null) {
            String criterion = attribute.toLowerCase(Locale.ROOT);
            entities = entities.stream()
                    .filter(e -> e.attributes().values().stream()
                            .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(criterion)))
                    .toList();
            appliedCriteria.add("ATTRIBUTE");
        }
        if (text != null) {
            String criterion = text.toLowerCase(Locale.ROOT);
            entities = entities.stream()
                    .filter(e -> containsIgnoreCase(e.name(), criterion)
                            || containsIgnoreCase(e.description(), criterion))
                    .toList();
            appliedCriteria.add("TEXT");
        }
        if (proximityRequested) {
            if (radiusKm <= 0) {
                throw new InvalidEntityException("El radio debe ser mayor que cero.");
            }
            Coordinate reference = new Coordinate(latitude, longitude);
            entities = entities.stream()
                    .filter(e -> e.geometry().minDistanceToKm(reference) <= radiusKm)
                    .toList();
            appliedCriteria.add("PROXIMITY");
        }

        return new QueryResult(entities, String.join(",", appliedCriteria));
    }

    private boolean containsIgnoreCase(String value, String criterion) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(criterion);
    }
}
