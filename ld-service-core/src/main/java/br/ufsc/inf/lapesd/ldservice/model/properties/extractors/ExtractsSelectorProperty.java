package br.ufsc.inf.lapesd.ldservice.model.properties.extractors;


import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents that a {@link SelectorPropertyExtractor} extracts a given type of
 * {@link SelectorProperty}
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtractsSelectorProperty {
    Class<? extends SelectorProperty> type();
    Class<? extends Selector> from();
}
