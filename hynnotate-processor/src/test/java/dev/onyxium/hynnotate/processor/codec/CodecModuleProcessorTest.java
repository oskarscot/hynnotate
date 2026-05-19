package dev.onyxium.hynnotate.processor.codec;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import dev.onyxium.hynnotate.processor.HynnotateProcessor;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static org.assertj.core.api.Assertions.assertThat;

class CodecModuleProcessorTest {

    @Test
    void generates_codec_class_for_simple_includecodec_type() {
        var source = JavaFileObjects.forSourceLines("sample.Reward",
                "package sample;",
                "import dev.onyxium.hynnotate.annotations.IncludeCodec;",
                "import dev.onyxium.hynnotate.annotations.CodecField;",
                "",
                "@IncludeCodec",
                "public class Reward {",
                "    @CodecField private String name;",
                "    @CodecField private Integer amount;",
                "",
                "    public Reward() {}",
                "    public String getName() { return name; }",
                "    public void setName(String value) { this.name = value; }",
                "    public Integer getAmount() { return amount; }",
                "    public void setAmount(Integer value) { this.amount = value; }",
                "}");

        var compilation = compile(source);

        assertThat(compilation.errors()).isEmpty();
        var generated = findGeneratedSource(compilation, "sample.RewardCodec");
        assertThat(generated).contains("class RewardCodec");
        assertThat(generated).contains("CODEC");
        assertThat(generated).contains("Codec.STRING");
        assertThat(generated).contains("Codec.INTEGER");
        assertThat(generated).contains("Reward::setName");
        assertThat(generated).contains("Reward::getName");
        assertThat(generated).contains("Reward::setAmount");
        assertThat(generated).contains("Reward::getAmount");
    }

    @Test
    void respects_explicit_key_on_codecfield_value() {
        var source = JavaFileObjects.forSourceLines("sample.Reward",
                "package sample;",
                "import dev.onyxium.hynnotate.annotations.IncludeCodec;",
                "import dev.onyxium.hynnotate.annotations.CodecField;",
                "",
                "@IncludeCodec",
                "public class Reward {",
                "    @CodecField(\"DisplayName\") private String name;",
                "",
                "    public Reward() {}",
                "    public String getName() { return name; }",
                "    public void setName(String value) { this.name = value; }",
                "}");

        var compilation = compile(source);

        assertThat(compilation.errors()).isEmpty();
        var generated = findGeneratedSource(compilation, "sample.RewardCodec");
        assertThat(generated).contains("\"DisplayName\"");
    }

    @Test
    void inlines_codecwith_expression_verbatim() {
        var source = JavaFileObjects.forSourceLines("sample.Reward",
                "package sample;",
                "import dev.onyxium.hynnotate.annotations.IncludeCodec;",
                "import dev.onyxium.hynnotate.annotations.CodecField;",
                "import dev.onyxium.hynnotate.annotations.CodecWith;",
                "",
                "@IncludeCodec",
                "public class Reward {",
                "    @CodecField @CodecWith(\"com.hypixel.hytale.codec.Codec.STRING\") private String opaque;",
                "",
                "    public Reward() {}",
                "    public String getOpaque() { return opaque; }",
                "    public void setOpaque(String value) { this.opaque = value; }",
                "}");

        var compilation = compile(source);

        assertThat(compilation.errors()).isEmpty();
        var generated = findGeneratedSource(compilation, "sample.RewardCodec");
        assertThat(generated).contains("com.hypixel.hytale.codec.Codec.STRING");
    }

    @Test
    void reports_error_when_class_lacks_an_empty_constructor() {
        var source = JavaFileObjects.forSourceLines("sample.Reward",
                "package sample;",
                "import dev.onyxium.hynnotate.annotations.IncludeCodec;",
                "import dev.onyxium.hynnotate.annotations.CodecField;",
                "",
                "@IncludeCodec",
                "public class Reward {",
                "    @CodecField private String name;",
                "",
                "    public Reward(String name) { this.name = name; }",
                "    public String getName() { return name; }",
                "    public void setName(String value) { this.name = value; }",
                "}");

        var compilation = compile(source);

        assertThat(compilation.errors())
                .extracting(d -> d.getMessage(null))
                .anyMatch(msg -> msg.contains("does not declare an empty constructor"));
    }

    @Test
    void reports_error_when_field_lacks_getter_or_setter() {
        var source = JavaFileObjects.forSourceLines("sample.Reward",
                "package sample;",
                "import dev.onyxium.hynnotate.annotations.IncludeCodec;",
                "import dev.onyxium.hynnotate.annotations.CodecField;",
                "",
                "@IncludeCodec",
                "public class Reward {",
                "    @CodecField private String name;",
                "",
                "    public Reward() {}",
                "    public String getName() { return name; }",
                "    // no setter",
                "}");

        var compilation = compile(source);

        assertThat(compilation.errors())
                .extracting(d -> d.getMessage(null))
                .anyMatch(msg -> msg.contains("does not declare a valid getter or setter"));
    }

    @Test
    void reports_error_when_field_type_has_no_codec_and_no_codecwith() {
        var source = JavaFileObjects.forSourceLines("sample.Reward",
                "package sample;",
                "import dev.onyxium.hynnotate.annotations.IncludeCodec;",
                "import dev.onyxium.hynnotate.annotations.CodecField;",
                "",
                "@IncludeCodec",
                "public class Reward {",
                "    @CodecField private sample.Custom field;",
                "",
                "    public Reward() {}",
                "    public sample.Custom getField() { return field; }",
                "    public void setField(sample.Custom value) { this.field = value; }",
                "}");
        var custom = JavaFileObjects.forSourceLines("sample.Custom",
                "package sample;",
                "public class Custom {}");

        var compilation = Compiler.javac()
                .withProcessors(new HynnotateProcessor())
                .compile(source, custom);

        assertThat(compilation.errors())
                .extracting(d -> d.getMessage(null))
                .anyMatch(msg -> msg.contains("No codec available"));
    }

    private static Compilation compile(JavaFileObject source) {
        return Compiler.javac()
                .withProcessors(new HynnotateProcessor())
                .compile(source);
    }

    private static String findGeneratedSource(Compilation compilation, String qualifiedName) {
        return compilation.generatedSourceFiles().stream()
                .filter(f -> f.getName().endsWith(qualifiedName.replace('.', '/') + ".java"))
                .findFirst()
                .map(f -> {
                    try {
                        return f.getCharContent(true).toString();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new AssertionError("No generated source for " + qualifiedName
                        + "; found: " + compilation.generatedSourceFiles().stream().map(JavaFileObject::getName).toList()));
    }
}
