# zc-json

Blazingly fast zero-copy JSON access for the JVM - no heap allocations, powered by JDK 23's
[Project Panama](https://openjdk.org/projects/panama/).

> [!WARNING]
> This library is experimental and not production-ready. The API is subject to breaking changes.

## Why zc-json

Memory allocations are a major bottleneck in JSON deserialization - especially with sparse access patterns, where you
often read only a small subset of fields. In those cases, only paying for what you actually access can be a huge win.

Most JSON libraries allocate aggressively: Strings, boxed numbers, node/object trees, and intermediate buffers.
zc-json keeps the original JSON bytes off-heap and provides byte-accurate access to values without copying, so you can
navigate and parse on demand with minimal overhead.

## Features

- **Zero heap allocations** during deserialization - data stays in off-heap memory
- **Direct byte-level access** through the `MemorySegment` API
- **Compact token encoding** - position, length, and type packed into a single `long`, no object overhead
- **Memory-mapped file I/O** via `FileChannel.map()` for efficient large-file access
- **Easy-to-use abstraction** for ergonomic typed access (`asString()`, `asInteger()`, ...`)
- **Nested object and array navigation** with a fluent API

## Quick start
```java
JsonDocumentReader reader = JsonDocumentReader.defaultTokenizer();
try (JsonDocument document = reader.read(file)) {
    String name = document.readValue("name").asString();
    int age = document.readValue("age").asInteger();
}
```

## Zero-copy access

For real zero-copy access, read JSON values as raw `MemorySegment` slices - no allocations required:

```java
JsonKey NAME = JsonKey.of("name");
JsonKey UNIVERSITY = JsonKey.of("university");

JsonDocumentReader reader = JsonDocumentReader.defaultTokenizer();
try (JsonDocument document = reader.read(file)) {
    MemorySegment nameSegment = document.readValueSegment(NAME);
    MemorySegment universitySegment = document.readValueSegment(UNIVERSITY);
    // Work directly on memory segments without allocating Strings
    // (e.g., compare via MemorySegment.mismatch(...) or custom UTF-8 utilities)
}
```

## Easy-to-use abstraction

For everyday use, `JsonValue` provides typed accessors that handle parsing directly from the underlying memory region:

```java
JsonDocumentReader reader = JsonDocumentReader.defaultTokenizer();
try (JsonDocument document = reader.read(file)) {
    String name = document.readValue("name").asString();

    // Nested objects
    var university = document.readObject("university");
    String universityName = university.readValue("name").asString();

    // Arrays with mixed types
    int age = document.readValue("age").asString();
    double height = document.readValue("height").asDouble();
}
```

## Installation

Requires **JDK 23+**.

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    implementation("com.github.johanneshaberlah:zc-json:0.1")
}
```

**Gradle (Groovy)**

```groovy
dependencies {
    implementation 'com.github.johanneshaberlah:zc-json:0.1'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.github.johanneshaberlah</groupId>
    <artifactId>zc-json</artifactId>
    <version>0.1</version>
</dependency>
```
