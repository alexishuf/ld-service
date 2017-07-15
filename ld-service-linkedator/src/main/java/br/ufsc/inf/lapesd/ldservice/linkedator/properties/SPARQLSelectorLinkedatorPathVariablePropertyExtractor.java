package br.ufsc.inf.lapesd.ldservice.linkedator.properties;

import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.impl.SPARQLSelector;
import br.ufsc.inf.lapesd.ldservice.model.properties.extractors.ExtractsSelectorProperty;
import br.ufsc.inf.lapesd.ldservice.model.properties.extractors.SelectorPropertyExtractor;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.*;
import org.apache.jena.sparql.syntax.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;

import static br.ufsc.inf.lapesd.ldservice.model.impl.ActivationHelper.defReplacementRx;

/**
 * Extracts {@link LinkedatorPathVariableProperty} from a {@link SPARQLSelector}.
 */
@ExtractsSelectorProperty(type = LinkedatorPathVariableProperty.class, from = SPARQLSelector.class)
public class SPARQLSelectorLinkedatorPathVariablePropertyExtractor
        implements SelectorPropertyExtractor<LinkedatorPathVariableProperty> {
    private static Logger logger = LoggerFactory.getLogger(SPARQLSelectorLinkedatorPathVariablePropertyExtractor.class);

    @Override
    public Set<LinkedatorPathVariableProperty> extract(Selector selector) {
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

        SetMultimap<String, String> properties = getPropertyUsages(query.getQueryPattern());
        Set<LinkedatorPathVariableProperty> result = new HashSet<>();
        for (String varName : properties.keySet()) {
            Set<String> set = properties.get(varName);
            if (set.size() == 1) {
                result.add(new LinkedatorPathVariableProperty(varName, set.iterator().next()));
            } else {
                logger.trace("Variable {} was used with more than one property: {}", varName, set);
            }
        }

        return result;
    }

    private SetMultimap<String, String> getPropertyUsages(Element queryPattern) {
        SetMultimap<String, String> multimap = HashMultimap.create();

        ElementWalker.walk(queryPattern, new ElementVisitorBase() {
            private void processTriple(Triple triple) {
                Node predicate = triple.getPredicate(), object = triple.getObject();
                if (!object.isLiteral()) return;
                if (!predicate.isURI()) return;

                Matcher matcher = defReplacementRx.matcher(object.getLiteralLexicalForm());
                if (!matcher.find()) return;
                String varName = matcher.group(1);
                if (matcher.find()) {
                    logger.trace("Ambiguous literal {}", object.getLiteralLexicalForm());
                    return;
                }

                multimap.get(varName).add(predicate.toString());
            }

            @Override
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> it = el.patternElts();
                while (it.hasNext()) {
                    TriplePath t = it.next();
                    t.getPath().visit(new PathVisitor() {
                        @Override
                        public void visit(P_Link l) {
                            processTriple(new Triple(t.getSubject(), l.getNode(), t.getObject()));
                        }
                        @Override
                        public void visit(P_ReverseLink pathNode) {
                            // skip, as our object must be a literal
                        }
                        @Override
                        public void visit(P_NegPropSet pathNotOneOf) {
                            // no !! in https://www.w3.org/TR/sparql11-query/#propertypaths
                        }
                        @Override
                        public void visit(P_Inverse inversePath) {
                            // skip, as our object must be a literal
                        }
                        @Override
                        public void visit(P_Mod pathMod) {
                            pathMod.getSubPath().visit(this);
                        }
                        @Override
                        public void visit(P_FixedLength pFixedLength) {
                            pFixedLength.visit(this);
                        }
                        @Override
                        public void visit(P_Distinct pathDistinct) {
                            pathDistinct.visit(this);
                        }
                        @Override
                        public void visit(P_Multi pathMulti) {
                            pathMulti.getSubPath().visit(this);
                        }
                        @Override
                        public void visit(P_Shortest pathShortest) {
                            pathShortest.visit(this);
                        }
                        @Override
                        public void visit(P_ZeroOrOne path) {
                            path.visit(this);
                        }
                        @Override
                        public void visit(P_ZeroOrMore1 path) {
                            path.visit(this);
                        }
                        @Override
                        public void visit(P_ZeroOrMoreN path) {
                            path.visit(this);
                        }
                        @Override
                        public void visit(P_OneOrMore1 path) {
                            path.visit(this);
                        }
                        @Override
                        public void visit(P_OneOrMoreN path) {
                            path.getSubPath().visit(this);
                        }
                        @Override
                        public void visit(P_Alt pathAlt) {
                            pathAlt.getLeft().visit(this);
                            pathAlt.getRight().visit(this);
                        }
                        @Override
                        public void visit(P_Seq pathSeq) {
                            pathSeq.getRight().visit(this);
                        }
                    });

//                    tp.getPath().visit(new PathVisitorBase() {
//
//                    });
                }
            }

            @Override
            public void visit(ElementTriplesBlock el) {
                Iterator<Triple> it = el.patternElts();
                while (it.hasNext())
                    processTriple(it.next());
            }
        });
        return multimap;
    }
}
