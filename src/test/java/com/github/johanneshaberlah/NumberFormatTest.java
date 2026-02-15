package com.github.johanneshaberlah;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.johanneshaberlah.JsonDocumentReaderTest.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Number Format Tests")
class NumberFormatTest {

    @Test
    @DisplayName("Read positive integer")
    void testPositiveInteger() {
        String json = """
            {
              "count": 42
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("42", segmentToString(document.readValue(JsonKey.of("count"))));
        }
    }

    @Test
    @DisplayName("Read zero")
    void testZero() {
        String json = """
            {
              "value": 0
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("0", segmentToString(document.readValue(JsonKey.of("value"))));
        }
    }

    @Test
    @DisplayName("Read large positive integer")
    void testLargeInteger() {
        String json = """
            {
              "population": 1000000
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("1000000", segmentToString(document.readValue(JsonKey.of("population"))));
        }
    }

    @Test
    @DisplayName("Read negative integer")
    void testNegativeInteger() {
        String json = """
            {
              "temperature": -3
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("-3", segmentToString(document.readValue(JsonKey.of("temperature"))));
        }
    }

    @Test
    @DisplayName("Read large negative integer")
    void testLargeNegativeInteger() {
        String json = """
            {
              "debt": -100000
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("-100000", segmentToString(document.readValue(JsonKey.of("debt"))));
        }
    }

    @Test
    @DisplayName("Read floating point number")
    void testFloatingPoint() {
        String json = """
            {
              "pi": 3.14159
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("3.14159", segmentToString(document.readValue(JsonKey.of("pi"))));
        }
    }

    @Test
    @DisplayName("Read decimal less than one")
    void testDecimalLessThanOne() {
        String json = """
            {
              "fraction": 0.5
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("0.5", segmentToString(document.readValue(JsonKey.of("fraction"))));
        }
    }

    @Test
    @DisplayName("Read scientific notation lowercase")
    void testScientificNotationLowercase() {
        String json = """
            {
              "large": 3e5
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("3e5", segmentToString(document.readValue(JsonKey.of("large"))));
        }
    }

    @Test
    @DisplayName("Read scientific notation with negative exponent")
    void testScientificNotationNegativeExponent() {
        String json = """
            {
              "small": 2.5e-3
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("2.5e-3", segmentToString(document.readValue(JsonKey.of("small"))));
        }
    }

    @Test
    @DisplayName("Read scientific notation uppercase")
    void testScientificNotationUppercase() {
        String json = """
            {
              "huge": 1.5E10
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("1.5E10", segmentToString(document.readValue(JsonKey.of("huge"))));
        }
    }

    @Test
    @DisplayName("Read scientific notation with positive exponent")
    void testScientificNotationPositiveExponent() {
        String json = """
            {
              "value": 1e+8
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("1e+8", segmentToString(document.readValue(JsonKey.of("value"))));
        }
    }

    @Test
    @DisplayName("Read mixed number formats")
    void testMixedNumberFormats() {
        String json = """
            {
              "int": 100,
              "negative": -50,
              "float": 3.14,
              "scientific": 1.2e3
            }
            """;

        try (JsonDocument document = parseJson(json)) {
            assertEquals("100", segmentToString(document.readValue(JsonKey.of("int"))));
            assertEquals("-50", segmentToString(document.readValue(JsonKey.of("negative"))));
            assertEquals("3.14", segmentToString(document.readValue(JsonKey.of("float"))));
            assertEquals("1.2e3", segmentToString(document.readValue(JsonKey.of("scientific"))));
        }
    }
}
