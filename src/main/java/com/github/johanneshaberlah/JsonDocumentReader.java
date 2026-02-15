package com.github.johanneshaberlah;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class JsonDocumentReader {
  private final Tokenizer tokenizer;

  private JsonDocumentReader(Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  public JsonDocument read(Path path) throws IOException {
    Arena arena = Arena.ofConfined();

    try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
      MemorySegment segment = channel.map(
        FileChannel.MapMode.READ_ONLY,
        0,
        channel.size(),
        arena
      );

      long[] tokens = tokenizer.read(segment);
      return new JsonDocument(arena, segment, tokens, 0, tokens.length);
    } catch (Exception e) {
      arena.close();
      throw e;
    }
  }

  public JsonDocument read(MemorySegment segment) {
    Arena arena = Arena.ofConfined();
    long[] tokens = tokenizer.read(segment);
    return new JsonDocument(arena, segment, tokens, 0, tokens.length);
  }

  public static JsonDocumentReader withTokenizer(Tokenizer tokenizer) {
    return new JsonDocumentReader(tokenizer);
  }

  public static JsonDocumentReader defaultTokenizer() {
    return new JsonDocumentReader(new Tokenizer());
  }
}
