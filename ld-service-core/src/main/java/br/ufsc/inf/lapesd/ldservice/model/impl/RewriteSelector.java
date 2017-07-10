package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Selector;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RewriteSelector implements Selector{
    private final @Nonnull String template;

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
}
