package br.ufsc.inf.lapesd.ldservice.model;

import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Resources selector.
 */
public interface Selector {
    /**
     * Selects one or a list of resources.
     *
     * @param activation activation with variable values to be used in resource selection
     * @return A list with the selected resources. If no resources were found, the list
     *         will be empty. If isSingleResource() == true, then the list may at most have
     *         a single element.
     */
    @Nonnull List<Resource> selectResource(Activation activation);

    /**
     * If true, selectResource() will either return an empty list or a singleton list.
     */
    boolean isSingleResource();
}
