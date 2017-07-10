package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.RenderTransformer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;

import javax.annotation.Nonnull;

public class JenaReasoningTransformer implements RenderTransformer {
    private @Nonnull final Reasoner reasoner;

    public JenaReasoningTransformer(@Nonnull Reasoner reasoner) {
        this.reasoner = reasoner;
    }

    @Nonnull
    @Override
    public Model transform(@Nonnull Model model, boolean isSingleResource) {
        return ModelFactory.createInfModel(reasoner, model);
    }
}
