package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;

import javax.annotation.Nonnull;
import java.util.Set;

public abstract class AbstractSelector implements Selector {
    protected SelectorPropertyList properties = new SelectorPropertyList();

    @Nonnull
    @Override
    public <T extends SelectorProperty> Set<T> getProperties(Class<T> propertyClass) {
        return properties.getProperties(propertyClass);
    }

    @Nonnull
    @Override
    public <T extends SelectorProperty> Selector addProperty(T property) {
        properties.add(property);
        return this;
    }

    @Override
    public <T extends SelectorProperty> Selector removeProperty(T property) {
        properties.remove(property);
        return this;
    }
}
