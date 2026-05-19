package dev.onyxium.hynnotate.processor.codec;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import com.palantir.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class CodecResolverTest {

    @Test
    void resolves_String_field_to_Codec_STRING() {
        var resolved = resolveFieldsIn(
                "test.Sample",
                "package test;",
                "public class Sample {",
                "    String name;",
                "}");

        assertThat(resolved).containsKey("name");
        assertThat(resolved.get("name").toString()).contains("STRING");
    }

    @Test
    void resolves_primitive_int_via_boxing_to_Codec_INTEGER() {
        var resolved = resolveFieldsIn(
                "test.Sample",
                "package test;",
                "public class Sample {",
                "    int amount;",
                "}");

        assertThat(resolved).containsKey("amount");
        assertThat(resolved.get("amount").toString()).contains("INTEGER");
    }

    @Test
    void reports_error_when_no_codec_is_available_for_field_type() {
        var captured = new LinkedHashMap<String, CodeBlock>();
        var compilation = compile(
                (env, roundEnv) -> captureResolutions(env, roundEnv, captured),
                JavaFileObjects.forSourceLines("test.Sample",
                        "package test;",
                        "public class Sample {",
                        "    test.Custom field;",
                        "}"),
                JavaFileObjects.forSourceLines("test.Custom",
                        "package test;",
                        "public class Custom {}"));

        assertThat(captured).containsEntry("field", null);
        assertThat(compilation.errors())
                .extracting(d -> d.getMessage(null))
                .anyMatch(msg -> msg.contains("No codec available"));
    }

    private Map<String, CodeBlock> resolveFieldsIn(String typeName, String... source) {
        var captured = new LinkedHashMap<String, CodeBlock>();
        compile(
                (env, roundEnv) -> captureResolutions(env, roundEnv, captured),
                JavaFileObjects.forSourceLines(typeName, source));
        return captured;
    }

    private static Compilation compile(
            BiConsumer<ProcessingEnvironment, RoundEnvironment> callback,
            javax.tools.JavaFileObject... sources) {
        return Compiler.javac()
                .withProcessors(new CapturingProcessor(callback))
                .compile(sources);
    }

    private static void captureResolutions(
            ProcessingEnvironment env,
            RoundEnvironment roundEnv,
            Map<String, CodeBlock> captured) {
        var resolver = new CodecResolver(env);
        for (var rootElement : roundEnv.getRootElements()) {
            if (rootElement instanceof TypeElement type) {
                for (var field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
                    captured.put(field.getSimpleName().toString(), resolver.resolve(field));
                }
            }
        }
    }

    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_25)
    private static final class CapturingProcessor extends AbstractProcessor {
        private final BiConsumer<ProcessingEnvironment, RoundEnvironment> callback;
        private boolean ran;

        CapturingProcessor(BiConsumer<ProcessingEnvironment, RoundEnvironment> callback) {
            this.callback = callback;
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            if (!ran && !roundEnv.processingOver() && !roundEnv.getRootElements().isEmpty()) {
                ran = true;
                callback.accept(processingEnv, roundEnv);
            }
            return false;
        }
    }
}
