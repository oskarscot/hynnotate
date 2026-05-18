package dev.onyxium.hynnotate.processor.codec;

import com.palantir.javapoet.*;
import dev.onyxium.hynnotate.annotations.CodecField;
import dev.onyxium.hynnotate.annotations.CodecWith;
import dev.onyxium.hynnotate.processor.ModuleProcessor;
import dev.onyxium.hynnotate.processor.StringHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;

public class CodecModuleProcessor implements ModuleProcessor {

    private final ProcessingEnvironment env;
    private final ClassName builderCodec = ClassName.get("com.hypixel.hytale.codec.builder", "BuilderCodec");
    private final ClassName keyedCodec = ClassName.get("com.hypixel.hytale.codec", "KeyedCodec");

    public CodecModuleProcessor(ProcessingEnvironment env) {
        this.env = env;
    }

    @Override
    public void generate(TypeElement type) {
        // TODO: check whether no-arg constructor exists
        // TODO: check whether public getters and setters exist
        var packageElement = env.getElementUtils().getPackageOf(type);
        var typeName = ClassName.get(type);
        var builderCodecWithType = ParameterizedTypeName.get(builderCodec, typeName);
        var codec = FieldSpec.builder(builderCodecWithType, "CODEC", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(buildCodec(type))
                .build();

        var clazz = TypeSpec.classBuilder(type.getSimpleName() + "_Codec")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(codec)
                .build();

        var build = JavaFile.builder(packageElement.getQualifiedName().toString(), clazz)
                .build();

        env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated CODEC for " + type);
        try {
            build.writeTo(env.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<? extends Element> gatherAllEligibleElements(TypeElement type) {
        return type.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.FIELD)
                .filter(element -> element.getAnnotation(CodecField.class) != null)
                .toList();
    }

    private CodeBlock buildCodec(TypeElement type) {
        var elements = gatherAllEligibleElements(type);
        var codeBlock = CodeBlock.builder()
                .add("$T.builder($T.class, $T::new)\n", builderCodec, type, type);

        for (var element : elements) {
            var declaredType = env.getElementUtils().getTypeElement(element.asType().toString());
            var codecField = element.getAnnotation(CodecField.class);
            if(codecField == null) continue;

            var fieldName = codecField.value().isBlank() ? StringHelper.toPascalCase(element.toString()) : codecField.value();
            var finalCodec = element.getAnnotation(CodecWith.class);
            // TODO: choose appropriate codec
            codeBlock.add(buildKeyedCodec(type, fieldName, declaredType.getSimpleName().toString().toUpperCase(), element.toString()));
        }

        codeBlock.add(".build()");
        return codeBlock.build();
    }

    private CodeBlock buildKeyedCodec(TypeElement type, String codecKey, String codecName, String fieldName) {
        var keyedCodecBuilder = CodeBlock.builder()
                .add(".append(new $T<>($S, $T.$L), $T::set$L, $T::get$L).add()\n",
                        keyedCodec,
                        codecKey,
                        builderCodec,
                        codecName,
                        type,
                        StringHelper.toPascalCase(fieldName),
                        type,
                        StringHelper.toPascalCase(fieldName)
                );
        return keyedCodecBuilder.build();
    }
}
