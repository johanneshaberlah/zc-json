package com.github.johanneshaberlah;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonValue Conversion Tests")
class JsonValueTest {

    @Nested
    @DisplayName("asString() conversion tests")
    class StringConversionTests {

        @Test
        @DisplayName("Basic string conversion")
        void testBasicString() {
            JsonValue value = createJsonValue("hello");
            assertEquals("hello", value.asString());
        }

        @Test
        @DisplayName("Empty string")
        void testEmptyString() {
            JsonValue value = createJsonValue("");
            assertEquals("", value.asString());
        }

        @Test
        @DisplayName("Numeric string")
        void testNumericString() {
            JsonValue value = createJsonValue("12345");
            assertEquals("12345", value.asString());
        }

        @Test
        @DisplayName("String with spaces")
        void testStringWithSpaces() {
            JsonValue value = createJsonValue("hello world");
            assertEquals("hello world", value.asString());
        }

        @Test
        @DisplayName("UTF-8 multi-byte characters - Chinese")
        void testChineseCharacters() {
            JsonValue value = createJsonValue("你好世界");
            assertEquals("你好世界", value.asString());
        }

        @Test
        @DisplayName("Special characters")
        void testSpecialCharacters() {
            JsonValue value = createJsonValue("!@#$%^&*()");
            assertEquals("!@#$%^&*()", value.asString());
        }

        @Test
        @DisplayName("Control characters - newline and tab")
        void testControlCharacters() {
            JsonValue value = createJsonValue("line1\nline2\ttab");
            assertEquals("line1\nline2\ttab", value.asString());
        }

        @Test
        @DisplayName("Unicode emoji - 4-byte UTF-8")
        void testEmoji() {
            JsonValue value = createJsonValue("Hello 👋 World 🌍");
            assertEquals("Hello 👋 World 🌍", value.asString());
        }
    }

    // ========== Long Conversion Tests ==========

    @Nested
    @DisplayName("asLong() conversion tests")
    class LongConversionTests {

        @Test
        @DisplayName("Zero")
        void testZero() {
            JsonValue value = createJsonValue("0");
            assertEquals(0L, value.asLong());
        }

        @Test
        @DisplayName("Positive single digit")
        void testPositiveSingleDigit() {
            JsonValue value = createJsonValue("5");
            assertEquals(5L, value.asLong());
        }

        @Test
        @DisplayName("Negative single digit")
        void testNegativeSingleDigit() {
            JsonValue value = createJsonValue("-7");
            assertEquals(-7L, value.asLong());
        }

        @Test
        @DisplayName("Positive number")
        void testPositiveNumber() {
            JsonValue value = createJsonValue("12345");
            assertEquals(12345L, value.asLong());
        }

        @Test
        @DisplayName("Negative number")
        void testNegativeNumber() {
            JsonValue value = createJsonValue("-9876");
            assertEquals(-9876L, value.asLong());
        }

        @Test
        @DisplayName("Plus sign prefix")
        void testPlusSignPrefix() {
            JsonValue value = createJsonValue("+123");
            assertEquals(123L, value.asLong());
        }

        @Test
        @DisplayName("Large positive value")
        void testLargePositive() {
            JsonValue value = createJsonValue("999999999999");
            assertEquals(999999999999L, value.asLong());
        }

        @Test
        @DisplayName("Large negative value")
        void testLargeNegative() {
            JsonValue value = createJsonValue("-888888888888");
            assertEquals(-888888888888L, value.asLong());
        }

        @Test
        @DisplayName("Long.MAX_VALUE boundary")
        void testMaxLongValue() {
            JsonValue value = createJsonValue(String.valueOf(Long.MAX_VALUE));
            assertEquals(Long.MAX_VALUE, value.asLong());
        }

        @Test
        @DisplayName("Long.MIN_VALUE boundary")
        void testMinLongValue() {
            JsonValue value = createJsonValue(String.valueOf(Long.MIN_VALUE));
            assertEquals(Long.MIN_VALUE, value.asLong());
        }

        @Test
        @DisplayName("Leading zeros")
        void testLeadingZeros() {
            JsonValue value = createJsonValue("00123");
            assertEquals(123L, value.asLong());
        }

        @Test
        @DisplayName("Empty string returns zero")
        void testEmptyString() {
            JsonValue value = createJsonValue("");
            assertEquals(0L, value.asLong());
        }

