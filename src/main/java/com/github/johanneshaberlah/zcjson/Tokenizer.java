package com.github.johanneshaberlah.zcjson;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class Tokenizer {
  private static final byte OBJECT_START = '{';
  private static final byte OBJECT_END = '}';
  private static final byte ARRAY_START = '[';
  private static final byte ARRAY_END = ']';
  private static final byte STRING_LITERAL = '"';

  private static final byte COLON = ':';
  private static final byte COMMA = ',';
  private static final byte WHITESPACE = ' ';

  private static final byte[] TRUE = "true".getBytes(StandardCharsets.UTF_8);
  private static final byte[] FALSE = "false".getBytes(StandardCharsets.UTF_8);
  private static final byte[] NULL = "null".getBytes(StandardCharsets.UTF_8);

  private static final boolean[] IS_NUMERIC = new boolean[256];

  private long[] tokens = new long[1024];
  private int tokenCount = 0;

  public Tokenizer() {
    for (char c : "0123456789+-eE.".toCharArray()) {
      IS_NUMERIC[c] = true;
    }
  }

  public long[] read(MemorySegment segment) {
    long length = segment.byteSize();
    Arrays.fill(tokens, -1);

    for (long index = 0; index < length; index++) {
      byte symbol = segment.get(ValueLayout.JAVA_BYTE, index);
      if (symbol <= WHITESPACE) {
        continue;
      }
      switch (symbol) {
        case STRING_LITERAL:
          long start = index;
          while (++index < length && segment.get(ValueLayout.JAVA_BYTE, index) != STRING_LITERAL);
          int tokenType = nextEquals(segment, index + 1, COLON) ? Token.KEY : Token.STRING;
          addToken(tokenType, (int) (start + 1), (int) (index - start - 1));
          break;
        case 't':
          expectValue(segment, index, TRUE);
          addToken(Token.BOOLEAN_TRUE, (int) index, 4);
          index += 3;
          break;
        case 'f':
          expectValue(segment, index, FALSE);
          addToken(Token.BOOLEAN_FALSE, (int) index, 5);
          index += 4;
          break;
        case 'n':
          expectValue(segment, index, NULL);
          addToken(Token.NULL, (int) index, 4);
          index += 3;
          break;
        case ARRAY_START:
          addToken(Token.ARRAY_START_MARKER, (int) index, 1);
          break;
        case ARRAY_END:
          addToken(Token.ARRAY_END_MARKER, (int) index, 1);
          break;
        case '-':
        case '0': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
          long numStart = index;
          while (++index < length && IS_NUMERIC[segment.get(ValueLayout.JAVA_BYTE, index) & 0xFF]);
          int numLength = (int) (index - numStart);
          addToken(Token.NUMBER, (int) numStart, numLength);
          index--;
          break;
        case OBJECT_START:
          addToken(Token.OBJECT_START_MARKER, (int) index, 1);
          break;
        case OBJECT_END:
          addToken(Token.OBJECT_END_MARKER, (int) index, 1);
          break;
        case COLON:
        case COMMA:
          break;
        default:
          throw new TokenizerException(
            String.format("Unexpected symbol '%c' at char %d", symbol, index)
          );
      }
    }
    return tokens;
  }

  private boolean nextEquals(MemorySegment segment, long index, byte value) {
    long length = segment.byteSize();
    while (index < length) {
      byte symbol = segment.get(ValueLayout.JAVA_BYTE, index);
      if (symbol <= WHITESPACE) {
        index++;
        continue;
      }
      return symbol == value;
    }
    return false;
  }

  private void expectValue(MemorySegment segment, long index, byte[] expectedValue) {
    for (int i = 0; i < expectedValue.length; i++) {
      byte actualByte = segment.get(ValueLayout.JAVA_BYTE, index + i);
      if (actualByte != expectedValue[i]) {
        throw new TokenizerException("Expected literal match failed at index: " + (index + i));
      }
    }
  }

  private void addToken(int type, int start, int len) {
    if (tokenCount >= tokens.length) {
      int newCapacity = tokens.length * 2;
      long[] newTokens = new long[newCapacity];
      System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
      this.tokens = newTokens;
    }
    long token = ((long) type << 60) | ((long) start << 30) | (long) len;
    tokens[tokenCount++] = token;
  }
}
