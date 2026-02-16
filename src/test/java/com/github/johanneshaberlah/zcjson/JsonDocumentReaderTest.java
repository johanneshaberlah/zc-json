package com.github.johanneshaberlah.zcjson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonDocumentReader Base Tests")
class JsonDocumentReaderTest {

    @Test
    @DisplayName("Parse empty JSON object")
    void testEmptyObject() {
        String json = "{}";

        try (JsonDocument document = parseJson(json)) {
            assertNotNull(document);
        }
    }

    @Test
    @DisplayName("Parse simple JSON object with single key-value pair")
    void testSimpleObject() {
        String json = """
            {
              "name": "Alice"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            MemorySegment value = document.readValueSegment(JsonKey.of("name"));
            assertEquals("Alice", segmentToString(value));
        }
    }

    @Test
    @DisplayName("Parse JSON object with multiple keys")
    void testMultipleKeys() {
        String json = """
            {
              "firstName": "John",
              "lastName": "Doe",
              "age": "30"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("John", segmentToString(document.readValueSegment(JsonKey.of("firstName"))));
            assertEquals("Doe", segmentToString(document.readValueSegment(JsonKey.of("lastName"))));
            assertEquals("30", segmentToString(document.readValueSegment(JsonKey.of("age"))));
        }
    }

    @Test
    @DisplayName("Parse JSON with extensive whitespace")
    void testWhitespaceHandling() {
        String json = """
            {
              "key1"  :  "value1"  ,
              "key2"  :  "value2"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("value1", segmentToString(document.readValueSegment(JsonKey.of("key1"))));
            assertEquals("value2", segmentToString(document.readValueSegment(JsonKey.of("key2"))));
        }
    }

  protected static MemorySegment jsonToSegment(String json) {
    return MemorySegment.ofArray(json.getBytes(StandardCharsets.UTF_8));
  }

  protected static String segmentToString(MemorySegment segment) {
    return StandardCharsets.UTF_8.decode(segment.asByteBuffer()).toString();
  }

  protected static JsonDocument parseJson(String json) {
    return JsonDocumentReader.simdTokenizer().read(jsonToSegment(json));
  }
}