        @Test
        @DisplayName("Just minus sign")
        void testJustMinus() {
            JsonValue value = createJsonValue("-");
            // This will result in 0 since there are no digits after the minus
            assertEquals(0L, value.asLong());
        }

        @Test
        @DisplayName("Just plus sign")
        void testJustPlus() {
            JsonValue value = createJsonValue("+");
            // This will result in 0 since there are no digits after the plus
            assertEquals(0L, value.asLong());
        }

        @Test
        @DisplayName("Overflow behavior - value wraps around")
        void testOverflowBehavior() {
            // Test that overflow is not detected and value wraps
            // This documents actual behavior, not ideal behavior
            String overflowValue = "9223372036854775808"; // Long.MAX_VALUE + 1
            JsonValue value = createJsonValue(overflowValue);
            // Value will wrap to negative due to overflow
            assertTrue(value.asLong() < 0);
        }
    }

    // ========== Integer Conversion Tests ==========

    @Nested
    @DisplayName("asInteger() conversion tests")
    class IntegerConversionTests {

        @Test
        @DisplayName("Zero")
        void testZero() {
            JsonValue value = createJsonValue("0");
            assertEquals(0, value.asInteger());
        }

        @Test
        @DisplayName("Positive integer")
        void testPositiveInteger() {
            JsonValue value = createJsonValue("42");
            assertEquals(42, value.asInteger());
        }

        @Test
        @DisplayName("Negative integer")
        void testNegativeInteger() {
            JsonValue value = createJsonValue("-100");
            assertEquals(-100, value.asInteger());
        }

        @Test
        @DisplayName("Integer.MAX_VALUE boundary")
        void testMaxIntValue() {
            JsonValue value = createJsonValue(String.valueOf(Integer.MAX_VALUE));
            assertEquals(Integer.MAX_VALUE, value.asInteger());
        }

        @Test
        @DisplayName("Integer.MIN_VALUE boundary")
        void testMinIntValue() {
            JsonValue value = createJsonValue(String.valueOf(Integer.MIN_VALUE));
            assertEquals(Integer.MIN_VALUE, value.asInteger());
        }

        @Test
        @DisplayName("Value overflow to negative - Integer.MAX_VALUE + 1")
        void testIntegerOverflow() {
            // Integer.MAX_VALUE + 1 will overflow in the int cast
            JsonValue value = createJsonValue("2147483648");
            assertEquals(Integer.MIN_VALUE, value.asInteger());
        }

        @Test
        @DisplayName("Long value truncation")
        void testLongTruncation() {
            // Test that long values get truncated to int range
            JsonValue value = createJsonValue("9999999999"); // > Integer.MAX_VALUE
            int result = value.asInteger();
            // Value will be truncated via cast
            assertNotEquals(9999999999L, result);
        }

        @Test
        @DisplayName("Empty string returns zero")
        void testEmptyString() {
            JsonValue value = createJsonValue("");
            assertEquals(0, value.asInteger());
        }
    }

    // ========== Double Conversion Tests ==========

    @Nested
    @DisplayName("asDouble() conversion tests")
    class DoubleConversionTests {

