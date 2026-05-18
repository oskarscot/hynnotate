package dev.onyxium.hynnotate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a field for inclusion in the generated codec.
///
/// Only fields annotated with `@CodecField` appear in the codec generated
/// for the enclosing [IncludeCodec] class. Fields without this
/// annotation are ignored. The codec used for the field is inferred from
/// the field's declared Java type, but you can override the inference with
/// [CodecWith] when needed.
///
/// ### Key naming
/// The serialized key defaults to the field name converted to PascalCase
/// (e.g. `playerName` -> `PlayerName`). Set [#value()] to choose an
/// explicit key.
///
/// ### Example
/// ```java
/// @CodecField    // key becomes "Amount" (derived from field name)
/// private Integer amount;
///
/// @CodecField("Expires")  // key becomes "Expires" (explicit override)
/// private Instant expiresAt;
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface CodecField {

    /// The serialized key for this field in the generated codec.
    ///
    /// Defaults to the field name converted to PascalCase.
    String value() default "";
}
