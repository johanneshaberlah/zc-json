package com.github.johanneshaberlah.zcjson;

import com.github.johanneshaberlah.zcjson.tokenizer.Token;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class JsonDocument implements AutoCloseable {
  final MemorySegment segment;
  final long[] tokens;

  private final Arena arena;
  private final int startToken;
  private int endToken;

  public JsonDocument(
    Arena arena,
    MemorySegment segment,
    long[] tokens,
    int startToken,
    int endToken
  ) {
    this.arena = arena;
    this.segment = segment;
    this.tokens = tokens;
    this.startToken = startToken;
    this.endToken = endToken;
  }

  public JsonValue readValue(String key) {
    return JsonValue.of(readValueSegment(JsonKey.of(key)));
  }

  public JsonValue readValue(JsonKey key) {
    return JsonValue.of(readValueSegment(key));
  }

  public MemorySegment readValueSegment(JsonKey key) {
    int tokenIndex = findTokenIndex(key);
    return readValueAt(tokenIndex + 1);
  }

  MemorySegment readValueAt(int tokenIndex) {
    long valueToken = tokens[tokenIndex];
    int valueStart = (int) ((valueToken >>> 30) & 0x3FFFFFFFL);
    int valueLength = (int) (valueToken & 0x3FFFFFFFL);
    return segment.asSlice(valueStart, valueLength);
  }

  public JsonDocument readObject(String key) {
    int startIndex = findTokenIndex(JsonKey.of(key));
    return readObjectAt(startIndex + 1);
  }

  public JsonDocument readObject(JsonKey key) {
    int startIndex = findTokenIndex(key);
    return readObjectAt(startIndex + 1);
  }

  JsonDocument readObjectAt(int startIndex) {
    int endIndex = findEndOfStructure(startIndex);
    return new JsonDocument(
      this.arena,
      this.segment,
      this.tokens,
      startIndex,
      endIndex
    );
  }

  public JsonArray readArray(String key) {
    int startIndex = findTokenIndex(JsonKey.of(key));
    return readArrayAt(startIndex + 1);
  }

  public JsonArray readArray(JsonKey key) {
    int startIndex = findTokenIndex(key);
    return readArrayAt(startIndex + 1);
  }

  JsonArray readArrayAt(int startIndex) {
    int endIndex = findEndOfStructure(startIndex);
    return new JsonArray(
      this,
      startIndex,
      endIndex
    );
  }

  int findTokenIndex(JsonKey key) {
    int depth = 0;
    for (int index = startToken; index < endToken; index++) {
      long token = tokens[index];
      if (token == -1) {
        endToken = index - 1;
        break;
      }
      int type = (int) (token >>> 60);
      int length = (int) (token & 0x3FFFFFFFL);
      if (type == Token.OBJECT_START_MARKER) {
        depth++;
        continue;
      }
      if (type == Token.OBJECT_END_MARKER) {
        depth--;
        continue;
      }
      if (depth != 1 || type != Token.KEY || key.length() != length) {
        continue;
      }
      int start = (int) ((token >>> 30) & 0x3FFFFFFFL);

      MemorySegment candidate = segment.asSlice(start, length);
      if (candidate.mismatch(key.segment()) == -1) {
        return index;
      }
    }
    throw new JsonKeyNotFoundException(key);
  }

  int findEndOfStructure(int startIndex) {
    int depth = 0;
    for (int index = startIndex; index < endToken; index++) {
      int type = (int) (tokens[index] >>> 60);
      if (type == Token.OBJECT_START_MARKER || type == Token.ARRAY_START_MARKER) {
        depth++;
      }
      if (type == Token.OBJECT_END_MARKER || type == Token.ARRAY_END_MARKER) {
        depth--;
      }
      if (depth != 0) {
        continue;
      }
      return index;
    }
    throw new IllegalStateException("Malformed JSON structure starting at token " + startIndex);
  }

  public void close() {
    arena.close();
  }
}
