package dev.onyxium.hynnotate.processor;

import javax.lang.model.element.TypeElement;

public interface ModuleProcessor {

    void generate(TypeElement type);
}
