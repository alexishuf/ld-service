package br.ufsc.inf.lapesd.ldservice.tabular.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import br.ufsc.inf.lapesd.ldservice.tabular.raw.impl.SimpleRow;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import static br.ufsc.inf.lapesd.ldservice.tabular.impl.TabularExplicitMapping.builder;
import static java.util.Arrays.asList;

public class TabularExplicitMappingTest {
    private static final String NS = "http://example.org/ns#";
    private static final Property a = ResourceFactory.createProperty(NS + "a");
    private static final Property b = ResourceFactory.createProperty(NS + "b");
    private static final Property c = ResourceFactory.createProperty(NS + "c");


    @DataProvider
    public static Object[][] data() {
        final String prologue = "@prefix ex: <" + NS + ">.\n";
        TabularExplicitMapping mapping1 = builder()
                .map("a", a).map("b", b).map("c", c).build();
        TabularExplicitMapping mapping2 = builder()
                .map("a", a).map("b", b).map("c", c)
                .withRule("[(?x <"+NS+"a> \"1\") -> (?x rdf:type <"+NS+"One>)]")
                .build();
        List<String> abc = asList("a", "b", "c");
        return new Object[][] {
                {mapping1, new SimpleRow(abc, asList("1", "2", "3")), prologue +
                        "[ ex:a \"1\"; ex:b \"2\"; ex:c \"3\" ]."},
                {mapping2, new SimpleRow(abc, asList("1", "2", "3")), prologue +
                        "[ a ex:One; ex:a \"1\"; ex:b \"2\"; ex:c \"3\" ]."}
        };
    }

    @Test(dataProvider = "data")
    public void test(TabularExplicitMapping mapping, Row row, String turtle) {
        Resource resource = mapping.map(row);
        Assert.assertNotNull(resource.getModel());

        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, IOUtils.toInputStream(turtle, StandardCharsets.UTF_8), Lang.TURTLE);
        Assert.assertTrue(model.isIsomorphicWith(resource.getModel()));
    }

    @Test
    public void testToProperty() {
        Assert.assertEquals(builder().map("a", a).build().toProperty("a"), a);
    }

    @Test
    public void testToColumn() {
        Assert.assertEquals(builder().map("a", a).build().toColumn(a), "a");
    }

    @Test
    public void testWithIncompleteMapping() {
        TabularExplicitMapping mapping = builder().map("a", a).build();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> mapping.map(new SimpleRow(asList("a", "b"), asList("1", "2"))));
    }

    @Test
    public void testGetMissingProperty() {
        TabularExplicitMapping m = builder().map("a", a).build();
        Assert.assertThrows(NoSuchElementException.class, () -> m.toProperty("b"));
    }

    @Test
    public void testGetMissingColumn() {
        TabularExplicitMapping m = builder().map("a", a).build();
        Assert.assertThrows(NoSuchElementException.class, () -> m.toColumn(b));
    }
}