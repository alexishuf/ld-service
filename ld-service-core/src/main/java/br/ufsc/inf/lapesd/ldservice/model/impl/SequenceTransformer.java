package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.RenderTransformer;
import com.google.common.base.Preconditions;
import org.apache.jena.rdf.model.Model;

import javax.annotation.Nonnull;
import java.util.*;

public class SequenceTransformer implements RenderTransformer {
    private final @Nonnull Collection<RenderTransformer> transformers;

    public SequenceTransformer(@Nonnull Collection<RenderTransformer> transformers) {
        Preconditions.checkArgument(transformers.stream().allMatch(Objects::nonNull));
        this.transformers = transformers;
    }
    public SequenceTransformer(RenderTransformer... transformers) {
        this(Arrays.asList(transformers));
    }

    @Nonnull
    @Override
    public Model transform(@Nonnull Model model, boolean isSingleResource) {
        for (RenderTransformer t : transformers) {
            model = t.transform(model, isSingleResource);
        }
        return model;
    }

    @Nonnull
    @Override
    public RenderTransformer andThen(@Nonnull RenderTransformer next) {
        List<RenderTransformer> list = new ArrayList<>(transformers);
        list.add(next);
        return new SequenceTransformer(list);
    }
}
