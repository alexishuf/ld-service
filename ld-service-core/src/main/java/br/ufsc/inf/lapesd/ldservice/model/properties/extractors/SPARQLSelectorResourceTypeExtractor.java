package br.ufsc.inf.lapesd.ldservice.model.properties.extractors;

import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.impl.SPARQLSelector;
import br.ufsc.inf.lapesd.ldservice.model.properties.ResourceType;
import com.google.common.base.Preconditions;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@ExtractsSelectorProperty(type = ResourceType.class, from = SPARQLSelector.class)
public class SPARQLSelectorResourceTypeExtractor implements SelectorPropertyExtractor<ResourceType> {
    private static Logger logger = LoggerFactory.getLogger(SPARQLSelectorResourceTypeExtractor.class);

    @Override
    public Set<ResourceType> extract(Selector selector) {
        Preconditions.checkArgument(selector instanceof SPARQLSelector);
        String template = ((SPARQLSelector) selector).getSPARQLTemplate();
        Query query;
        try {
            query = QueryFactory.create(template);
        } catch (QueryParseException e) {
            return Collections.emptySet();
        }
        if (!query.isSelectType())
            return Collections.emptySet();

        List<String> list = new ArrayList<>();
        Var main = query.getProjectVars().get(0);
        ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {
            private void processTriple(Triple triple) {
                if (!triple.getSubject().matches(main)) return;
                if (!triple.getPredicate().isURI()) return;
                if (!triple.getPredicate().getURI().equals(RDF.type.getURI())) return;
                if (!triple.getObject().isURI()) return;

                list.add(triple.getObject().getURI());
            }

            @Override
            public void visit(ElementPathBlock el) {
                for (Iterator<TriplePath> it = el.patternElts(); it.hasNext(); ) {
                    TriplePath triplePath = it.next();
                    if (triplePath.getPredicate() != null) processTriple(triplePath.asTriple());
                }
            }

            @Override
            public void visit(ElementTriplesBlock el) {
                for (Iterator<Triple> it = el.patternElts(); it.hasNext(); )
                    processTriple(it.next());
            }
        });

        if (list.isEmpty())
            logger.trace("Could not extract type from {}", template, list.size());
        return list.stream().map(u -> new ResourceType(true, u)).collect(Collectors.toSet());
    }
}
