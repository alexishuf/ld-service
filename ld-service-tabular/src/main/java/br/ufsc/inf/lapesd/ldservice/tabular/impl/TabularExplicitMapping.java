package br.ufsc.inf.lapesd.ldservice.tabular.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.TabularSemanticMapping;
import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;

public class TabularExplicitMapping implements TabularSemanticMapping {
    private @Nonnull  BiMap<String, Property> map;
    private @Nullable GenericRuleReasoner reasoner;

    public TabularExplicitMapping(@Nonnull BiMap<String, Property> map,
                                  @Nullable GenericRuleReasoner reasoner) {
        this.map = map;
        this.reasoner = reasoner;
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    @Override
    public String toColumn(Property property) throws NoSuchElementException {
        String col = map.inverse().get(property);
        if (col == null) throw new NoSuchElementException();
        return col;
    }

    @Nonnull
    @Override
    public Property toProperty(String column) throws NoSuchElementException {
        Property property = map.get(column);
        if (property == null) throw new NoSuchElementException();
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TabularExplicitMapping that = (TabularExplicitMapping) o;

        if (!map.equals(that.map)) return false;
        if (reasoner == null || that.reasoner == null) return reasoner == that.reasoner;
        return reasoner.getRules().equals(that.reasoner.getRules());
    }

    @Override
    public int hashCode() {
        int result = map.hashCode();
        result = 31 * result + (reasoner != null ? reasoner.getRules().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TabularExplicitMapping(map=" + map.toString()
                + (reasoner == null ? ")"
                                    : ", rules=" + reasoner.getRules().toString());
    }

    @Nonnull
    @Override
    public Resource map(Row row) {
        Preconditions.checkArgument(row.getColumns().stream().allMatch(map::containsKey));
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource();
        for (String column : row.getColumns()) {
            resource.addProperty(map.get(column), createPlainLiteral(row.get(column)));
        }
        if (reasoner == null)
            return resource;
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        return infModel.createResource(resource.getId());
    }

    public static class Builder {
        private BiMap<String, Property> map = HashBiMap.create();
        private List<Rule> rules = new ArrayList<>();

        @Nonnull
        public Builder map(@Nonnull String columnName, @Nonnull Property property) {
            map.put(columnName, property);
            return this;
        }
        @Nonnull
        public Builder withRule(@Nonnull String rule) {
            return withRule(Rule.parseRule(rule));
        }
        @Nonnull
        public Builder withRule(@Nonnull Rule rule) {
            rules.add(rule);
            return this;
        }

        @Nonnull
        public TabularExplicitMapping build() {
            GenericRuleReasoner reasoner = rules.isEmpty() ? null : new GenericRuleReasoner(rules);
            return new TabularExplicitMapping(map, reasoner);
        }
    }
}
