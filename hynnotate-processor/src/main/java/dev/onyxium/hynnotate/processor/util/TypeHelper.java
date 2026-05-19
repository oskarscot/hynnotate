package dev.onyxium.hynnotate.processor.util;

import com.palantir.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class TypeHelper {

    private TypeHelper() {}

    public static boolean hasEmptyConstructor(TypeElement type) {
        return type.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .map(ExecutableElement.class::cast)
                .anyMatch(constructor -> constructor.getParameters().isEmpty());
    }

    public static boolean hasAccessors(ProcessingEnvironment env, TypeElement type, List<VariableElement> fields) {
        var methods = publicInstanceMethods(env, type);
        for (var field : fields) {
            if (!hasGetter(env, field, methods) || !hasSetter(env, field, methods)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasGetter(ProcessingEnvironment env, VariableElement field, List<ExecutableElement> methods) {
        var pascal = StringHelper.toPascalCase(field.getSimpleName().toString());
        var fieldType = field.asType();
        var isBoolean = fieldType.getKind() == TypeKind.BOOLEAN;
        Types types = env.getTypeUtils();

        return methods.stream().anyMatch(m -> {
            var name = m.getSimpleName().toString();
            var nameOk = name.equals("get" + pascal)
                    || (isBoolean && name.equals("is" + pascal));
            return nameOk
                    && m.getParameters().isEmpty()
                    && types.isAssignable(m.getReturnType(), fieldType);
        });
    }

    public static boolean hasSetter(ProcessingEnvironment env, VariableElement field, List<ExecutableElement> methods) {
        var pascal = StringHelper.toPascalCase(field.getSimpleName().toString());
        var fieldType = field.asType();
        Types types = env.getTypeUtils();

        return methods.stream().anyMatch(m -> {
            var name = m.getSimpleName().toString();
            if (!name.equals("set" + pascal)) return false;
            if (m.getParameters().size() != 1) return false;
            if (m.getReturnType().getKind() != TypeKind.VOID) return false;
            TypeMirror paramType = m.getParameters().getFirst().asType();
            return types.isAssignable(fieldType, paramType);
        });
    }

    public static List<TypeElement> getAllCodecImplementations(ProcessingEnvironment env) {
        var basePackage = env.getElementUtils().getPackageElement("com.hypixel.hytale.codec.codecs");
        var arrayCodecPackage = env.getElementUtils().getPackageElement("com.hypixel.hytale.codec.codecs.array");
        var mapCodecPackage = env.getElementUtils().getPackageElement("com.hypixel.hytale.codec.codecs.map");
        var setCodecPackage = env.getElementUtils().getPackageElement("com.hypixel.hytale.codec.codecs.set");
        var simpleCodecPackage = env.getElementUtils().getPackageElement("com.hypixel.hytale.codec.codecs.simple");
        var codec = env.getElementUtils().getTypeElement("com.hypixel.hytale.codec.Codec");

        IO.println("Scanning all codec implementations...");

        return Stream.of(basePackage, arrayCodecPackage, mapCodecPackage, setCodecPackage, simpleCodecPackage)
                .filter(Objects::nonNull)
                .flatMap(pkg -> ElementFilter.typesIn(pkg.getEnclosedElements()).stream())
                .filter(type -> env.getTypeUtils().isAssignable(
                        type.asType(), env.getTypeUtils().erasure(codec.asType())
                ))
                .toList();
    }

    private static List<ExecutableElement> publicInstanceMethods(ProcessingEnvironment env, TypeElement type) {
        return ElementFilter.methodsIn(env.getElementUtils().getAllMembers(type)).stream()
                .filter(m -> m.getModifiers().contains(Modifier.PUBLIC))
                .filter(m -> !m.getModifiers().contains(Modifier.STATIC))
                .toList();
    }
}
