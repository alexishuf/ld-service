package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.RenderTransformer;
import org.apache.jena.rdf.model.Model;

import javax.annotation.Nonnull;

/**
 * A transformer that does nothing
 */
public class IdentityTransformer implements RenderTransformer {
    @Nonnull
    @Override
    public Model transform(@Nonnull Model model, boolean isSingleResource) {
        return model;
    }
}
