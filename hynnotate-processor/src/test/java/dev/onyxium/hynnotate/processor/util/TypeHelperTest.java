package dev.onyxium.hynnotate.processor.util;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import org.junit.jupiter.api.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class TypeHelperTest {

    @Test
    void scans_known_hytale_codec_implementations_across_subpackages() {
        var captured = new ArrayList<String>();

        Compiler.javac()
                .withProcessors(new CapturingProcessor((env, roundEnv) ->
                        TypeHelper.getAllCodecImplementations(env)
                                .forEach(t -> captured.add(t.getQualifiedName().toString()))))
                .compile(JavaFileObjects.forSourceLines("test.Trigger",
                        "package test;",
                        "public class Trigger {}"));

        assertThat(captured).contains(
                "com.hypixel.hytale.codec.codecs.set.SetCodec",
                "com.hypixel.hytale.codec.codecs.map.MapCodec",
                "com.hypixel.hytale.codec.codecs.array.ArrayCodec",
                "com.hypixel.hytale.codec.codecs.simple.StringCodec");
        assertThat(captured).allSatisfy(fqn ->
                assertThat(fqn).startsWith("com.hypixel.hytale.codec.codecs."));

        captured.forEach(IO::println);
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
