package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RewriteSelector implements Selector{
    private final @Nonnull String template;
    private SelectorPropertyList properties = new SelectorPropertyList();

    public RewriteSelector(@Nonnull String template) {
        this.template = template;
    }

    @Nonnull
    @Override
    public List<Resource> selectResource(Activation activation) {
        String uri = ActivationHelper.replace(template, activation);
        return Collections.singletonList(ResourceFactory.createResource(uri));
    }

    @Override
    public boolean isSingleResource() {
        return true;
    }

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
