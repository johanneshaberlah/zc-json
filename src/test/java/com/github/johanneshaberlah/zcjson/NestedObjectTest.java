package com.github.johanneshaberlah.zcjson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.johanneshaberlah.JsonDocumentReaderTest.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Nested Object Tests")
class NestedObjectTest {

    @Test
    @DisplayName("Navigate one level deep nested object")
    void testOneLevelNesting() {
        String json = """
            {
              "user": {
                "name": "Alice"
              }
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonDocument user = document.readObject(JsonKey.of("user"));
            assertEquals("Alice", segmentToString(user.readValueSegment(JsonKey.of("name"))));
        }
    }

    @Test
    @DisplayName("Navigate two levels deep nested object")
    void testTwoLevelNesting() {
        String json = """
            {
              "company": {
                "address": {
                  "city": "Hamburg"
                }
              }
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonDocument company = document.readObject(JsonKey.of("company"));
            JsonDocument address = company.readObject(JsonKey.of("address"));
            assertEquals("Hamburg", segmentToString(address.readValueSegment(JsonKey.of("city"))));
        }
    }

    @Test
    @DisplayName("Navigate three levels deep nested object")
    void testThreeLevelNesting() {
        String json = """
            {
              "university": {
                "location": {
                  "city": {
                    "name": "Berlin"
                  }
                }
              }
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonDocument university = document.readObject(JsonKey.of("university"));
            JsonDocument location = university.readObject(JsonKey.of("location"));
            JsonDocument city = location.readObject(JsonKey.of("city"));
            assertEquals("Berlin", segmentToString(city.readValueSegment(JsonKey.of("name"))));
        }
    }

    @Test
    @DisplayName("Multiple nested objects at same level")
    void testMultipleNestedObjectsSameLevel() {
        String json = """
            {
              "person": {
                "name": "John"
              },
              "address": {
                "street": "Main St"
              }
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonDocument person = document.readObject(JsonKey.of("person"));
            assertEquals("John", segmentToString(person.readValueSegment(JsonKey.of("name"))));

            JsonDocument address = document.readObject(JsonKey.of("address"));
            assertEquals("Main St", segmentToString(address.readValueSegment(JsonKey.of("street"))));
        }
    }

    @Test
    @DisplayName("Nested object with mixed value types")
    void testNestedObjectWithMixedTypes() {
        String json = """
            {
              "user": {
                "name": "Alice",
                "age": 30,
                "active": true,
                "score": 98.5,
                "metadata": null
              }
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonDocument user = document.readObject(JsonKey.of("user"));
            assertEquals("Alice", segmentToString(user.readValueSegment(JsonKey.of("name"))));
            assertEquals("30", segmentToString(user.readValueSegment(JsonKey.of("age"))));
            assertEquals("true", segmentToString(user.readValueSegment(JsonKey.of("active"))));
            assertEquals("98.5", segmentToString(user.readValueSegment(JsonKey.of("score"))));
            assertEquals("null", segmentToString(user.readValueSegment(JsonKey.of("metadata"))));
        }
    }

    @Test
    @DisplayName("Empty nested object")
    void testEmptyNestedObject() {
        String json = """
            {
              "config": {}
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonDocument config = document.readObject(JsonKey.of("config"));
            assertNotNull(config);
        }
    }

    @Test
    @DisplayName("Complex nested structure with multiple levels")
    void testComplexNestedStructure() {
        String json = """
            {
              "organization": {
                "name": "TechCorp",
                "headquarters": {
                  "country": "Germany",
                  "city": {
                    "name": "Munich",
                    "population": 1500000
                  }
                },
                "active": true
              }
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonDocument org = document.readObject(JsonKey.of("organization"));
            assertEquals("TechCorp", segmentToString(org.readValueSegment(JsonKey.of("name"))));
            assertEquals("true", segmentToString(org.readValueSegment(JsonKey.of("active"))));

            JsonDocument hq = org.readObject(JsonKey.of("headquarters"));
            assertEquals("Germany", segmentToString(hq.readValueSegment(JsonKey.of("country"))));

            JsonDocument city = hq.readObject(JsonKey.of("city"));
            assertEquals("Munich", segmentToString(city.readValueSegment(JsonKey.of("name"))));
            assertEquals("1500000", segmentToString(city.readValueSegment(JsonKey.of("population"))));
        }
    }
}
