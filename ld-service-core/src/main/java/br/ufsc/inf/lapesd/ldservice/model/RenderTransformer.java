package br.ufsc.inf.lapesd.ldservice.model;

import br.ufsc.inf.lapesd.ldservice.model.impl.SequenceTransformer;
import org.apache.jena.rdf.model.Model;

import javax.annotation.Nonnull;

/**
 * Performs arbitrary transformation on a rendered {@link Model}
 */
public interface RenderTransformer {
    /**
     * Performs a transformation on the model and returns the result as a new model.
     *
     * This method is responsible for closing the input model, if it should be closed.
     *
     * @param model input render Model
     * @param isSingleResource true if the render is of a single resource, false if it is of a list
     * @return The result of transformation
     */
    @Nonnull Model transform(@Nonnull Model model, boolean isSingleResource);

    default @Nonnull RenderTransformer andThen(@Nonnull RenderTransformer next) {
        return new SequenceTransformer(this, next);
    }
}
