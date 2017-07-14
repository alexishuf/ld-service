package br.ufsc.inf.lapesd.ldservice.model;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public interface Activation<T> {
    /**
     * Gets all defined variables in this activation. These retain the order
     * of {@link Activator}.getVarNames().
     */
    @Nonnull List<String> getVarNames();

    /**
     * Equivalent to <code>getVarNames().contains(varName)</code>, but may be substantially faster.
     */
    boolean contains(String varName);

    /**
     * Gets the value that matched the given variable name.
     *
     * @throws NoSuchElementException if <code>!contains(varName)</code>
     */
    @Nonnull T get(String varName) throws NoSuchElementException;

    T getOrDefault(String varName, T defaultValue);
}
