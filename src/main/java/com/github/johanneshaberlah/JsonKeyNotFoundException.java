package com.github.johanneshaberlah;

public final class JsonKeyNotFoundException extends RuntimeException {

  public JsonKeyNotFoundException(JsonKey key) {
    super(String.format("The field '%s' was not found", key.name()));
  }
}
