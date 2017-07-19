package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.apache.jena.query.QueryExecutionFactory.sparqlService;

public class SPARQLSelector extends AbstractSelector implements Selector  {
    private final @Nonnull Function<String, QueryExecution> executionFactory;
    private final boolean singleResource;
    private final @Nonnull String template;

    public static Builder fromModel(@Nonnull Model model) {
        return new BuilderImpl(q -> QueryExecutionFactory.create(q, model));
    }

    public static ServiceBuilder fromService(String service) {
        return new ServiceBuilder(service);
    }

    protected SPARQLSelector(@Nonnull Function<String, QueryExecution> executionFactory,
                          boolean singleResource, @Nonnull String template) {
        this.executionFactory = executionFactory;
        this.singleResource = singleResource;
        this.template = template;
    }

    @Nonnull
    @Override
    public List<Resource> selectResource(Activation<?> activation) {
        String q = ActivationHelper.replace(template, activation);
        List<Resource> list = new ArrayList<>();
        try (QueryExecution execution = executionFactory.apply(q)) {
            ResultSet resultSet = execution.execSelect();
            String main = resultSet.getResultVars().iterator().next();
            while (resultSet.hasNext()) {
                QuerySolution sol = resultSet.next();
                RDFNode node = sol.get(main);
                if (!node.isResource())
                    continue;
                list.add(node.asResource());
                if (isSingleResource())
                    break;
            }
        }
        return list;
    }

    @Nonnull
    public String getSPARQLTemplate() {
        return template;
    }

    @Override
    public boolean isSingleResource() {
        return singleResource;
    }

    public interface Builder {
        default SPARQLSelector selectSingle(@Nonnull String template) {
            return select(true, template);
        }
        default SPARQLSelector selectList(@Nonnull String template) {
            return select(false, template);
        }
        SPARQLSelector select(boolean singleResource, @Nonnull String template);
    }

    public static class BuilderImpl implements Builder {
        private final @Nonnull Function<String, QueryExecution> executionFactory;
        BuilderImpl(@Nonnull Function<String, QueryExecution> executionFactory) {
            this.executionFactory = executionFactory;
        }

        public SPARQLSelector select(boolean singleResource, @Nonnull String template) {
            return new SPARQLSelector(executionFactory, singleResource, template);
        }
    }

    public static class ServiceBuilder implements Builder {
        private HttpClient httpClient;
        private HttpContext httpContext;
        private final @Nonnull String service;

        ServiceBuilder(@Nonnull String service) {
            this.service = service;
        }

        @Nonnull public ServiceBuilder withHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }
        @Nonnull public ServiceBuilder withHttpContext(HttpContext httpContext) {
            this.httpContext = httpContext;
            return this;
        }

        @Override
        public SPARQLSelector select(boolean singleResource, @Nonnull String template) {
            return new SPARQLSelector(q -> sparqlService(service, q, httpClient, httpContext),
                    singleResource, template);
        }
    }
}
