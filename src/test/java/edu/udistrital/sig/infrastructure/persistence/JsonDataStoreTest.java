package edu.udistrital.sig.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDataStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsJsonAtomically() {
        JsonDataStore store = new JsonDataStore(tempDir.toString());

        ObjectNode content = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode()
                .put("id", "abc")
                .put("name", "Entidad de prueba");
        store.write("entities-test.json", content);

        assertTrue(store.exists("entities-test.json"));
        JsonNode read = store.read("entities-test.json");
        assertEquals("abc", read.get("id").asText());
        assertEquals("Entidad de prueba", read.get("name").asText());
    }

    @Test
    void overwriteReplacesPreviousContent() {
        JsonDataStore store = new JsonDataStore(tempDir.toString());
        store.write("entities-test.json", JsonNodeFactoryHelper.object("name", "primero"));
        store.write("entities-test.json", JsonNodeFactoryHelper.object("name", "segundo"));

        JsonNode read = store.read("entities-test.json");
        assertEquals("segundo", read.get("name").asText());
    }
}

final class JsonNodeFactoryHelper {
    private JsonNodeFactoryHelper() {
    }

    static ObjectNode object(String key, String value) {
        return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode().put(key, value);
    }
}
