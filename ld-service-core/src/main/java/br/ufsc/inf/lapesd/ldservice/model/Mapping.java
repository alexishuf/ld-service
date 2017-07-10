package br.ufsc.inf.lapesd.ldservice.model;

import br.ufsc.inf.lapesd.ld_jaxrs.core.traverser.Traverser;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A collection of {@link Selector} and {@link UriRewrite} instances.
 */
public class Mapping {
    private final @Nonnull LinkedHashMap<Activator, Selector> selectors;
    private final @Nonnull LinkedHashMap<Activator, Traverser> traversers;
    private final @Nonnull LinkedHashMap<Activator, RenderTransformer> transformers;
    private final @Nonnull List<UriRewrite> rewrites;

    public Mapping(@Nonnull LinkedHashMap<Activator, Selector> selectors,
                   @Nonnull LinkedHashMap<Activator, Traverser> traversers,
                   @Nonnull LinkedHashMap<Activator, RenderTransformer> transformers,
                   @Nonnull List<UriRewrite> rewrites) {
        this.selectors = selectors;
        this.traversers = traversers;
        this.transformers = transformers;
        this.rewrites = rewrites;
    }

    public @Nonnull Collection<Map.Entry<Activator, Selector>> getSelectors() {
        return selectors.entrySet();
    }
    public @Nonnull Collection<Map.Entry<Activator, Traverser>> getTraversers() {
        return traversers.entrySet();
    }
    public @Nonnull Collection<Map.Entry<Activator, RenderTransformer>> getTransformers() {
        return transformers.entrySet();
    }

    public @Nonnull List<UriRewrite> getRewrites() {
        return Collections.unmodifiableList(rewrites);
    }

    public static class Builder {
        private LinkedHashMap<Activator, Selector> selectors = new LinkedHashMap<>();
        private LinkedHashMap<Activator, Traverser> traversers = new LinkedHashMap<>();
        private LinkedHashMap<Activator, RenderTransformer> transformers = new LinkedHashMap<>();
        private List<UriRewrite> rewrites = new ArrayList<>();

        public @Nonnull Builder addSelector(@Nonnull Activator activator,
                                            @Nonnull Selector selector) {
            selectors.put(activator, selector);
            return this;
        }
        public @Nonnull Builder addTraverser(@Nonnull Activator activator,
                                             @Nonnull Traverser traverser) {
            traversers.put(activator, traverser);
            return this;
        }
        public @Nonnull Builder addTransformer(@Nonnull Activator activator,
                                             @Nonnull RenderTransformer transformer) {
            transformers.put(activator, transformer);
            return this;
        }
        public @Nonnull Builder addRewrite(UriRewrite rewrite) {
            rewrites.add(rewrite);
            return this;
        }

        public @Nonnull Mapping build() {
            Mapping built = new Mapping(selectors, traversers, transformers, rewrites);
            selectors = new LinkedHashMap<>();
            traversers = new LinkedHashMap<>();
            transformers = new LinkedHashMap<>();
            rewrites = new ArrayList<>();
            return built;
        }
    }
}
