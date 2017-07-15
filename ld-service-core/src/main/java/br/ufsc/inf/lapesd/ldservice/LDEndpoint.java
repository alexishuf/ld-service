package br.ufsc.inf.lapesd.ldservice;

import br.ufsc.inf.lapesd.ld_jaxrs.core.traverser.CBDTraverser;
import br.ufsc.inf.lapesd.ld_jaxrs.core.traverser.Traverser;
import br.ufsc.inf.lapesd.ld_jaxrs.jena.impl.model.JenaModelGraph;
import br.ufsc.inf.lapesd.ld_jaxrs.jena.impl.model.JenaNode;
import br.ufsc.inf.lapesd.ldservice.model.*;
import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.impl.IdentityTransformer;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static br.ufsc.inf.lapesd.ldservice.priv.Utils.schemaP;

@Path("/")
public class LDEndpoint {
    private static Logger logger = LoggerFactory.getLogger(LDEndpoint.class);
    private List<Mapping> mappings = new ArrayList<>();
    private List<UriRewrite> fromPrivate = new ArrayList<>();
    private LinkedHashMap<Activator, Selector> selectors = new LinkedHashMap<>();
    private LinkedHashMap<Activator, Traverser> traversers = new LinkedHashMap<>();
    private LinkedHashMap<Activator, RenderTransformer> transformers = new LinkedHashMap<>();


    public void addMapping(@Nonnull Mapping mapping) {
        fromPrivate.addAll(mapping.getRewrites());
        mapping.getSelectors().forEach(e -> selectors.put(e.getKey(), e.getValue()));
        mapping.getTraversers().forEach(e -> traversers.put(e.getKey(), e.getValue()));
        mapping.getTransformers().forEach(e -> transformers.put(e.getKey(), e.getValue()));
        mappings.add(mapping);
    }

    public List<Mapping> getMappings() {
        return Collections.unmodifiableList(mappings);
    }

