package com.github.johanneshaberlah;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

public class JsonArray {
  private final JsonDocument parent;
  private final int start;
  private final int end;

  private int length = -1;

  JsonArray(JsonDocument parent, int start, int end) {
    this.parent = parent;
    this.start = start;
    this.end = end;
  }

  public MemorySegment readValue(int index) {
    int tokenIndex = findTokenIndex(index);
    return parent.readValueAt(tokenIndex);
  }

  public JsonDocument readObject(int index) {
    int tokenIndex = findTokenIndex(index);
    return parent.readObjectAt(tokenIndex);
  }

  public JsonArray readArray(int index) {
    int tokenIndex = findTokenIndex(index);
    return parent.readArrayAt(tokenIndex + 1);
  }

  public int findTokenIndex(int arrayIndex) {
    int logicalIndex = 0;
    for (int index = start + 1; index < end; index++) {
      long token = parent.tokens[index];
      int tokenIndex = index;
      int type = (int) (token >>> 60);

      if (!Token.isValue(type)) {
        index = parent.findEndOfStructure(index);
      }
      if (logicalIndex++ == arrayIndex) {
        return tokenIndex;
      }
    }
    throw new ArrayIndexOutOfBoundsException(arrayIndex);
  }

  public int length() {
    if (length != -1) {
      return length;
    }
    int logicalIndex = 0;
    for (int index = start + 1; index < end; index++) {
      long token = parent.tokens[index];
      int type = (int) (token >>> 60);

      if (!Token.isValue(type)) {
        index = parent.findEndOfStructure(index);
      }
      logicalIndex++;
    }
    length = logicalIndex;
    return logicalIndex;
  }

  public void debugTokens() {
    for (int index = start; index < end; index++) {
      long token = parent.tokens[index];
      int type = (int) (token >>> 60);
      int length = (int) (token & 0x3FFFFFFFL);
      int start = (int) ((token >>> 30) & 0x3FFFFFFFL);
      if (token == -1) return;
      MemorySegment candidate = parent.segment.asSlice(start, length);
      System.out.println(index + "# Type: " + Token.toString(type) + " | Length: " + length + " | Value: " + StandardCharsets.UTF_8.decode(candidate.asByteBuffer()).toString());
    }
  }
}
