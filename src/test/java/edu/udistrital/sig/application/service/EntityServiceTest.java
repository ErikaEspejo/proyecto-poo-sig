package edu.udistrital.sig.application.service;

import edu.udistrital.sig.application.support.InMemoryEntityRepository;
import edu.udistrital.sig.domain.exception.EntityNotFoundException;
import edu.udistrital.sig.domain.exception.UnauthorizedOperationException;
import edu.udistrital.sig.domain.model.Category;
import edu.udistrital.sig.domain.model.Coordinate;
import edu.udistrital.sig.domain.model.EntityNature;
import edu.udistrital.sig.domain.model.GeographicEntity;
import edu.udistrital.sig.domain.model.Point;
import edu.udistrital.sig.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityServiceTest {

    private static final Category TURISMO = new Category("TURISMO", "Turismo");

    private InMemoryEntityRepository repository;
    private EntityService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEntityRepository();
        service = new EntityService(repository);
    }

    private GeographicEntity entity(String id, String name) {
        return GeographicEntity.create(id, name, "Descripción",
                EntityNature.POINT_OF_INTEREST, TURISMO, Map.of(),
                new Point(new Coordinate(4.612, -74.071)));
    }

    @Test
    void createStoresEntity() {
        GeographicEntity created = service.create(entity("id-1", "Torre Colpatria"), Role.ADMINISTRATOR);

        Optional<GeographicEntity> found = service.findById("id-1");
        assertTrue(found.isPresent());
        assertEquals("Torre Colpatria", found.get().name());
    }

    @Test
    void updateReplacesFieldsAndKeepsId() {
        service.create(entity("id-1", "Torre Colpatria"), Role.ADMINISTRATOR);

        GeographicEntity updated = service.update("id-1", entity("id-1", "Nuevo nombre"), Role.ADMINISTRATOR);

        assertEquals("Nuevo nombre", updated.name());
        assertEquals("id-1", updated.id());
        assertEquals("Nuevo nombre", service.findById("id-1").orElseThrow().name());
    }

    @Test
    void updateOfNonexistentEntityIsRejected() {
        assertThrows(EntityNotFoundException.class,
                () -> service.update("no-existe", entity("no-existe", "x"), Role.ADMINISTRATOR));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void deleteRemovesEntity() {
        service.create(entity("id-1", "Torre Colpatria"), Role.ADMINISTRATOR);

        service.delete("id-1", Role.ADMINISTRATOR);

        assertTrue(service.findById("id-1").isEmpty());
    }

    @Test
    void deleteOfNonexistentEntityIsRejected() {
        assertThrows(EntityNotFoundException.class, () -> service.delete("no-existe", Role.ADMINISTRATOR));
    }

    @Test
    void consultationRoleCannotCreate() {
        assertThrows(UnauthorizedOperationException.class,
                () -> service.create(entity("id-1", "Torre Colpatria"), Role.CONSULTATION));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void consultationRoleCannotUpdate() {
        service.create(entity("id-1", "Torre Colpatria"), Role.ADMINISTRATOR);

        assertThrows(UnauthorizedOperationException.class,
                () -> service.update("id-1", entity("id-1", "Nuevo nombre"), Role.CONSULTATION));

        assertEquals("Torre Colpatria", service.findById("id-1").orElseThrow().name());
    }

    @Test
    void consultationRoleCannotDelete() {
        service.create(entity("id-1", "Torre Colpatria"), Role.ADMINISTRATOR);

        assertThrows(UnauthorizedOperationException.class, () -> service.delete("id-1", Role.CONSULTATION));

        assertTrue(service.findById("id-1").isPresent());
    }
}
