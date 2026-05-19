package dev.onyxium.hynnotate.processor.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.List;

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

    private static List<ExecutableElement> publicInstanceMethods(ProcessingEnvironment env, TypeElement type) {
        return ElementFilter.methodsIn(env.getElementUtils().getAllMembers(type)).stream()
                .filter(m -> m.getModifiers().contains(Modifier.PUBLIC))
                .filter(m -> !m.getModifiers().contains(Modifier.STATIC))
                .toList();
    }
}
