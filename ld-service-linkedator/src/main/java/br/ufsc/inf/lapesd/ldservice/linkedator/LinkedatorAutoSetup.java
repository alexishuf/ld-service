package br.ufsc.inf.lapesd.ldservice.linkedator;

import br.ufsc.inf.lapesd.ld_jaxrs.jena.JenaProviders;
import br.ufsc.inf.lapesd.ldservice.LDEndpoint;
import br.ufsc.inf.lapesd.linkedator.SemanticMicroserviceDescription;
import br.ufsc.inf.lapesd.linkedator.jersey.LinkedadorApi;
import br.ufsc.inf.lapesd.linkedator.jersey.LinkedadorWriterInterceptor;
import br.ufsc.inf.lapesd.linkedator.jersey.LinkedatorConfig;
import com.google.gson.Gson;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class LinkedatorAutoSetup {
    private static Set<Class<?>> providers;

    static {
        providers = new HashSet<>();
        providers.addAll(JenaProviders.getProviders());
        providers.add(LinkedadorWriterInterceptor.class);

        ClientBuilder cb = LinkedadorWriterInterceptor.getGlobalLinkedatorApi().getClientBuilder();
        JenaProviders.getProviders().forEach(cb::register);
    }


    public static Set<Class<?>> getProviders() {
        return providers;
    }

    public static Builder1 setup(int port, @Nonnull String applicationPath) {
        return new Builder1(port, applicationPath);
    }
    public static Builder1 setup(int port) {
        return setup(port, "/");
    }

    public static class Builder1 {
        private int port;
        private @Nonnull String applicationPath;

        public Builder1(int port, @Nonnull String applicationPath) {
            this.port = port;
            this.applicationPath = applicationPath;
        }

        public Builder2 withEndpoints(Application application) {
            return withEndpoints(application.getSingletons().stream()
                    .filter(o -> o instanceof LDEndpoint)
                    .map(o -> (LDEndpoint) o).collect(Collectors.toList()));
        }
        public Builder2 withEndpoints(Collection<LDEndpoint> endpoints) {
            return new Builder2(port, applicationPath, endpoints);
        }
    }

    public static class Builder2 {
        private int port;
        private @Nonnull String applicationPath;
        private @Nonnull Collection<LDEndpoint> endpoints;
        private @Nonnull List<Model> ontologies = new ArrayList<>();

        public Builder2(int port, @Nonnull String applicationPath,
                        @Nonnull Collection<LDEndpoint> endpoints) {
            this.port = port;
            this.applicationPath = applicationPath;
            this.endpoints = endpoints;
        }

        public Builder2 withOntology(Model ontology) {
            ontologies.add(ontology);
            return this;
        }

        private LinkedadorApi doSetup(Application app) throws IOException {
            LinkedadorApi api = getApi(app);
            api.setConfig(generateLinkedatorConfig(endpoints, port, applicationPath,
                    ontologies, api.getConfig()));
            JenaProviders.getProviders().forEach(api.getClientBuilder()::register);
            return api;
        }

        public void setup(Application app) throws IOException { doSetup(app); }
        public void setupAndRegister(Application app) throws IOException {
            doSetup(app).registerMicroservice();
        }

        public Application createApplication() throws IOException {
            Set<Object> singletons = new HashSet<>(endpoints);
            return new Application() {
                @Override
                public Set<Object> getSingletons() {
                    return singletons;
                }

                @Override
                public Set<Class<?>> getClasses() {
                    return getProviders();
                }
            };
        }
    }


    public static LinkedadorApi getApi(@Nullable Application application) {
        LinkedadorApi api = LinkedadorWriterInterceptor.getGlobalLinkedatorApi();
        if (application != null) {
            api = application.getSingletons().stream()
                    .filter(s -> s instanceof LinkedadorWriterInterceptor)
                    .map(s -> ((LinkedadorWriterInterceptor)s).getLinkedatorApi())
                    .findFirst().orElse(api);
        }
        return api;
    }

    @Nonnull
    private static LinkedatorConfig
    generateLinkedatorConfig(@Nonnull Collection<LDEndpoint> endpoints, int port,
                             @Nonnull String applicationPath,
                             @Nonnull Collection<Model> ontologies,
                             @Nonnull LinkedatorConfig config) throws IOException {
        SemanticMicroserviceDescription smd;
        smd = generateDescription(endpoints, applicationPath, Collections.emptyList());

        config.setMicroserviceUriBase(smd.getUriBase());
        config.setServerPort(port);

        MultiUnion unionGraph = new MultiUnion();
        ontologies.stream().map(Model::getGraph).forEach(unionGraph::addGraph);
        Model union = ModelFactory.createModelForGraph(unionGraph);
        File ontologyFile = null, smdFile = null;
        try {
            ontologyFile = Files.createTempFile("", ".rdf").toFile();
            ontologyFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(ontologyFile)) {
                RDFDataMgr.write(out, union, Lang.RDFXML);
            }

            smdFile = Files.createTempFile("", ".json").toFile();
            try (FileWriter writer = new FileWriter(smdFile)) {
                new Gson().toJson(smd, writer);
            }
        } catch (Exception e) {
            if (ontologyFile != null) Files.delete(ontologyFile.toPath());
            if (smdFile != null) Files.delete(smdFile.toPath());
            throw e;
        } finally {
            unionGraph.getSubGraphs().forEach(unionGraph::removeGraph);
            union.close();
        }

        config.setMicroserviceDescriptionFile(smdFile.getAbsolutePath());
        config.setOntologyFile(ontologyFile.getAbsolutePath());
        return config;
    }

    public static SemanticMicroserviceDescription
    generateDescription(@Nonnull Collection<LDEndpoint> endpoints, @Nonnull String applicationPath,
                        Collection<Model> ontologies) {
        DescriptionGenerator generator = new DescriptionGenerator();
        SemanticMicroserviceDescription base = new SemanticMicroserviceDescription();
        base.setUriBase(applicationPath);

        MultiUnion unionGraph = new MultiUnion();
        ontologies.stream().map(Model::getGraph).forEach(unionGraph::addGraph);
        Model union = ModelFactory.createModelForGraph(unionGraph);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RDFDataMgr.write(out, union, Lang.RDFXML);
            base.setOntologyBase64(Base64.getEncoder().encodeToString(out.toByteArray()));
        } finally {
            union.close();
        }

        generator.overloadDescription(base);
        endpoints.forEach(generator::addMappings);
        return generator.getDescription();
    }
}