    @GET
    @Path("/{subPath:.*}")
    public Model get(@PathParam("subPath") String subPath, @Context UriInfo uriInfo) {
        subPath = "/" + subPath;
        for (Map.Entry<Activator, Selector> e : selectors.entrySet()) {
            Activation activation = tryActivate(e.getKey(), subPath, uriInfo);
            if (activation == null) continue;

            List<Resource> resources;
            try {
                resources = e.getValue().selectResource(activation);
            } catch (Exception ex) {
                logger.error("Selector {} for path {} threw {}", e.getValue(),
                        uriInfo.getAbsolutePath(), ex.getClass(), ex);
                throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
            }

            Model model = ModelFactory.createDefaultModel();
            if (e.getValue().isSingleResource()) {
                Traverser traverser = select(traversers, subPath, uriInfo, new CBDTraverser());
                if (resources.isEmpty()) {
                    logger.debug("get({}): NOT_FOUND (0 results)", subPath);
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                render(resources.get(0), uriInfo, traverser, model);
            } else {
                Traverser traverser = select(traversers, subPath, uriInfo, null);
                renderList(resources, uriInfo, traverser, model);
            }

            return select(transformers, subPath, uriInfo, new IdentityTransformer())
                    .transform(model, e.getValue().isSingleResource());
        }
        logger.debug("get({}): NOT_FOUND (no query)", subPath);
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private <T> T select(Map<Activator, T> map, String subPath, UriInfo uriInfo, T orElse) {
        for (Map.Entry<Activator, T> e : map.entrySet()) {
            Activation activation = tryActivate(e.getKey(), subPath, uriInfo);
            if (activation != null) return e.getValue();
        }
        return orElse;
    }

    private void render(@Nonnull Resource resource, @Nonnull UriInfo uriInfo,
                        @Nonnull Traverser traverser, @Nonnull Model out) {
        traverser.traverse(new JenaModelGraph(resource.getModel()), new JenaNode(resource),
                new JenaModelGraph(out));
        Map<Resource, Resource> replacements = new HashMap<>();
        rewriteUris(out, uriInfo, replacements);
        Resource rwResource = replacements.getOrDefault(resource, resource);

        /* mark newResource as mainEntity */
        Resource newResource = out.createResource(uriInfo.getRequestUri().toString());
        newResource.addProperty(schemaP("mainEntity"), newResource);
        out.add(newResource, OWL2.sameAs, resource);

        /* (resource, p, o) => (newResource, p, o) */
        StmtIterator it = out.listStatements(rwResource, null, (RDFNode) null);
        while (it.hasNext()) {
            Statement s = it.next();
            out.add(newResource, s.getPredicate(), s.getObject());
            it.remove();
        }
        /* (r, p, resource) => (r, p, newResource) */
        it = out.listStatements(null, null, rwResource);
        while (it.hasNext()) {
            Statement s = it.next();
            out.add(s.getSubject(), s.getPredicate(), newResource);
            it.remove();
        }

        /* this must come after the rwResource => newResource replacement */
        out.add(newResource, OWL2.sameAs, rwResource);
    }

    private void renderList(@Nonnull List<Resource> resources, @Nonnull UriInfo uriInfo,
                            @Nullable Traverser traverser, @Nonnull Model out) {
        /* empty list */
        if (resources.isEmpty()) {
            out.add(out.createResource(uriInfo.getRequestUri().toString()), OWL2.sameAs, RDF.nil);
            return;
        }

        /* identify the RDF list as the mainEntity */
        Resource list = out.createResource(uriInfo.getRequestUri().toString());
        list.addProperty(schemaP("mainEntity"), list);

        /* create RDF List with head being reqURI */
        Resource[] node = {list.addProperty(RDF.first, resources.get(0))};
        resources.stream().skip(1).forEach(r -> {
            Resource rest = out.createResource().addProperty(RDF.first, r);
            node[0].addProperty(RDF.rest, rest);
            node[0] = rest;
        });
        node[0].addProperty(RDF.rest, RDF.nil);

        /* Add traversals of list members */
        if (traverser != null) {
            JenaModelGraph dest = new JenaModelGraph(out);
            for (Resource r : resources) {
                JenaModelGraph source = new JenaModelGraph(r.getModel());
                traverser.traverse(source, new JenaNode(r), dest);
            }
        }

        /* rewrite URIs and add sameAs triples */
        Map<Resource, Resource> rewrites = new HashMap<>();
        rewriteUris(out, uriInfo, rewrites);
        for (Resource r : resources) {
            Resource r2 = rewrites.getOrDefault(r, null);
            if (r2 != null)
                out.add(r2, OWL2.sameAs, r);
        }
    }

    @Nullable
    private Activation tryActivate(@Nonnull Activator activator, String subPath, UriInfo uriInfo) {
        String what = null;
        if (activator instanceof PathActivator)
            what = subPath;
        else if (activator instanceof UriActivator)
            what = uriInfo.getRequestUri().toString();

        if (what == null) {
            logger.error("Do not know what to provide to {}.tryActivate()",
                    activator.getClass().getName());
            return null;
        }
        return activator.tryActivate(what);
    }

    private void rewriteUris(Model m, UriInfo uriInfo,
                             @Nullable Map<Resource, Resource> replacements) {
        /* find all resources */
        StmtIterator it = m.listStatements();
        Set<Resource> resources = new HashSet<>();
        while (it.hasNext()) {
            Statement s = it.next();
            Resource r = s.getSubject();
            if (r.isURIResource()) resources.add(r);
            RDFNode node = s.getObject();
            if (node.isURIResource()) resources.add(node.asResource());
        }

        /* create map of replacements */
        String base = uriInfo.getBaseUri().toString().replaceAll("/$", "");
        if (replacements == null)
            replacements = new HashMap<>();
        for (Resource r : resources) {
            String path = fromPrivate.stream().map(u -> u.apply(r.getURI()))
                    .filter(Objects::nonNull).findFirst().orElse(null);
            if (path != null) {
                String newURI = base + (!path.startsWith("/") ? "/" : "") + path;
                replacements.put(r, m.createResource(newURI));
            }
        }

        /* find and remove statements to replace */
        List<List<Statement>> victims = new ArrayList<>();
        for (int i = 0; i < 3; i++) victims.add(new ArrayList<>());
        it = m.listStatements();
        while (it.hasNext()) {
            Statement s = it.next();
            int row = ((replacements.containsKey(s.getSubject()) ? 1 : 0) << 1)
                    | ((s.getObject().isResource()
                        && replacements.containsKey(s.getResource()) ? 1 : 0));
            if (row > 0) {
                victims.get(row - 1).add(s);
                it.remove();
            }
        }

        /* add statement replacements */
        for (Statement s : victims.get(2))
            m.add(replacements.get(s.getSubject()), s.getPredicate(), replacements.get(s.getResource()));
        for (Statement s : victims.get(1))
            m.add(replacements.get(s.getSubject()), s.getPredicate(), s.getObject());
        for (Statement s : victims.get(0))
            m.add(s.getSubject(), s.getPredicate(), replacements.get(s.getResource()));
    }
}
