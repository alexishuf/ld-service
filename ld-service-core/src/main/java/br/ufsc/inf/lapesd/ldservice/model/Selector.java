package br.ufsc.inf.lapesd.ldservice.model;

import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

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

    /**
     * Gets all properties assignable to the given property class.
     *
     * @param propertyClass Class of properties desired, can be {@link SelectorProperty}
     * @return possibly empty list of properties.
     */
    @Nonnull
    <T extends SelectorProperty> Set<T> getProperties(Class<T> propertyClass);

    /**
     * Adds a property to the {@link Selector}.
     *
     * @param property Property to be added, if already present will have no effect
     * @return The {@link Selector} itself, for chaining method calls
     */
    @Nonnull
    <T extends SelectorProperty> Selector addProperty(T property);

    /**
     * removes a property from the {@link Selector}, if present.
     *
     * @param property Property to be added, if already present will have no effect
     * @return The {@link Selector} itself, for chaining method calls
     */
    <T extends SelectorProperty> Selector removeProperty(T property);
}
