package com.github.johanneshaberlah.zcjson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.johanneshaberlah.zcjson.JsonDocumentReaderTest.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Array Tests")
class ArrayTest {

    @Test
    @DisplayName("Read array length")
    void testArrayLength() {
        String json = """
            {
              "items": [1, 2, 3, 4, 5]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray items = document.readArray(JsonKey.of("items"));
            assertEquals(5, items.length());
        }
    }

    @Test
    @DisplayName("Read primitive values from array by index")
    void testReadValueByIndex() {
        String json = """
            {
              "numbers": [10, 20, 30]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray numbers = document.readArray(JsonKey.of("numbers"));
            assertEquals("10", segmentToString(numbers.readValueSegment(0)));
            assertEquals("20", segmentToString(numbers.readValueSegment(1)));
            assertEquals("30", segmentToString(numbers.readValueSegment(2)));
        }
    }

    @Test
    @DisplayName("Read nested object from array by index")
    void testReadObjectByIndex() {
        String json = """
            {
              "users": [
                {"name": "Alice"},
                {"name": "Bob"}
              ]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray users = document.readArray(JsonKey.of("users"));

            JsonDocument user0 = users.readObject(0);
            assertEquals("Alice", segmentToString(user0.readValueSegment(JsonKey.of("name"))));

            JsonDocument user1 = users.readObject(1);
            assertEquals("Bob", segmentToString(user1.readValueSegment(JsonKey.of("name"))));
        }
    }

    @Test
    @DisplayName("Read nested array from array by index")
    void testReadArrayByIndex() {
        String json = """
            {
              "matrix": [
                [1, 2, 3],
                [4, 5, 6]
              ]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray matrix = document.readArray(JsonKey.of("matrix"));

            JsonArray row0 = matrix.readArray(0);
            assertEquals(3, row0.length());
            assertEquals("1", segmentToString(row0.readValueSegment(0)));
            assertEquals("2", segmentToString(row0.readValueSegment(1)));
            assertEquals("3", segmentToString(row0.readValueSegment(2)));

            JsonArray row1 = matrix.readArray(1);
            assertEquals(3, row1.length());
            assertEquals("4", segmentToString(row1.readValueSegment(0)));
            assertEquals("5", segmentToString(row1.readValueSegment(1)));
            assertEquals("6", segmentToString(row1.readValueSegment(2)));
        }
    }

    @Test
    @DisplayName("Read mixed type array")
    void testMixedTypeArray() {
        String json = """
            {
              "mixed": ["Hello", true, null, 3.12, 5]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray mixed = document.readArray(JsonKey.of("mixed"));
            assertEquals(5, mixed.length());
            assertEquals("Hello", segmentToString(mixed.readValueSegment(0)));
            assertEquals("true", segmentToString(mixed.readValueSegment(1)));
            assertEquals("null", segmentToString(mixed.readValueSegment(2)));
            assertEquals("3.12", segmentToString(mixed.readValueSegment(3)));
            assertEquals("5", segmentToString(mixed.readValueSegment(4)));
        }
    }

    @Test
    @DisplayName("Read empty array")
    void testEmptyArray() {
        String json = """
            {
              "empty": []
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray empty = document.readArray(JsonKey.of("empty"));
            assertEquals(0, empty.length());
        }
    }

    @Test
    @DisplayName("Read string array")
    void testStringArray() {
        String json = """
            {
              "colors": ["red", "green", "blue"]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray colors = document.readArray(JsonKey.of("colors"));
            assertEquals(3, colors.length());
            assertEquals("red", segmentToString(colors.readValueSegment(0)));
            assertEquals("green", segmentToString(colors.readValueSegment(1)));
            assertEquals("blue", segmentToString(colors.readValueSegment(2)));
        }
    }

    @Test
    @DisplayName("Array index out of bounds throws exception")
    void testArrayIndexOutOfBounds() {
        String json = """
            {
              "items": [1, 2, 3]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray items = document.readArray(JsonKey.of("items"));
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> items.readValueSegment(5));
            assertThrows(ArrayIndexOutOfBoundsException.class, () -> items.readValueSegment(-1));
        }
    }

    @Test
    @DisplayName("Verify array length caching behavior")
    void testArrayLengthCaching() {
        String json = """
            {
              "data": [1, 2, 3, 4]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray data = document.readArray(JsonKey.of("data"));
            // Call length multiple times - should return cached value
            assertEquals(4, data.length());
            assertEquals(4, data.length());
            assertEquals(4, data.length());
        }
    }

    @Test
    @DisplayName("Array with boolean values")
    void testBooleanArray() {
        String json = """
            {
              "flags": [true, false, true]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray flags = document.readArray(JsonKey.of("flags"));
            assertEquals(3, flags.length());
            assertEquals("true", segmentToString(flags.readValueSegment(0)));
            assertEquals("false", segmentToString(flags.readValueSegment(1)));
            assertEquals("true", segmentToString(flags.readValueSegment(2)));
        }
    }

    @Test
    @DisplayName("Complex nested array with objects")
    void testComplexNestedArrayWithObjects() {
        String json = """
            {
              "teams": [
                {
                  "name": "Team A",
                  "members": [
                    {"name": "Alice"},
                    {"name": "Bob"}
                  ]
                },
                {
                  "name": "Team B",
                  "members": [
                    {"name": "Charlie"}
                  ]
                }
              ]
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            JsonArray teams = document.readArray(JsonKey.of("teams"));
            assertEquals(2, teams.length());

            JsonDocument team0 = teams.readObject(0);
            assertEquals("Team A", segmentToString(team0.readValueSegment(JsonKey.of("name"))));
            JsonArray team0Members = team0.readArray(JsonKey.of("members"));
            assertEquals(2, team0Members.length());
            assertEquals("Alice", segmentToString(team0Members.readObject(0).readValueSegment(JsonKey.of("name"))));
            assertEquals("Bob", segmentToString(team0Members.readObject(1).readValueSegment(JsonKey.of("name"))));

            JsonDocument team1 = teams.readObject(1);
            assertEquals("Team B", segmentToString(team1.readValueSegment(JsonKey.of("name"))));
            JsonArray team1Members = team1.readArray(JsonKey.of("members"));
            assertEquals(1, team1Members.length());
            assertEquals("Charlie", segmentToString(team1Members.readObject(0).readValueSegment(JsonKey.of("name"))));
        }
    }
}
