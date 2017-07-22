package br.ufsc.inf.lapesd.ldservice.tabular;


import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.impl.AbstractSelector;
import com.google.common.base.Preconditions;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabularSelector extends AbstractSelector {
    private final boolean singleResource;
    private final @Nonnull Map<String, Property> propertiesMap;
    private final @Nonnull
    TabularRDFSource source;

    protected TabularSelector(@Nonnull TabularRDFSource source, boolean singleResource,
                              @Nonnull Map<String, Property> propertiesMap) {
        this.singleResource = singleResource;
        this.propertiesMap = propertiesMap;
        this.source = source;
    }

    public static Builder from(@Nonnull TabularRDFSource source) {
        return new Builder(source);
    }

    @Nonnull
    @Override
    public List<Resource> selectResource(Activation<?> activation) {
        List<String> vars = activation.getVarNames();
        Preconditions.checkArgument(vars.stream().allMatch(propertiesMap::containsKey));

        Map<Property, String> valueMap = new HashMap<>();
        vars.forEach(n -> valueMap.put(propertiesMap.get(n), activation.get(n).toString()));
        return source.select(valueMap);
    }

    @Override
    public boolean isSingleResource() {
        return singleResource;
    }

    public static class Builder {
        private final TabularRDFSource source;
        private Map<String, Property> propertyMap = new HashMap<>();

        public Builder(TabularRDFSource source) {
            this.source = source;
        }

        public @Nonnull Builder with(@Nonnull String varName, @Nonnull Property property) {
            propertyMap.put(varName, property);
            return this;
        }
        public @Nonnull Builder withRow(@Nonnull String varName) {
            return with(varName, TabularConstants.row);
        }

        public @Nonnull TabularSelector selectSingle() {
            return build(true);
        }
        public @Nonnull TabularSelector selectList() {
            return build(false);
        }

        public @Nonnull TabularSelector build(boolean isSingleResource) {
            return new TabularSelector(source, isSingleResource, propertyMap);
        }
    }
}
