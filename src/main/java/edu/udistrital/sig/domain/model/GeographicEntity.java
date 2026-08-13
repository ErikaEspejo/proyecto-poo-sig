package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidEntityException;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GeographicEntity {

    private final String id;
    private final String name;
    private final String description;
    private final EntityNature nature;
    private final Category category;
    private final Map<String, String> attributes;
    private final Geometry geometry;

    private GeographicEntity(String id, String name, String description, EntityNature nature,
                             Category category, Map<String, String> attributes, Geometry geometry) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.nature = nature;
        this.category = category;
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        this.geometry = geometry;
        validate();
    }

    public static GeographicEntity create(String id, String name, String description, EntityNature nature,
                                          Category category, Map<String, String> attributes, Geometry geometry) {
        return new GeographicEntity(id, name, description, nature, category, attributes, geometry);
    }

    public GeographicEntity updatedWith(String name, String description, EntityNature nature,
                                        Category category, Map<String, String> attributes, Geometry geometry) {
        return new GeographicEntity(this.id, name, description, nature, category, attributes, geometry);
    }

    private void validate() {
        if (id == null || id.isBlank()) {
            throw new InvalidEntityException("El identificador de la entidad es obligatorio.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidEntityException("El nombre de la entidad es obligatorio.");
        }
        if (nature == null) {
            throw new InvalidEntityException("La naturaleza de la entidad es obligatoria.");
        }
        if (category == null) {
            throw new InvalidEntityException("La categoría de la entidad es obligatoria.");
        }
        if (geometry == null) {
            throw new InvalidEntityException("La geometría de la entidad es obligatoria.");
        }
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public EntityNature nature() {
        return nature;
    }

    public Category category() {
        return category;
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public Geometry geometry() {
        return geometry;
    }
}
