package com.github.johanneshaberlah.zcjson;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// API to enhance dev-experience at the cost of one allocation per call and one allocation for this wrapper - no string
// transformation required for number handling.
public class JsonValue {
  private final Charset charset = StandardCharsets.UTF_8;
  private final MemorySegment segment;

  private static final byte ZERO = '0';
  private static final byte PLUS = '+';
  private static final byte MINUS = '-';
  private static final byte DOT = '.';
  private static final byte E_LOWER = 'e';
  private static final byte E_UPPER = 'E';

  private static final MemorySegment TRUE_BYTES = Arena.global().allocateFrom("true", StandardCharsets.UTF_8);
  private static final MemorySegment FALSE_BYTES = Arena.global().allocateFrom("false", StandardCharsets.UTF_8);
  private static final MemorySegment NULL_BYTES = Arena.global().allocateFrom("null", StandardCharsets.UTF_8);

  private JsonValue(MemorySegment segment) {
    this.segment = segment;
  }

  public boolean isNull() {
    return segment.mismatch(NULL_BYTES) == -1;
  }

  public boolean nonNull() {
    return !isNull();
  }

  public String asString() {
    return charset.decode(segment.asByteBuffer()).toString();
  }

  public boolean asBoolean() {
    return Boolean.parseBoolean(asString());
  }

  public long asLong() {
    final long length = segment.byteSize();
    if (length == 0) return 0;

    long index = 0;
    byte first = segment.get(ValueLayout.JAVA_BYTE, 0);
    boolean isNegative = (first == MINUS);

    if (isNegative || first == PLUS) {
      index = 1;
    }

    long value = 0;
    for (; index < length; index++) {
      byte digitChar = segment.get(ValueLayout.JAVA_BYTE, index);
      value = value * 10 + (digitChar - ZERO);
    }

    return isNegative ? -value : value;
  }

  public int asInteger() {
    return (int) asLong();
  }

  public double asDouble() {
    final long length = segment.byteSize();
    if (length == 0) return 0.0;

    long index = 0;
    byte first = segment.get(ValueLayout.JAVA_BYTE, 0);
    boolean isNegative = (first == MINUS);

    if (isNegative || first == PLUS) {
      index = 1;
    }

    double mantissa = 0.0;
    int decimalExponent = 0;
    boolean parsingFraction = false;

    while (index < length) {
      byte c = segment.get(ValueLayout.JAVA_BYTE, index);

      if (c == DOT) {
        parsingFraction = true;
        index++;
        continue;
      }

      if (c == E_LOWER || c == E_UPPER) {
        index++;
        break;
      }
      mantissa = mantissa * 10 + (c - ZERO);
      if (parsingFraction) {
        decimalExponent--;
      }
      index++;
    }

    if (index < length) {
      int exponentPart = 0;
      byte expFirst = segment.get(ValueLayout.JAVA_BYTE, index);
      boolean expNegative = (expFirst == MINUS);

      if (expNegative || expFirst == PLUS) {
        index++;
      }

      while (index < length) {
        exponentPart = exponentPart * 10 + (segment.get(ValueLayout.JAVA_BYTE, index) - ZERO);
        index++;
      }
      decimalExponent += expNegative ? -exponentPart : exponentPart;
    }

    double value = mantissa * Math.pow(10, decimalExponent);
    return isNegative ? -value : value;
  }

  public float asFloat() {
    return (float) asDouble();
  }

  public static JsonValue of(MemorySegment segment) {
    return new JsonValue(segment);
  }
}