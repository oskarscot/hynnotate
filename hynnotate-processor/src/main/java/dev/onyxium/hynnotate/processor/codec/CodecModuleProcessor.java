package dev.onyxium.hynnotate.processor.codec;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import dev.onyxium.hynnotate.processor.ModuleProcessor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

public class CodecModuleProcessor implements ModuleProcessor {

    private final ProcessingEnvironment env;

    public CodecModuleProcessor(ProcessingEnvironment env) {
        this.env = env;
    }

    @Override
    public void generate(TypeElement type) {
        var hello = TypeSpec.classBuilder(type.getSimpleName() + "_Codec")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();

        var build = JavaFile.builder("dev.onyxium.hynnotate.example", hello).build();
        try {
            build.writeTo(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
