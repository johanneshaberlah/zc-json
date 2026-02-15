package com.github.johanneshaberlah;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

public record JsonKey(String name, MemorySegment segment, int length) {

  public static JsonKey of(String key) {
    byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
    return new JsonKey(key, MemorySegment.ofArray(bytes), bytes.length);
  }
}
