package edu.udistrital.sig.infrastructure.persistence;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Component
public class SeedDataInitializer implements ApplicationRunner {

    private static final List<String> SEED_FILES = List.of("users.json", "categories.json", "entities.json");

    private final JsonDataStore store;
    private final ResourceLoader resourceLoader;

    public SeedDataInitializer(JsonDataStore store, ResourceLoader resourceLoader) {
        this.store = store;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String fileName : SEED_FILES) {
            if (!store.exists(fileName)) {
                copySeedFile(fileName);
            }
        }
    }

    private void copySeedFile(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/" + fileName);
            Path target = store.path(fileName);
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new PersistenceException("No se pudo inicializar el archivo de datos " + fileName, e);
        }
    }
}
