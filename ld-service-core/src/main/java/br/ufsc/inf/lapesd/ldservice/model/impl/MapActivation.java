package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Activator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Activation implementation that stores all variables on a Map.
 */
public class MapActivation<T> implements Activation<T> {
    private final @Nonnull Activator activator;
    private final @Nonnull LinkedHashMap<String, T> map;
    private final @Nonnull List<String> names;

    public MapActivation(@Nonnull Activator activator,
                         @Nonnull LinkedHashMap<String, T> map) {
        this.activator = activator;
        this.map = map;
        names = Collections.unmodifiableList(new ArrayList<>(map.keySet()));
    }

    @Nonnull
    @Override
    public List<String> getVarNames() {
        return names;
    }

    @Nonnull
    @Override
    public T get(String varName) {
        if (!map.containsKey(varName)) {
            throw new NoSuchElementException(String.format("No variable %s for activation of %s",
                    varName, activator));
        }
        return map.get(varName);
    }
}
