# Hynnotate

Generate Hytale codecs from annotations instead of writing them by hand. Mark a class `@IncludeCodec`, mark its fields `@CodecField`, and the processor writes the `BuilderCodec` at compile time. No reflection at runtime.

## Why

Hand-written, a class with a codec usually looks like this:

```java
// hand-written
public class Reward {

    // rest of the class...

    public Reward() { } // no-args constructor is required (we could also generate it if missing)

    public static final BuilderCodec<Reward> CODEC = BuilderCodec
            .builder(Reward.class, Reward::new)
            .append(new KeyedCodec<>("Name", Codec.STRING),
                    (o, v) -> o.name = v, o -> o.name)
                .add()
            .append(new KeyedCodec<>("Amount", Codec.INTEGER),
                    (o, v) -> o.amount = v, o -> o.amount)
                .add()
            .build();
}
```

Every field is another `.append(...).add()` chain: a `KeyedCodec` wrapper, a setter lambda, a getter lambda. Multiply that across every config and DTO in a plugin and most of your codec code is mechanical.

The annotated version:

```java
@IncludeCodec
public class Reward {

    @CodecField
    private String name;

    @CodecField("Amount")
    private int amount;

    public Reward() {}
}
```

The processor generates `Reward_Codec.java` at compile time. You reference `Reward_Codec.CODEC` like you would have written by hand.

## Status

- [in progress] Processor scaffolding: discovers `@IncludeCodec` classes via `javac`
- [in progress] Codec generation: emitting the real `BuilderCodec` initializer
- [planned] Validation annotations
- [planned] Documentation annotations
- [planned] Annotations beyond codecs (commands, listeners, ECS components)

## Modules

| Module | Description |
|---|---|
| `hynnotate-annotations` | The annotations plugin authors put on their code |
| `hynnotate-processor` | The `javac` processor that reads them and generates source |
| `hynnotate-example` | A Hytale plugin used to test the pipeline end to end |

## Usage

```kotlin
dependencies {
    //TODO: maven repo
}
```

// TODO 

## Annotation reference

### `@IncludeCodec`

Goes on a class. The processor generates `<ClassName>_Codec.java` in the same package with a static `CODEC` field of type `BuilderCodec<ClassName>`.

The class needs a no-arg constructor, since BuilderCodec calls it during decoding. Fields without `@CodecField` are ignored.

Codec resolution for each field, in order:
1. `@CodecWith` expression on the field.
2. Built-in mapping (`int` -> `Codec.INTEGER`, `String` -> `Codec.STRING`, `Instant` -> `Codec.INSTANT`, etc.).
3. `<Type>_Codec.CODEC` if the field's type also has `@IncludeCodec`.
4. `<Type>.CODEC` if the field's type has a public static `CODEC`.
5. Compile error.

### `@CodecField`

Marks a field for inclusion. The key defaults to PascalCase of the field name (`playerName` -> `"PlayerName"`). Pass a string to override:

```java
@CodecField              // key: "Name"
private String name;

@CodecField("Expires")   // key: "Expires"
private Instant expiresAt;
```

### `@CodecWith`

Overrides codec inference. The value is a Java expression copied into the generated file as-is. `javac` validates it when it compiles the generated source, so typos surface as normal compile errors against the generated file.

```java
@CodecField
@CodecWith("Codec.STRING")
private BigDecimal price;

@CodecField
@CodecWith("new EnumCodec<>(Rarity.class)")
private Rarity rarity;
```

Use it when the type has more than one codec to pick from, when the codec lives on a different class, or when the codec needs constructor arguments.

## Building

```
./gradlew build
```

Run the example plugin against a local Hytale dev server:

```
./gradlew :hynnotate-example:runServer
```

Requires Java 25 (set via the Gradle toolchain).

## Roadmap

Not promises, just direction.

**Validation annotations.** BuilderCodec's `FieldBuilder` exposes `.addValidator(...)` today. The plan is to wire annotations like `@Min(0)` or `@NotBlank` into those calls at generation time, inspired by spring validation, so authors stop writing validators by hand after the codec runs.

**Documentation annotations.** Same idea for `.documentation(...)`. Annotations on the source class become doc strings on the generated codec, which feed into Hytale's existing schema output.

**Beyond codecs.** Commands, event listeners, ECS components, and asset declarations all carry similar boilerplate. None are committed to. They're the obvious next places to look.
