package edu.udistrital.sig.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class JsonDataStore {

    private final Path dataDirectory;
    private final ObjectMapper mapper;

    public JsonDataStore(@Value("${sig.data.directory:./data}") String dataDirectory) {
        this.dataDirectory = Path.of(dataDirectory);
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path path(String fileName) {
        return dataDirectory.resolve(fileName);
    }

    public boolean exists(String fileName) {
        return Files.exists(path(fileName));
    }

    public JsonNode read(String fileName) {
        try {
            return mapper.readTree(Files.readAllBytes(path(fileName)));
        } catch (IOException e) {
            throw new PersistenceException("No se pudo leer el archivo de datos " + fileName, e);
        }
    }

    public ArrayNode readArray(String fileName) {
        if (!exists(fileName)) {
            return JsonNodeFactory.instance.arrayNode();
        }
        JsonNode node = read(fileName);
        if (node instanceof ArrayNode array) {
            return array;
        }
        throw new PersistenceException("El archivo de datos " + fileName + " no es una lista.", null);
    }

    public void write(String fileName, JsonNode content) {
        Path target = dataDirectory.resolve(fileName);
        try {
            Files.createDirectories(dataDirectory);
            Path temp = Files.createTempFile(dataDirectory, fileName + ".", ".tmp");
            try (OutputStream out = Files.newOutputStream(temp)) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(out, content);
            }
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PersistenceException("No se pudo escribir el archivo de datos " + fileName, e);
        }
    }
}
