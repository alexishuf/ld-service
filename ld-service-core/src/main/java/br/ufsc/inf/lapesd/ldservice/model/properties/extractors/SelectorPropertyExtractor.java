package br.ufsc.inf.lapesd.ldservice.model.properties.extractors;

import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;

import java.util.Set;

/**
 * Extracts some {@link SelectorProperty} from a {@link Selector}.
 */
public interface SelectorPropertyExtractor<P extends SelectorProperty> {
    /**
     * List of {@link SelectorProperty} instances that can be inferred from the selector,
     * ignoring the result of <code>Selector.getProperties()</code>. Normally a user of this class
     * will want to override the results of this method with the results obtained from
     * {@link Selector}.getProperties().
     *
     * @param selector selector to analyze
     * @return list of inferred properties
     */
    Set<P> extract(Selector selector);
}
