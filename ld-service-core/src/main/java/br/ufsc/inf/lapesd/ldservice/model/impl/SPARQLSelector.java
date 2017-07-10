package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Selector;
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

public class SPARQLSelector implements Selector {
    private final @Nonnull
    Model model;
    private final boolean singleResource;
    private final @Nonnull
    String template;

    public SPARQLSelector(@Nonnull Model model, boolean singleResource,
                          @Nonnull String template) {
        this.model = model;
        this.singleResource = singleResource;
        this.template = template;
    }

    @Nonnull
    @Override
    public List<Resource> selectResource(Activation activation) {
        String q = ActivationHelper.replace(template, activation);
        List<Resource> list = new ArrayList<>();
        try (QueryExecution execution = QueryExecutionFactory.create(q, model)) {
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

    @Override
    public boolean isSingleResource() {
        return singleResource;
    }
}
