package dev.onyxium.hynnotate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a class for codec generation.
///
/// The annotation processor generates a `<ClassName>_Codec` class with a
/// static `CODEC` field that serializes the annotated class using
/// Hytale's `BuilderCodec`. Fields to include must be annotated with
/// [CodecField].
///
/// ### How the codec for each field is picked
/// For each [CodecField], the processor tries these in order:
/// 1. The [CodecWith] expression on the field, if present.
/// 2. The built-in mapping for standard types (`int` -> `Codec.INTEGER`,
///    `String` -> `Codec.STRING`, `Instant` -> `Codec.INSTANT`, etc.).
/// 3. `<Type>_Codec.CODEC` if the field's type also has `@IncludeCodec`.
/// 4. `<Type>.CODEC` if the field's type has a public static `CODEC`.
/// 5. Otherwise compilation fails with an error.
///
/// ### Requirements
/// The class must have a no-argument constructor. `BuilderCodec` needs
/// one to create new instances when deserializing.
///
/// ### Example
/// ```java
/// @IncludeCodec
/// public class Reward {
///     @CodecField private String name;
///     @CodecField private Integer amount;
/// }
/// ```
/// Generates `Reward_Codec.CODEC` with keys `Name` and `Amount`.
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface IncludeCodec {
}
