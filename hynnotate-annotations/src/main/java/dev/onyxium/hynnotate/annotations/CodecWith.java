package dev.onyxium.hynnotate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Tells the processor which codec to use for a field, instead of
/// picking one from the field's type.
///
/// The [#value()] is a Java expression that gets copied straight into
/// the generated code. It must return a codec that works for the field's
/// type. The processor itself does not check the expression, javac
/// does when it compiles the generated file. A typo shows up as a
/// normal compile error on the generated source. The provided codec HAS to
/// be a full FQN expression (temporary).
///
/// ### When to use
/// - The codec lives on a different class than the field's type
///   (e.g. `Codecs.COOL_TYPE` rather than `CoolType.CODEC`).
/// - The type has more than one valid codec and you need to pick one
///   (e.g. `BigDecimal` as `Codec.STRING` for JSON, or
///   `MyCodec.BIG_DECIMAL_CODEC` for BSON).
/// - The codec needs to be built with arguments
///   (e.g. `new EnumCodec<>(Rarity.class)`).
///
/// Without this annotation, the processor picks the codec itself —
/// see [IncludeCodec] for the rules it follows.
///
/// ### Example
/// ```java
/// @CodecField
/// @CodecWith("Codec.STRING")
/// private BigDecimal price;
///
/// @CodecField
/// @CodecWith("new EnumCodec<>(Rarity.class)")
/// private Rarity rarity;
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface CodecWith {

    /// Java expression for the codec. Copied straight into the generated
    /// code as-is.
    ///
    /// Examples:
    /// - `"Codec.STRING"`
    /// - `"MyCodec.COOL_TYPE"`
    /// - `"new EnumCodec<>(MyEnum.class)"`
    String value();
}
