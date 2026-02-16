package com.github.johanneshaberlah.zcjson.tokenizer;

import java.lang.foreign.MemorySegment;

public interface Tokenizer {

  public long[] read(MemorySegment segment);

}
