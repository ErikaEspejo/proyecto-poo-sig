package edu.udistrital.sig.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDataStoreIntegrityTest {

    @TempDir
    Path tempDir;

    @Test
    void failedWriteLeavesPreviousContentIntact() throws Exception {
        JsonDataStore store = new JsonDataStore(tempDir.toString());
        store.write("integrity.json", JsonNodeFactory.instance.objectNode().put("state", "original"));

        Path target = store.path("integrity.json");
        try (FileInputStream lock = new FileInputStream(target.toFile())) {
            assertThrows(PersistenceException.class,
                    () -> store.write("integrity.json", JsonNodeFactory.instance.objectNode().put("state", "reemplazo")));
        }

        JsonNode content = store.read("integrity.json");
        assertEquals("original", content.get("state").asText());
        assertTrue(store.exists("integrity.json"));
    }

    @Test
    void writeCreatesMissingDirectory() {
        Path nested = tempDir.resolve("noexiste").resolve("sub");
        JsonDataStore store = new JsonDataStore(nested.toString());

        store.write("data.json", JsonNodeFactory.instance.objectNode().put("ok", true));

        assertTrue(store.exists("data.json"));
        assertEquals(true, store.read("data.json").get("ok").asBoolean());
    }
}
