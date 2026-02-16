package com.github.johanneshaberlah.zcjson.tokenizer;

public final class Token {
  public static final int OBJECT_START_MARKER = 0;
  public static final int OBJECT_END_MARKER = 1;
  public static final int ARRAY_START_MARKER = 2;
  public static final int ARRAY_END_MARKER = 3;
  public static final int KEY = 4;
  public static final int STRING = 5;
  public static final int NUMBER = 6;
  public static final int BOOLEAN_TRUE = 7;
  public static final int BOOLEAN_FALSE = 8;
  public static final int NULL = 9;

  public static boolean isValue(int token) {
    return token >= STRING;
  }

  public static String toString(int tokenType) {
    return switch (tokenType) {
      case Token.OBJECT_START_MARKER -> "OBJECT_START_MARKER";
      case Token.OBJECT_END_MARKER -> "OBJECT_END_MARKER";
      case Token.ARRAY_START_MARKER -> "ARRAY_START_MARKER";
      case Token.ARRAY_END_MARKER -> "ARRAY_END_MARKER";
      case Token.KEY -> "KEY";
      case Token.STRING -> "STRING";
      case Token.NUMBER -> "NUMBER";
      case Token.BOOLEAN_TRUE -> "BOOLEAN_TRUE";
      case Token.BOOLEAN_FALSE -> "BOOLEAN_FALSE";
      case Token.NULL -> "NULL";
      default -> "UNKNOWN_TOKEN(" + tokenType + ")";
    };
  }
}
