package br.ufsc.inf.lapesd.ldservice.linkedator;

import br.ufsc.inf.lapesd.ldservice.TestUtils;
import br.ufsc.inf.lapesd.ldservice.linkedator.properties.LinkedatorPathVariableProperty;
import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Mapping;
import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.impl.AbstractSelector;
import br.ufsc.inf.lapesd.ldservice.model.impl.PathTemplateActivator;
import br.ufsc.inf.lapesd.ldservice.model.impl.SPARQLSelector;
import br.ufsc.inf.lapesd.ldservice.model.impl.SelectorPropertyList;
import br.ufsc.inf.lapesd.ldservice.model.properties.ResourceType;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;
import br.ufsc.inf.lapesd.linkedator.SemanticMicroserviceDescription;
import br.ufsc.inf.lapesd.linkedator.SemanticResource;
import br.ufsc.inf.lapesd.linkedator.UriTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.*;

public class DescriptionGeneratorTest {
    private static String NS = "http://example.org/ns#";
    private static LinkedatorPathVariableProperty idProperty = new LinkedatorPathVariableProperty("id", NS + "id");
    private static ResourceType workerProperty = new ResourceType(true, NS+"Worker");

    private static boolean equals(UriTemplate a,
                           UriTemplate b) {
        return Objects.equals(a.getUri(), b.getUri())
                && Objects.equals(a.getMethod(), b.getMethod())
                && Objects.equals(a.getParameters(), b.getParameters());
    }

    private static boolean equals(SemanticResource a,
                           SemanticResource b)  {
        if (!Objects.equals(a.getEntity(), b.getEntity())) return false;
        if ((a.getProperties() == null) != (b.getProperties() == null)) return false;
        if (!new HashSet<>(a.getProperties()).equals(new HashSet<>(b.getProperties()))) return false;

        List<UriTemplate> tsA = a.getUriTemplates(), tsB = b.getUriTemplates();
        return tsA.stream().allMatch(tA -> tsB.stream().anyMatch(tB -> equals(tA, tB)))
                && tsB.stream().allMatch(tB -> tsA.stream().anyMatch(tA -> equals(tB, tA)));

    }

    private static boolean equals(SemanticMicroserviceDescription a,
                           SemanticMicroserviceDescription b) {
        List<SemanticResource> srsA = a.getSemanticResources(), srsB = b.getSemanticResources();
        return Objects.equals(a.getIpAddress(), b.getIpAddress())
                && Objects.equals(a.getServerPort(), b.getServerPort())
                && Objects.equals(a.getUriBase(), b.getUriBase())
                && Objects.equals(a.getOntologyBase64(), b.getOntologyBase64())
                && srsA.stream().allMatch(srA -> srsB.stream()
                        .anyMatch(srB -> equals(srA, srB)))
                && srsB.stream().allMatch(srB -> srsA.stream()
                        .anyMatch(srA -> equals(srB, srA)));
    }

    private static String notEqualsMessage(SemanticMicroserviceDescription actual,
                                    SemanticMicroserviceDescription expected) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return String.format("Descriptions differ.\nactual=%s\nexpected=%s\n",
                gson.toJson(actual), gson.toJson(expected));
    }

    private static String loadJSON(String filename) throws IOException {
        String path = "DescriptionGenerator/" + filename + ".json";
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return IOUtils.toString(cl.getResourceAsStream(path), "UTF-8");
    }

    private static class DummySelector  extends AbstractSelector implements Selector {
        @Nonnull
        @Override
        public List<Resource> selectResource(Activation activation) {
            return Collections.emptyList();
        }
        @Override
        public boolean isSingleResource() {
            return true;
        }
    }

    @DataProvider
    public static Object[][] overloadData() throws IOException {
        return new Object[][] {
                {1, loadJSON("empty"), loadJSON("simple"),
                        loadJSON("simple")},
                {2, loadJSON("simple"), loadJSON("ip-only"),
                        loadJSON("simple+ip-only")},
                {3, loadJSON("simple"), loadJSON("car"),
                        loadJSON("simple+car")},
                {4, loadJSON("simple"), loadJSON("by-name"),
                        loadJSON("simple+by-name")},
                {5, loadJSON("incomplete"), loadJSON("missing-param"),
                        loadJSON("incomplete+missing-param")},
        };
    }

    @Test(dataProvider = "overloadData")
    public void testOverload(int row, @Nonnull String bottomJSON,
                             @Nonnull String topJSON,
                             @Nonnull String expectedJSON) {
        SemanticMicroserviceDescription bottom, top, expected;
        bottom = new Gson().fromJson(bottomJSON, SemanticMicroserviceDescription.class);
        top = new Gson().fromJson(topJSON, SemanticMicroserviceDescription.class);
        expected = new Gson().fromJson(expectedJSON, SemanticMicroserviceDescription.class);

        DescriptionGenerator gen = new DescriptionGenerator();
        gen.overloadDescription(bottom);
        gen.overloadDescription(top);
        SemanticMicroserviceDescription actual = gen.getDescription();
        Assert.assertTrue(equals(actual, expected), notEqualsMessage(actual, expected));
    }

    @DataProvider
    public static Object[][] mappingsData() throws IOException {
        return new Object[][]{
                {
                        new Mapping.Builder().addSelector(new PathTemplateActivator("/{id}"),
                                new DummySelector().addProperty(idProperty)
                                        .addProperty(workerProperty)).build(),
                        loadJSON("mapping-worker")
                },
                {
                /* goal: test if extractors are called */
                        new Mapping.Builder().addSelector(new PathTemplateActivator("/{id}"),
                                SPARQLSelector.fromModel(ModelFactory.createDefaultModel())
                                        .selectSingle(TestUtils.PREFIXES +
                                                "SELECT ?p WHERE {\n" +
                                                "  ?p a foaf:Person.\n" +
                                                "  ?p foaf:account/foaf:accountName \"${id}\".\n" +
                                                "}")).build(),
                        loadJSON("mapping-person")
                }
        };
    }

    @Test(dataProvider = "mappingsData")
    public void testMappingDescription(@Nonnull Mapping mapping,
                                       @Nonnull String expectedJSON) {
        DescriptionGenerator gen = new DescriptionGenerator();
        SemanticMicroserviceDescription expected = new Gson()
                .fromJson(expectedJSON, SemanticMicroserviceDescription.class);
        SemanticMicroserviceDescription actual = gen.toPartialDescription(mapping);
        Assert.assertTrue(equals(actual, expected), notEqualsMessage(actual, expected));
    }
}