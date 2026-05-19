package dev.onyxium.hynnotate.processor.codec;

import com.palantir.javapoet.*;
import dev.onyxium.hynnotate.annotations.CodecField;
import dev.onyxium.hynnotate.annotations.CodecWith;
import dev.onyxium.hynnotate.processor.ModuleProcessor;
import dev.onyxium.hynnotate.processor.util.StringHelper;
import dev.onyxium.hynnotate.processor.util.TypeHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;

public class CodecModuleProcessor implements ModuleProcessor {

    private final ProcessingEnvironment env;
    private final CodecResolver resolver;
    private final ClassName builderCodec = ClassName.get("com.hypixel.hytale.codec.builder", "BuilderCodec");
    private final ClassName keyedCodec = ClassName.get("com.hypixel.hytale.codec", "KeyedCodec");

    public CodecModuleProcessor(ProcessingEnvironment env) {
        this.env = env;
        this.resolver = new CodecResolver(env);
    }

    @Override
    public void generate(TypeElement type) {
        var packageElement = env.getElementUtils().getPackageOf(type);
        var typeName = ClassName.get(type);

        if(!TypeHelper.hasEmptyConstructor(type)) {
            env.getMessager().printError("Class " + type.getSimpleName() + " does not declare an empty constructor!");
            return;
        }

        var codecFields = gatherAllEligibleElements(type).stream()
                .map(VariableElement.class::cast)
                .toList();

        if(!TypeHelper.hasAccessors(env, type, codecFields)) {
            env.getMessager().printError("Class " + type.getSimpleName() + " does not declare a valid getter or setter method!");
            return;
        }

        var builderCodecWithType = ParameterizedTypeName.get(builderCodec, typeName);
        var codec = FieldSpec.builder(builderCodecWithType, "CODEC", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(buildCodec(type))
                .build();

        var clazz = TypeSpec.classBuilder(type.getSimpleName() + "Codec")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(codec)
                .build();

        var build = JavaFile.builder(packageElement.getQualifiedName().toString(), clazz)
                .indent("    ")
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
            var field = (VariableElement) element;
            var codecField = field.getAnnotation(CodecField.class);
            if(codecField == null) continue;

            var fieldName = codecField.value().isBlank() ? StringHelper.toPascalCase(field.toString()) : codecField.value();
            var finalCodec = field.getAnnotation(CodecWith.class);

            CodeBlock codecExpression;
            if (finalCodec != null) {
                codecExpression = CodeBlock.of("$L", finalCodec.value());
            } else {
                codecExpression = resolver.resolve(field);
                if (codecExpression == null) continue;
            }

            codeBlock.add(buildKeyedCodec(type, fieldName, codecExpression, field.toString()));
        }

        codeBlock.add(".build()");
        return codeBlock.build();
    }

    // TODO: ugly af
    private CodeBlock buildKeyedCodec(TypeElement type, String codecKey, CodeBlock codecExpression, String fieldName) {
        return CodeBlock.builder()
                .add(".append(new $T<>($S, $L), $T::set$L, $T::get$L).add()\n",
                        keyedCodec,
                        codecKey,
                        codecExpression,
                        type,
                        StringHelper.toPascalCase(fieldName),
                        type,
                        StringHelper.toPascalCase(fieldName)
                )
                .build();
    }
}
