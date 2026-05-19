package dev.onyxium.hynnotate.processor.codec;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CodecResolver {

    private static final String CODEC_INTERFACE = "com.hypixel.hytale.codec.Codec";

    private final ProcessingEnvironment env;
    private Map<String, FieldRef> codecConstants;
    private Map<String, List<String>> ambiguousConstants;

    public CodecResolver(ProcessingEnvironment env) {
        this.env = env;
    }

    public CodeBlock resolve(VariableElement field) {
        init();
        var key = keyFor(field.asType());
        var ref = codecConstants.get(key);
        if (ref != null) {
            return CodeBlock.of("$T.$L", ref.owner(), ref.name());
        }
        if (ambiguousConstants.containsKey(key)) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Multiple codecs available for " + key + " ("
                            + String.join(", ", ambiguousConstants.get(key))
                            + "); use @CodecWith to specify which one.",
                    field);
            return null;
        }
        env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "No codec available for type " + key + "; use @CodecWith to specify one.",
                field);
        return null;
    }

    private void init() {
        if (codecConstants != null) return;

        var elements = env.getElementUtils();
        var types = env.getTypeUtils();
        var codecInterface = elements.getTypeElement(CODEC_INTERFACE);

        if (codecInterface == null) {
            codecConstants = Map.of();
            ambiguousConstants = Map.of();
            return;
        }

        var codecRaw = types.erasure(codecInterface.asType());
        var owner = ClassName.get(codecInterface);

        Map<String, List<VariableElement>> byEncodedType = new LinkedHashMap<>();
        for (var field : ElementFilter.fieldsIn(codecInterface.getEnclosedElements())) {
            if (!field.getModifiers().contains(Modifier.STATIC)) continue;
            if (field.getAnnotation(Deprecated.class) != null) continue;

            TypeMirror encoded = findCodecTypeArg(field.asType(), types, codecRaw);
            if (encoded == null) continue;

            byEncodedType.computeIfAbsent(keyFor(encoded), _ -> new ArrayList<>()).add(field);
        }

        codecConstants = new HashMap<>();
        ambiguousConstants = new HashMap<>();
        for (var entry : byEncodedType.entrySet()) {
            var candidates = entry.getValue();
            if (candidates.size() == 1) {
                codecConstants.put(entry.getKey(),
                        new FieldRef(owner, candidates.getFirst().getSimpleName().toString()));
            } else {
                ambiguousConstants.put(entry.getKey(),
                        candidates.stream().map(c -> c.getSimpleName().toString()).toList());
            }
        }
    }

    private TypeMirror findCodecTypeArg(TypeMirror type, Types types, TypeMirror codecRaw) {
        if (!(type instanceof DeclaredType declared)) return null;
        if (types.isSameType(types.erasure(declared), codecRaw)) {
            return declared.getTypeArguments().isEmpty() ? null : declared.getTypeArguments().getFirst();
        }
        for (var supertype : types.directSupertypes(type)) {
            var found = findCodecTypeArg(supertype, types, codecRaw);
            if (found != null) return found;
        }
        return null;
    }

    private String keyFor(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            type = env.getTypeUtils().boxedClass((PrimitiveType) type).asType();
        }
        return type.toString();
    }

    private record FieldRef(ClassName owner, String name) {}
}
