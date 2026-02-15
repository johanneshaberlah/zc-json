package com.github.johanneshaberlah.zcjson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.johanneshaberlah.JsonDocumentReaderTest.*;

@DisplayName("Primitive Value Tests")
class PrimitiveValueTest {

    @Test
    @DisplayName("Read string value")
    void testStringValue() {
        String json = """
            {
              "message": "Hello, World!"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("Hello, World!", segmentToString(document.readValueSegment(JsonKey.of("message"))));
        }
    }

    @Test
    @DisplayName("Read empty string value")
    void testEmptyString() {
        String json = """
            {
              "empty": ""
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("", segmentToString(document.readValueSegment(JsonKey.of("empty"))));
        }
    }

    @Test
    @DisplayName("Read boolean true value")
    void testBooleanTrue() {
        String json = """
            {
              "active": true
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("true", segmentToString(document.readValueSegment(JsonKey.of("active"))));
        }
    }

    @Test
    @DisplayName("Read boolean false value")
    void testBooleanFalse() {
        String json = """
            {
              "inactive": false
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("false", segmentToString(document.readValueSegment(JsonKey.of("inactive"))));
        }
    }

    @Test
    @DisplayName("Read null value")
    void testNullValue() {
        String json = """
            {
              "optional": null
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("null", segmentToString(document.readValueSegment(JsonKey.of("optional"))));
        }
    }

    @Test
    @DisplayName("Read mixed primitive types in single object")
    void testMixedPrimitives() {
        String json = """
            {
              "name": "Alice",
              "active": true,
              "age": "25",
              "score": "98.5",
              "metadata": null
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("Alice", segmentToString(document.readValueSegment(JsonKey.of("name"))));
            assertEquals("true", segmentToString(document.readValueSegment(JsonKey.of("active"))));
            assertEquals("25", segmentToString(document.readValueSegment(JsonKey.of("age"))));
            assertEquals("98.5", segmentToString(document.readValueSegment(JsonKey.of("score"))));
            assertEquals("null", segmentToString(document.readValueSegment(JsonKey.of("metadata"))));
        }
    }

    @Test
    @DisplayName("Read string with special characters")
    void testStringWithSpecialCharacters() {
        String json = """
            {
              "path": "/home/user/file.txt",
              "email": "user@example.com"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("/home/user/file.txt", segmentToString(document.readValueSegment(JsonKey.of("path"))));
            assertEquals("user@example.com", segmentToString(document.readValueSegment(JsonKey.of("email"))));
        }
    }
}
