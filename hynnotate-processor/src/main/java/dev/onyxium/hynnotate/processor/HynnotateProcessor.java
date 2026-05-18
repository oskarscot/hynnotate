package dev.onyxium.hynnotate.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes({
                "dev.onyxium.hynnotate.annotations.CodecField",
                "dev.onyxium.hynnotate.annotations.CodecWith",
                "dev.onyxium.hynnotate.annotations.IncludeCodec"
})
public class HynnotateProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Hello testing processing!!!"
        );
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return true;
    }
}
