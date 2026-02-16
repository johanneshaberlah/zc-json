package com.github.johanneshaberlah.zcjson.tokenizer.simd;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;

public class SimdByteSearch {
  private static final VectorSpecies<Byte> BYTE_SPECIES = ByteVector.SPECIES_PREFERRED;
  private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

  public long findByte(MemorySegment segment, long from, long length, byte value) {
    int step = BYTE_SPECIES.length();

    for (long index = from; index < length; index += step) {
      VectorMask<Byte> indexInRangeMask = BYTE_SPECIES.indexInRange(index, length);

      ByteVector inputVector = ByteVector.fromMemorySegment(BYTE_SPECIES, segment, index, NATIVE_ORDER,
        indexInRangeMask);
      VectorMask<Byte> matches = inputVector.compare(VectorOperators.EQ, value);
      VectorMask<Byte> validMatches = matches.and(indexInRangeMask);

      if (validMatches.anyTrue()) {
        return index + validMatches.firstTrue();
      }
    }
    return length;
  }

  long findFirstNonNumeric(MemorySegment segment, long from, long length) {
    int step = BYTE_SPECIES.length();

    for (long index = from; index < length; index += step) {
      VectorMask<Byte> indexInRangeMask = BYTE_SPECIES.indexInRange(index, length);

      ByteVector inputVector = ByteVector.fromMemorySegment(BYTE_SPECIES, segment, index, NATIVE_ORDER,
        indexInRangeMask);
      VectorMask<Byte> nonNumeric = isDigitOrExtra(inputVector)
        .not()
        .and(indexInRangeMask);

      if (nonNumeric.anyTrue()) {
        return index + nonNumeric.firstTrue();
      }
    }

    return length;
  }

  private static VectorMask<Byte> isDigitOrExtra(ByteVector input) {
    VectorMask<Byte> digits =
      input.compare(VectorOperators.GE, '0')
        .and(input.compare(VectorOperators.LE, '9'));

    VectorMask<Byte> extras =
      input.compare(VectorOperators.EQ, '+')
        .or(input.compare(VectorOperators.EQ, '-'))
        .or(input.compare(VectorOperators.EQ, 'e'))
        .or(input.compare(VectorOperators.EQ, 'E'))
        .or(input.compare(VectorOperators.EQ, '.'));

    return digits.or(extras);
  }
}
