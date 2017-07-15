package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;

import java.util.*;

/**
 * A List for containing {@link SelectorProperty} instances.
 */
public class SelectorPropertyList extends LinkedHashSet<SelectorProperty> {
    public <T extends SelectorProperty> Set<T> getProperties(Class<T> propertyClass) {
        Set<T> set = new HashSet<>();
        for (SelectorProperty property : this) {
            if (propertyClass.isAssignableFrom(property.getClass())) {
                //noinspection unchecked
                set.add((T) property);
            }
        }
        return set;
    }
}
