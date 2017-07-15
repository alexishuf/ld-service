package br.ufsc.inf.lapesd.ldservice.model.properties.extractors;


import br.ufsc.inf.lapesd.ldservice.model.impl.SPARQLSelector;
import br.ufsc.inf.lapesd.ldservice.model.properties.ResourceType;
import br.ufsc.inf.lapesd.ldservice.priv.Utils;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static br.ufsc.inf.lapesd.ldservice.TestUtils.PREFIXES;

public class SPARQLSelectorResourceTypeExtractorTest {

    private final Model defaultModel = ModelFactory.createDefaultModel();

    private Set<ResourceType> extract(String template) {
        return ExtractorRegistry.get().extract(ResourceType.class,
                SPARQLSelector.fromModel(defaultModel).selectSingle(template));
    }

    private Set<ResourceType> expected(String... uris) {
        return Arrays.stream(uris).map(u -> new ResourceType(true, u))
                .collect(Collectors.toSet());
    }

    @Test
    public void testBadSPARQLToleratedOnParse() {
        /* we rely on jena's parser delaying the check of lexical form against the datatype */
        QueryFactory.create(PREFIXES + "SELECT ?p WHERE {\n" +
                "  ?p foaf:account/foaf:accountName \"${id}\"^^xsd:int." +
                "}");
    }

    @Test
    public void testBluntType() {
        Assert.assertEquals(extract(PREFIXES + "SELECT ?p WHERE { ?p a foaf:Person. }"),
                expected(FOAF.Person.getURI()));
    }

    @Test
    public void testTypeInPath() {
        Assert.assertEquals(extract(PREFIXES +
                        "SELECT ?p WHERE { ?p foaf:account/a foaf:Person. }"),
                Collections.emptySet());
    }

    @Test
    public void testAmbiguous() {
        Assert.assertEquals(extract(PREFIXES + "SELECT ?p WHERE { " +
                        "  ?p a foaf:Person. " +
                        "  ?p a sh:Place.\n}"),
                expected(FOAF.Person.getURI(), Utils.schemaR("Place").getURI()));
    }

    @Test
    public void testVariable() {
        Assert.assertEquals(extract(PREFIXES + "SELECT ?p WHERE { " +
                        "  ?p a foaf:Person. " +
                        "  ?p a ?b.\n}"),
                expected(FOAF.Person.getURI()));
    }
}