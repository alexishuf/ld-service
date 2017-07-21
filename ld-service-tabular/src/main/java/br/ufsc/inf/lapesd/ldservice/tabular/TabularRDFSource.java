package br.ufsc.inf.lapesd.ldservice.tabular;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.TabularDataSource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TabularRDFSource {
    private final @Nonnull TabularDataSource source;
    private final @Nonnull TabularSemanticMapping mapping;

    public TabularRDFSource(@Nonnull TabularDataSource source,
                            @Nonnull TabularSemanticMapping mapping) {
        this.source = source;
        this.mapping = mapping;
    }

    @Nonnull
    public TabularDataSource getSource() {
        return source;
    }

    @Nonnull
    public TabularSemanticMapping getMapping() {
        return mapping;
    }

    public List<Resource> select(Map<Property, String> valuesMap) {
        Map<String, String> columnMap = new HashMap<>();
        valuesMap.keySet().forEach(k -> columnMap.put(mapping.toColumn(k), valuesMap.get(k)));
        return source.select(columnMap).stream().map(mapping::map).collect(Collectors.toList());
    }
}
