package br.ufsc.inf.lapesd.ldservice.linkedator.properties;

import br.ufsc.inf.lapesd.ldservice.TestUtils;
import br.ufsc.inf.lapesd.ldservice.model.impl.SPARQLSelector;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static br.ufsc.inf.lapesd.ldservice.TestUtils.PREFIXES;


public class SPARQLSelectorLinkedatorPathVariablePropertyExtractorTest {
    private final String accountName = PREFIXES +
            "SELECT ?p WHERE {\n" +
            "  ?p foaf:account ?a.\n" +
            "  ?a foaf:accountName \"${id}\"^^xsd:int." +
            "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
            "}";

    private Set<LinkedatorPathVariableProperty> extract(String template) {
        Model empty = ModelFactory.createDefaultModel();
        return new SPARQLSelectorLinkedatorPathVariablePropertyExtractor()
                .extract(SPARQLSelector.fromModel(empty).selectSingle(template));
    }

    private Set<LinkedatorPathVariableProperty> expected(String... strings) {
        Set<LinkedatorPathVariableProperty> set = new HashSet<>();
        for (int i = 0; i < strings.length; i += 2)
            set.add(new LinkedatorPathVariableProperty(strings[i], strings[i + 1]));
        return set;
    }

    @Test
    public void testBadSPARQLToleratedOnParse() {
        /* we rely on jena's parser delaying the check of lexical form against the datatype */
        QueryFactory.create(accountName);
    }

    @Test
    public void testAccountName() {
        Assert.assertEquals(extract(accountName),
                expected("id", FOAF.accountName.getURI()));
    }

    @Test
    public void testAccountNameInPath() {
        Assert.assertEquals(extract(PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p foaf:account/foaf:accountName \"${id}\"^^xsd:int.\n" +
                        "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                        "}"),
                expected("id", FOAF.accountName.getURI()));
    }

    @Test
    public void testPathAlternative() {
        Assert.assertEquals(extract(PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p foaf:account/(foaf:name|foaf:accountName) \"${id}\"^^xsd:int.\n" +
                        "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                        "}"),
                expected()); //can't decide between foaf:name, and foaf:accountName
    }


    @Test
    public void testPathReverse() {
        Assert.assertEquals(extract(PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p ^foaf:accountName \"${id}\"^^xsd:int.\n" +
                        "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                        "}"),
                expected());
    }

    @Test
    public void testPathNegation() {
        Assert.assertEquals(extract(PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p !foaf:accountName \"${id}\"^^xsd:int.\n" +
                        "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                        "}"),
                expected());
    }

    @Test
    public void testPathAlternativeWithReverse() {
        Assert.assertEquals(extract(PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p foaf:account/(^foaf:name|foaf:accountName) \"${id}\"^^xsd:int.\n" +
                        "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                        "}"),
                expected("id", FOAF.accountName.getURI()));
    }

    @Test
    public void testPathAlternativeWithNegation() {
        Assert.assertEquals(extract(PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p foaf:account/(foaf:accountName|!foaf:name) \"${id}\"^^xsd:int.\n" +
                        "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                        "}"),
                expected("id", FOAF.accountName.getURI()));
        /* negation does not introduce ambiguity */
    }
}