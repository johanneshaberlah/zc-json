package com.github.johanneshaberlah;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.johanneshaberlah.JsonDocumentReaderTest.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Edge Case Tests")
class EdgeCaseTest {

    @Test
    @DisplayName("JSON with tabs and newlines")
    void testWhitespaceVariations() {
        String json = "{\n\t\"name\":\t\"Alice\",\n\t\"age\":\t30\n}";

        try (JsonDocument document = parseJson(json)) {
            assertEquals("Alice", segmentToString(document.readValue(JsonKey.of("name"))));
            assertEquals("30", segmentToString(document.readValue(JsonKey.of("age"))));
        }
    }

    @Test
    @DisplayName("String value with spaces")
    void testStringWithSpaces() {
        String json = """
            {
              "message": "Hello World from JSON"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("Hello World from JSON", segmentToString(document.readValue(JsonKey.of("message"))));
        }
    }

    @Test
    @DisplayName("Very long string value")
    void testVeryLongString() {
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longString.append("a");
        }
        String json = String.format("""
            {
              "long": "%s"
            }
            """, longString);

        try (JsonDocument document = parseJson(json)) {
            assertEquals(longString.toString(), segmentToString(document.readValue(JsonKey.of("long"))));
        }
    }

    @Test
    @DisplayName("Object with many keys")
    void testManyKeys() {
        StringBuilder jsonBuilder = new StringBuilder("{\n");
        for (int i = 0; i < 50; i++) {
            jsonBuilder.append(String.format("  \"key%d\": \"value%d\"", i, i));
            if (i < 49) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }
        jsonBuilder.append("}");

        try (JsonDocument document = parseJson(jsonBuilder.toString())) {
            for (int i = 0; i < 50; i++) {
                assertEquals("value" + i, segmentToString(document.readValue(JsonKey.of("key" + i))));
            }
        }
    }

    @Test
    @DisplayName("Minimal JSON with no whitespace")
    void testMinimalJSON() {
        String json = "{\"a\":\"b\"}";

        try (JsonDocument document = parseJson(json)) {
            assertEquals("b", segmentToString(document.readValue(JsonKey.of("a"))));
        }
    }

    @Test
    @DisplayName("Key names with numbers")
    void testKeyNamesWithNumbers() {
        String json = """
            {
              "field1": "value1",
              "field2": "value2",
              "field123": "value123"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("value1", segmentToString(document.readValue(JsonKey.of("field1"))));
            assertEquals("value2", segmentToString(document.readValue(JsonKey.of("field2"))));
            assertEquals("value123", segmentToString(document.readValue(JsonKey.of("field123"))));
        }
    }

    @Test
    @DisplayName("String with special characters in path")
    void testSpecialCharactersInPath() {
        String json = """
            {
              "path": "/usr/local/bin/app",
              "url": "https://example.com/api/v1/users?id=123"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("/usr/local/bin/app", segmentToString(document.readValue(JsonKey.of("path"))));
            assertEquals("https://example.com/api/v1/users?id=123", segmentToString(document.readValue(JsonKey.of("url"))));
        }
    }

    @Test
    @DisplayName("Empty string keys not recommended but valid")
    void testEmptyStringValue() {
        String json = """
            {
              "empty1": "",
              "empty2": "",
              "nonEmpty": "value"
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("", segmentToString(document.readValue(JsonKey.of("empty1"))));
            assertEquals("", segmentToString(document.readValue(JsonKey.of("empty2"))));
            assertEquals("value", segmentToString(document.readValue(JsonKey.of("nonEmpty"))));
        }
    }

    @Test
    @DisplayName("Complex real-world JSON integration test")
    void testComplexRealWorldJSON() {
        String json = """
            {
              "name": "TechCorp",
              "active": true,
              "employees": [
                {
                  "id": 1,
                  "name": "Alice",
                  "roles": ["developer", "team-lead"]
                },
                {
                  "id": 2,
                  "name": "Bob",
                  "roles": ["designer"]
                }
              ],
              "headquarters": {
                "country": "Germany",
                "city": {
                  "name": "Berlin",
                  "population": 3600000
                }
              },
              "revenue": 1.5e6,
              "metadata": null
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            // Root level primitives
            assertEquals("TechCorp", segmentToString(document.readValue(JsonKey.of("name"))));
            assertEquals("true", segmentToString(document.readValue(JsonKey.of("active"))));
            assertEquals("1.5e6", segmentToString(document.readValue(JsonKey.of("revenue"))));
            assertEquals("null", segmentToString(document.readValue(JsonKey.of("metadata"))));

            // Array with nested objects
            JsonArray employees = document.readArray(JsonKey.of("employees"));
            assertEquals(2, employees.length());

            JsonDocument alice = employees.readObject(0);
            assertEquals("1", segmentToString(alice.readValue(JsonKey.of("id"))));
            assertEquals("Alice", segmentToString(alice.readValue(JsonKey.of("name"))));
            JsonArray aliceRoles = alice.readArray(JsonKey.of("roles"));
            assertEquals(2, aliceRoles.length());
            assertEquals("developer", segmentToString(aliceRoles.readValue(0)));
            assertEquals("team-lead", segmentToString(aliceRoles.readValue(1)));

            JsonDocument bob = employees.readObject(1);
            assertEquals("2", segmentToString(bob.readValue(JsonKey.of("id"))));
            assertEquals("Bob", segmentToString(bob.readValue(JsonKey.of("name"))));
            JsonArray bobRoles = bob.readArray(JsonKey.of("roles"));
            assertEquals(1, bobRoles.length());
            assertEquals("designer", segmentToString(bobRoles.readValue(0)));

            // Nested objects (3 levels deep)
            JsonDocument hq = document.readObject(JsonKey.of("headquarters"));
            assertEquals("Germany", segmentToString(hq.readValue(JsonKey.of("country"))));
            JsonDocument city = hq.readObject(JsonKey.of("city"));
            assertEquals("Berlin", segmentToString(city.readValue(JsonKey.of("name"))));
            assertEquals("3600000", segmentToString(city.readValue(JsonKey.of("population"))));
        }
    }
}
