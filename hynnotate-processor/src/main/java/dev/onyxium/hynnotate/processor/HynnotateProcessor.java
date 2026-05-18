package dev.onyxium.hynnotate.processor;

import dev.onyxium.hynnotate.annotations.IncludeCodec;
import dev.onyxium.hynnotate.processor.codec.CodecModuleProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes({
        "dev.onyxium.hynnotate.annotations.CodecField",
        "dev.onyxium.hynnotate.annotations.CodecWith",
        "dev.onyxium.hynnotate.annotations.IncludeCodec"
})
public class HynnotateProcessor extends AbstractProcessor {

    private Map<Class<? extends Annotation>, ModuleProcessor> modules;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        modules = Map.of(
                IncludeCodec.class, new CodecModuleProcessor(env)
        );
        env.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "Hynnotate processor initialized");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        modules.forEach((annotationClass, module) -> {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotationClass)) {
                if (element instanceof TypeElement type) {
                    module.generate(type);
                }
            }
        });
        return true;
    }
}