        @Test
        @DisplayName("Zero")
        void testZero() {
            JsonValue value = createJsonValue("0");
            assertEquals(0.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Decimal zero")
        void testDecimalZero() {
            JsonValue value = createJsonValue("0.0");
            assertEquals(0.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Positive integer as double")
        void testPositiveInteger() {
            JsonValue value = createJsonValue("42");
            assertEquals(42.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Negative integer as double")
        void testNegativeInteger() {
            JsonValue value = createJsonValue("-100");
            assertEquals(-100.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Plus sign prefix")
        void testPlusSignPrefix() {
            JsonValue value = createJsonValue("+50");
            assertEquals(50.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Simple decimal - pi approximation")
        void testSimpleDecimal() {
            JsonValue value = createJsonValue("3.14");
            assertEquals(3.14, value.asDouble(), 0.0001);
        }

        @Test
        @DisplayName("Decimal less than one")
        void testDecimalLessThanOne() {
            JsonValue value = createJsonValue("0.5");
            assertEquals(0.5, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Negative decimal")
        void testNegativeDecimal() {
            JsonValue value = createJsonValue("-2.718");
            assertEquals(-2.718, value.asDouble(), 0.0001);
        }

        @Test
        @DisplayName("Multiple decimal places for precision")
        void testMultipleDecimalPlaces() {
            JsonValue value = createJsonValue("123.456789");
            assertEquals(123.456789, value.asDouble(), 0.000001);
        }

        @Test
        @DisplayName("Scientific notation lowercase - 3e5")
        void testScientificNotationLowercase() {
            JsonValue value = createJsonValue("3e5");
            assertEquals(300000.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Scientific notation uppercase - 3E5")
        void testScientificNotationUppercase() {
            JsonValue value = createJsonValue("3E5");
            assertEquals(300000.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Scientific notation negative exponent - 2.5e-3")
        void testScientificNotationNegativeExponent() {
            JsonValue value = createJsonValue("2.5e-3");
            assertEquals(0.0025, value.asDouble(), 0.000001);
        }

        @Test
        @DisplayName("Scientific notation positive exponent - 1e+8")
        void testScientificNotationPositiveExponent() {
            JsonValue value = createJsonValue("1e+8");
            assertEquals(100000000.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Combined decimal and scientific - 1.5E10")
        void testCombinedDecimalScientific() {
            JsonValue value = createJsonValue("1.5E10");
            assertEquals(15000000000.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Very large number")
        void testVeryLargeNumber() {
            JsonValue value = createJsonValue("9.9e307");
            assertEquals(9.9e307, value.asDouble(), 1e300);
        }

        @Test
        @DisplayName("Very small number")
        void testVerySmallNumber() {
            JsonValue value = createJsonValue("1.23e-100");
            assertEquals(1.23e-100, value.asDouble(), 1e-110);
        }

        @Test
        @DisplayName("Zero exponent - 5e0")
        void testZeroExponent() {
            JsonValue value = createJsonValue("5e0");
            assertEquals(5.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Empty string returns zero")
        void testEmptyString() {
            JsonValue value = createJsonValue("");
            assertEquals(0.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Real-world example from example.json - 23.3333e42")
        void testRealWorldExample() {
            JsonValue value = createJsonValue("23.3333e42");
            assertEquals(23.3333e42, value.asDouble(), 1e35);
        }

        @Test
        @DisplayName("Negative scientific notation")
        void testNegativeScientificNotation() {
            JsonValue value = createJsonValue("-6.02e23");
            assertEquals(-6.02e23, value.asDouble(), 1e15);
        }
    }

    // ========== Float Conversion Tests ==========

    @Nested
    @DisplayName("asFloat() conversion tests")
    class FloatConversionTests {

        @Test
        @DisplayName("Zero")
        void testZero() {
            JsonValue value = createJsonValue("0");
            assertEquals(0.0f, value.asFloat(), 0.0f);
        }

        @Test
        @DisplayName("Positive float")
        void testPositiveFloat() {
            JsonValue value = createJsonValue("3.14");
            assertEquals(3.14f, value.asFloat(), 0.001f);
        }

        @Test
        @DisplayName("Negative float")
        void testNegativeFloat() {
            JsonValue value = createJsonValue("-2.5");
            assertEquals(-2.5f, value.asFloat(), 0.0f);
        }

        @Test
        @DisplayName("Float.MAX_VALUE approximation")
        void testMaxFloatValue() {
            JsonValue value = createJsonValue("3.4e38");
            assertTrue(value.asFloat() > 0);
            assertTrue(Float.isFinite(value.asFloat()));
        }

        @Test
        @DisplayName("Float.MIN_VALUE approximation")
        void testMinFloatValue() {
            JsonValue value = createJsonValue("1.4e-45");
            assertTrue(value.asFloat() >= 0);
        }

        @Test
        @DisplayName("Precision loss from double to float")
        void testPrecisionLoss() {
            // A value that has more precision than float can represent
            JsonValue value = createJsonValue("1.123456789012345");
            float result = value.asFloat();
            // Float has less precision than double
            assertNotEquals(1.123456789012345, result, 0.0);
        }

        @Test
        @DisplayName("Scientific notation in float")
        void testScientificNotation() {
            JsonValue value = createJsonValue("2.5e3");
            assertEquals(2500.0f, value.asFloat(), 0.0f);
        }

        @Test
        @DisplayName("Value overflow to infinity")
        void testOverflowToInfinity() {
            // A value too large for float should become infinity
            JsonValue value = createJsonValue("1e100");
            assertEquals(Float.POSITIVE_INFINITY, value.asFloat());
        }
    }

    // ========== Factory Method Tests ==========

    @Nested
    @DisplayName("of() factory method tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Creates valid JsonValue instance")
        void testFactoryCreatesInstance() {
            MemorySegment segment = stringToSegment("test");
            JsonValue value = JsonValue.of(segment);
            assertNotNull(value);
            assertEquals("test", value.asString());
        }

        @Test
        @DisplayName("Works with empty segment")
        void testEmptySegment() {
            MemorySegment segment = stringToSegment("");
            JsonValue value = JsonValue.of(segment);
            assertNotNull(value);
            assertEquals("", value.asString());
        }

        @Test
        @DisplayName("Handles different segment sizes")
        void testDifferentSegmentSizes() {
            String smallString = "a";
            String largeString = "a".repeat(10000);

            JsonValue small = JsonValue.of(stringToSegment(smallString));
            JsonValue large = JsonValue.of(stringToSegment(largeString));

            assertEquals(smallString, small.asString());
            assertEquals(largeString, large.asString());
        }
    }

    // ========== Edge Case Tests ==========

    @Nested
    @DisplayName("Edge cases and error conditions")
    class EdgeCaseTests {

        @Test
        @DisplayName("Invalid characters in numeric string - no validation, produces incorrect result")
        void testInvalidCharactersInNumber() {
            JsonValue value = createJsonValue("12a34");
            // No input validation - 'a' (ASCII 97) minus '0' (ASCII 48) = 49
            // This documents that the class doesn't validate input
            assertDoesNotThrow(value::asLong);
            // The result will be mathematically incorrect but no exception thrown
        }

        @Test
        @DisplayName("Multiple decimal points - parsed until second decimal")
        void testMultipleDecimalPoints() {
            JsonValue value = createJsonValue("3.14.159");
            // Parsing stops at the second decimal point (treated as end of number)
            // This documents actual behavior - no validation
            double result = value.asDouble();
            assertDoesNotThrow(() -> value.asDouble());
        }

        @Test
        @DisplayName("Multiple minus signs - treated as invalid digit")
        void testMultipleMinusSigns() {
            JsonValue value = createJsonValue("--5");
            // First minus makes it negative, second minus (ASCII 45) is treated as a digit
            // No validation - produces incorrect result
            assertDoesNotThrow(value::asLong);
        }

        @Test
        @DisplayName("Spaces in numbers - treated as invalid digits")
        void testSpacesInNumbers() {
            JsonValue value = createJsonValue("1 2 3");
            // Space characters (ASCII 32) minus '0' (ASCII 48) gives negative values
            // No validation - produces mathematically incorrect result
            assertDoesNotThrow(value::asLong);
        }

        @Test
        @DisplayName("Exponent without mantissa - parsed as invalid")
        void testExponentWithoutMantissa() {
            JsonValue value = createJsonValue("e5");
            // 'e' character will be treated as a digit (incorrectly)
            // No validation - produces incorrect result
            assertDoesNotThrow(value::asLong);
        }

        @Test
        @DisplayName("Decimal point without digits - returns zero")
        void testDecimalPointWithoutDigits() {
            JsonValue value = createJsonValue(".");
            // Decimal point alone - mantissa stays 0, returns 0.0
            assertEquals(0.0, value.asDouble(), 0.0);
        }

        @Test
        @DisplayName("Very long numeric string - stress test")
        void testVeryLongNumericString() {
            String longNumber = "1" + "0".repeat(1000);
            JsonValue value = createJsonValue(longNumber);
            // Should handle very long strings (though value may overflow)
            assertDoesNotThrow(value::asLong);
        }

        @Test
        @DisplayName("Non-ASCII digits - no validation, incorrect result")
        void testNonASCIIDigits() {
            // Arabic-Indic digit for 5: ٥ (U+0665)
            JsonValue value = createJsonValue("٥");
            // No validation - non-ASCII digits produce incorrect mathematical results
            assertDoesNotThrow(value::asLong);
            // Result will not be 5, as the byte values are different
        }

        @Test
        @DisplayName("Decimal with negative sign")
        void testDecimalWithNegativeSign() {
            JsonValue value = createJsonValue("-0.001");
            assertEquals(-0.001, value.asDouble(), 0.0001);
        }

        @Test
        @DisplayName("Leading zeros in decimal")
        void testLeadingZerosInDecimal() {
            JsonValue value = createJsonValue("00.5");
            assertEquals(0.5, value.asDouble(), 0.0);
        }
    }

  private static MemorySegment stringToSegment(String value) {
    return MemorySegment.ofArray(value.getBytes(StandardCharsets.UTF_8));
  }

  private static JsonValue createJsonValue(String value) {
    return JsonValue.of(stringToSegment(value));
  }

}
