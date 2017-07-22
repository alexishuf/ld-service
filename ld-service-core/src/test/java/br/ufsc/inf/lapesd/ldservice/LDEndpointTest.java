package br.ufsc.inf.lapesd.ldservice;

import br.ufsc.inf.lapesd.ld_jaxrs.core.traverser.CBDTraverser;
import br.ufsc.inf.lapesd.ld_jaxrs.jena.JenaProviders;
import br.ufsc.inf.lapesd.ldservice.model.Mapping;
import br.ufsc.inf.lapesd.ldservice.model.UriRewrite;
import br.ufsc.inf.lapesd.ldservice.model.impl.*;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static br.ufsc.inf.lapesd.ldservice.TestUtils.PREFIXES;
import static br.ufsc.inf.lapesd.ldservice.priv.Utils.schemaP;
import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class LDEndpointTest extends JerseyTestNg.ContainerPerMethodTest {

    @Override
    protected Application configure() {
        /* instance data */
        Model model = Data1.load();

        /* reasoner for schema */
        Model schema = ModelFactory.createDefaultModel();
        InputStream in = getClass().getClassLoader().getResourceAsStream("schema-1.ttl");
        RDFDataMgr.read(schema, in, Lang.TURTLE);
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(schema);

        /* endpoint setup */
        LDEndpoint endpoint = new LDEndpoint();
        endpoint.addMapping(new Mapping.Builder()
                .addRewrite(new UriRewrite(new RxActivator("http://a.example.org/ns#(.*)"), "/a/${1}"))
                .addRewrite(new UriRewrite(new RxActivator("http://b.example.org/ns#(.*)"), "/b/${1}"))
                .addSelector(new PathTemplateActivator("a/{localName}"),
                        new RewriteSelector(model, "http://a.example.org/ns#${localName}"))
                .addSelector(new PathTemplateActivator("b/{localName}"),
                        new RewriteSelector(model, "http://b.example.org/ns#${localName}"))
                .addSelector(new PathTemplateActivator("by_id/a/{id}"),
                        SPARQLSelector.fromModel(model).selectSingle(
                        PREFIXES +
                                "SELECT ?p WHERE {\n" +
                                "  ?p foaf:account ?a.\n" +
                                "  ?a foaf:accountName \"${id}\"." +
                                "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                                "}"))
                .addSelector(new PathTemplateActivator("by_id/b/{id}"),
                        SPARQLSelector.fromModel(model).selectSingle(
                        PREFIXES +
                                "SELECT ?p WHERE {\n" +
                                "  ?p foaf:account ?a.\n" +
                                "  ?a foaf:accountName \"${id}\"." +
                                "  ?a foaf:accountServiceHomePage <http://b.example.org/>." +
                                "}"))
                .addSelector(new PathTemplateActivator("list/a"),
                        SPARQLSelector.fromModel(model).selectList(
                        PREFIXES +
                                "SELECT ?p WHERE {\n" +
                                "  ?p foaf:account ?x.\n" +
                                "  ?x foaf:accountServiceHomePage <http://a.example.org/>.\n" +
                                "}"))
                .addSelector(new PathTemplateActivator("list/b"),
                        SPARQLSelector.fromModel(model).selectList(
                        PREFIXES +
                                "SELECT ?p WHERE {\n" +
                                "  ?p foaf:account ?x.\n" +
                                "  ?x foaf:accountServiceHomePage <http://b.example.org/>.\n" +
                                "}"))
                .addTraverser(new PathTemplateActivator("by_id/b/{id}"),
                        new CBDTraverser().setMaxPath(0))
                .addTraverser(new PathTemplateActivator("list/b"),
                        new CBDTraverser())
                .addTransformer(new PathTemplateActivator("by_id/b/{id}"),
                        new JenaReasoningTransformer(reasoner))
                .addTransformer(new PathTemplateActivator("list/b"),
                        new JenaReasoningTransformer(reasoner))
                .build());
        ResourceConfig rc = new ResourceConfig().register(endpoint);
        JenaProviders.getProviders().forEach(rc::register);
        return rc;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        JenaProviders.getProviders().forEach(config::register);
    }

    @Test
    public void testSelectRewriteSelector() {
        Model m = target("/a/John").request("text/turtle").get(Model.class);
        List<Statement> list = m.listStatements(null, schemaP("mainEntity"), (RDFNode) null).toList();
        Assert.assertEquals(list.size(), 1);
        Resource john = list.get(0).getResource();

        Assert.assertTrue(QueryExecutionFactory.create(PREFIXES +
                "ASK WHERE {\n" +
                "  <" + john.getURI() + "> a foaf:Person;\n" +
                "    foaf:account ?a.\n" +
                "  ?a foaf:accountName \"johnny1986\"." +
                "}", m).execAsk());
    }

    @Test
    public void testResourceHasMainEntity() {
        Model m = target("/by_id/a/johnny1986").request("text/turtle").get(Model.class);

        List<Statement> list = m.listStatements(null, schemaP("mainEntity"), (RDFNode) null).toList();
        Assert.assertEquals(list.size(), 1);

        Resource expected = createResource(target("/by_id/a/johnny1986").getUri().toString());
        Assert.assertEquals(list.get(0).getSubject(), expected);
        Assert.assertEquals(list.get(0).getResource(), expected);
    }

    @Test
    public void testGetById() {
        Model model = target("/by_id/a/johnny1986").request("text/turtle").get(Model.class);

        List<Resource> list = model.listSubjectsWithProperty(RDF.type, FOAF.Person).toList();
        Assert.assertEquals(list.size(), 1);
        Assert.assertTrue(list.get(0).getURI().endsWith("/by_id/a/johnny1986"));
    }

    @Test
    public void testGetByIdSameAs() {
        Model model = target("/by_id/a/johnny1986").request("text/turtle").get(Model.class);

        List<Resource> list = model.listSubjectsWithProperty(RDF.type, FOAF.Person).toList();
        Assert.assertEquals(list.size(), 1);
        List<RDFNode> objs = model.listObjectsOfProperty(list.get(0), OWL2.sameAs).toList();
        Assert.assertEquals(objs.size(), 2);
        Assert.assertTrue(objs.stream().allMatch(RDFNode::isURIResource));

        Assert.assertTrue(objs.stream().anyMatch(n -> n.asResource().getURI().endsWith("/a/John")));
        Assert.assertTrue(objs.stream()
                .anyMatch(n -> n.asResource().getURI().endsWith(Data1.A_NS + "John")));
    }

    @Test
    public void testNoSelector() {
        Response response = target("/by_name/Adelir").request("text/turtle")
                .get(Response.class);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.NOT_FOUND);
    }

    @Test
    public void testNotFound() {
        Response response = target("/by_id/a/adelir").request("text/turtle")
                .get(Response.class);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.NOT_FOUND);
    }

    @Test
    public void testTraverserSelection() {
        /* default CBD is unbounded */
        Model m;
        m = target("/by_id/a/johnny1986").request("text/turtle").get(Model.class);
        try (QueryExecution ex = QueryExecutionFactory.create(PREFIXES + "ASK {" +
                "  ?x a foaf:Person.\n" +
                "  ?x foaf:account ?a.\n" +
                "  ?a foaf:accountName ?name.\n" +
                "}", m)) {
            Assert.assertTrue(ex.execAsk());
        }

        /* for by_id/b/ CBDTraverser has maxPath = 0 */
        m = target("/by_id/b/bobby").request("text/turtle").get(Model.class);
        try (QueryExecution ex = QueryExecutionFactory.create(
                PREFIXES + "ASK {?x a foaf:Person.}", m)) {
            Assert.assertTrue(ex.execAsk());
        }
        try (QueryExecution ex = QueryExecutionFactory.create(PREFIXES + "ASK {" +
                "  ?x a foaf:Person.\n" +
                "  ?x foaf:account ?a.\n" +
                "  ?a foaf:accountName ?name.\n" +
                "}", m)) {
            Assert.assertFalse(ex.execAsk());
        }
    }

    @Test
    public void testListHasMainEntity() {
        Model m = target("/list/a").request("text/turtle").get(Model.class);
        List<Resource> list = m.listSubjectsWithProperty(schemaP("mainEntity")).toList();
        Assert.assertEquals(list.size(), 1);

        Resource expected = createResource(target("/list/a").getUri().toString());
        Assert.assertTrue(m.contains(expected, schemaP("mainEntity"), expected));
    }

    @Test
    public void testListWithEndSlash() {
        Model m = target("/list/a/").request("text/turtle").get(Model.class);
        List<Resource> list = m.listSubjectsWithProperty(schemaP("mainEntity")).toList();
        Assert.assertEquals(list.size(), 1);
    }

    @Test
    public void testListNoCBD() {
        Model m = target("/list/a").request("text/turtle").get(Model.class);
        Set<Resource> members = m.listObjectsOfProperty(schemaP("mainEntity"))
                .next().as(RDFList.class).asJavaList()
                .stream().map(RDFNode::asResource).collect(Collectors.toSet());

        Assert.assertEquals(members.size(), 3);
        Pattern pattern = Pattern.compile(".*/([^/]*)$");
        Assert.assertEquals(members.stream().map(r -> {
                    Matcher matcher = pattern.matcher(r.getURI());
                    return matcher.matches() ? matcher.group(1) : null;
                }).collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList("John", "Mary", "Jack")));

        for (Resource member : members) {
            Assert.assertEquals(member.listProperties().toList().stream()
                    .map(Statement::getPredicate).filter(p -> !p.equals(OWL2.sameAs)).count(),
                    0L, member.toString() + " had properties!");
        }
    }

    @Test
    public void testListWithCBD() {
        Model m = target("/list/b").request("text/turtle").get(Model.class);
        try (QueryExecution ex = QueryExecutionFactory.create(PREFIXES + "ASK {\n" +
                "  ?p sh:mainEntity ?e.\n" +
                "  ?e rdf:first ?bob.\n" +
                "  ?bob foaf:name \"Robert\".\n" +
                "}", m)) {
            Assert.assertTrue(ex.execAsk());
        }
    }

    @Test
    public void testResourceReasoning() {
        Model m = target("/by_id/b/bobby").request("text/turtle").get(Model.class);

        Resource bobby = m.listObjectsOfProperty(schemaP("mainEntity")).next().asResource();
        Assert.assertTrue(bobby.hasProperty(RDF.type, FOAF.Person));
        Assert.assertTrue(bobby.hasProperty(RDF.type,
                ResourceFactory.createResource(Data1.S_NS + "Super")));
        Assert.assertTrue(bobby.hasProperty(RDF.type,
                ResourceFactory.createResource(Data1.S_NS + "NamedPerson")));
    }

    @Test
    public void testResourceNoReasoning() {
        Model m = target("/by_id/a/johnny1986").request("text/turtle").get(Model.class);

        Resource bobby = m.listObjectsOfProperty(schemaP("mainEntity")).next().asResource();
        Assert.assertTrue(bobby.hasProperty(RDF.type, FOAF.Person));
        Assert.assertFalse(bobby.hasProperty(RDF.type,
                ResourceFactory.createResource(Data1.B_NS + "NamedPerson")));
    }

    @Test
    public void testListReasoning() {
        Model m = target("/list/b").request("text/turtle").get(Model.class);

//        InputStream in = getClass().getClassLoader().getResourceAsStream("schema-1.ttl");
//        Model schema = ModelFactory.createDefaultModel();
//        RDFDataMgr.read(schema, in, Lang.TURTLE);
//        Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(schema);
//        m = ModelFactory.createInfModel(reasoner, m);
////        m.setNsPrefix("rdf", RDF.getURI());
////        m.setNsPrefix("rdfs", RDFS.getURI());
////        m.setNsPrefix("owl", OWL2.getURI());
////        m.setNsPrefix("xsd", XSD.getURI());
////        RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_BLOCKS);

        List<Resource> persons = m.listObjectsOfProperty(schemaP("mainEntity")).next()
                .as(RDFList.class).asJavaList().stream().map(RDFNode::asResource)
                .collect(Collectors.toList());
        Assert.assertEquals(persons.size(), 1);
        Resource bob = persons.get(0);
        Assert.assertTrue(bob.hasProperty(RDF.type, FOAF.Person));
        Assert.assertTrue(bob.hasProperty(FOAF.name, createPlainLiteral("Robert")));
        Assert.assertTrue(bob.hasProperty(RDF.type, createResource(Data1.S_NS + "Super")));
        Assert.assertTrue(bob.hasProperty(RDF.type, createResource(Data1.S_NS + "NamedPerson")));
    }
}
