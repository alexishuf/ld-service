package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.Data1;
import br.ufsc.inf.lapesd.ldservice.model.Activation;
import org.apache.jena.fuseki.embedded.FusekiEmbeddedServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static br.ufsc.inf.lapesd.ldservice.Data1.*;
import static br.ufsc.inf.lapesd.ldservice.TestUtils.PREFIXES;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class RemoteSPARQLSelectorTest {
    private Dataset dataset;
    private FusekiEmbeddedServer server;
    private Model model;
    private String service;

    @BeforeClass
    public void setUp() {
        model = Data1.load();

        dataset = DatasetFactory.create(model);
        server = FusekiEmbeddedServer.create()
                .add("/data", dataset)
                .setLoopback(true).setPort(0).build();
        server.start();
        service = String.format("http://localhost:%d/data", server.getPort());
    }

    @AfterClass
    public void tearDown() {
        server.stop();
        server.join();
        dataset.close();
        model.close();
    }

    @Test
    public void testSelector() {
        SPARQLSelector s = SPARQLSelector.fromService(service).selectSingle(PREFIXES +
                "SELECT ?p WHERE {\n" +
                "  ?p foaf:account ?a.\n" +
                "  ?a foaf:accountName \"${id}\"." +
                "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                "}");
        Activation<String> activation = new PathTemplateActivator("/{id}")
                .tryActivate("/johnny1986");
        Assert.assertNotNull(activation);

        List<Resource> resources = s.selectResource(activation);
        Assert.assertNotNull(resources);
        List<Resource> expected = Collections.singletonList(createResource(A_NS + "John"));
        Assert.assertEquals(resources, expected);
    }
}
