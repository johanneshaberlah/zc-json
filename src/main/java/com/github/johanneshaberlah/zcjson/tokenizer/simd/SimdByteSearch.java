package com.github.johanneshaberlah.zcjson.tokenizer.simd;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.function.Function;

public class SimdByteSearch {
  private static final VectorSpecies<Byte> BYTE_SPECIES = ByteVector.SPECIES_PREFERRED;
  private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

  public long findByte(MemorySegment segment, long from, long length, byte value) {
    return findFirstMatch(segment, from, length, mask -> mask.compare(VectorOperators.EQ, value));
  }

  public long findFirstNonNumeric(MemorySegment segment, long from, long length) {
    return findFirstMatch(segment, from, length, mask ->
      mask.compare(VectorOperators.GE, '0')
        .and(mask.compare(VectorOperators.LE, '9'))
        .or(mask.compare(VectorOperators.EQ, '+'))
        .or(mask.compare(VectorOperators.EQ, '-'))
        .or(mask.compare(VectorOperators.EQ, '.'))
        .or(mask.lanewise(VectorOperators.OR, 0x20).compare(VectorOperators.EQ, 'e')) // ignore case of 'e'
        .not()
    );
  }

  private long findFirstMatch(
    MemorySegment segment,
    long from,
    long length,
    Function<ByteVector, VectorMask<Byte>> mask
  ) {
    int step = BYTE_SPECIES.length();

    for (long index = from; index < length; index += step) {
      VectorMask<Byte> inRange = BYTE_SPECIES.indexInRange(index, length);
      ByteVector inputVector = ByteVector.fromMemorySegment(BYTE_SPECIES, segment, index, NATIVE_ORDER, inRange);

      VectorMask<Byte> matches = mask.apply(inputVector).and(inRange);

      if (matches.anyTrue()) {
        return index + matches.firstTrue();
      }
    }
    return length;
  }
}
