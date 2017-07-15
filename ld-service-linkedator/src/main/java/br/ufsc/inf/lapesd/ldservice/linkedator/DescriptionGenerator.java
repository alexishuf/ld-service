package br.ufsc.inf.lapesd.ldservice.linkedator;

import br.ufsc.inf.lapesd.ldservice.LDEndpoint;
import br.ufsc.inf.lapesd.ldservice.linkedator.properties.LinkedatorPathVariableProperty;
import br.ufsc.inf.lapesd.ldservice.model.Activator;
import br.ufsc.inf.lapesd.ldservice.model.Mapping;
import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.impl.PathTemplateActivator;
import br.ufsc.inf.lapesd.ldservice.model.properties.ResourceType;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;
import br.ufsc.inf.lapesd.ldservice.model.impl.SPARQLSelector;
import br.ufsc.inf.lapesd.ldservice.model.properties.extractors.ExtractorRegistry;
import br.ufsc.inf.lapesd.linkedator.SemanticMicroserviceDescription;
import br.ufsc.inf.lapesd.linkedator.SemanticResource;
import br.ufsc.inf.lapesd.linkedator.UriTemplate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates a {@link SemanticMicroserviceDescription} object from a given object and
 * {@link Mapping} instances. From Mappings, {@link SelectorProperty} instances are used, and in
 * the case of {@link SPARQLSelector}, the SPARQL query is parsed to extract missing information.
 */
public class DescriptionGenerator {
    private final WeakHashMap<Selector, Set<LinkedatorPathVariableProperty>>
            linkedatorPropertiesCache = new WeakHashMap<>();
    private final WeakHashMap<Selector, Set<ResourceType>>
            typePropertiesCache = new WeakHashMap<>();
    private SemanticMicroserviceDescription description = new SemanticMicroserviceDescription();
    private HashMap<String, SemanticResource> srMap = new HashMap<>();

    public void overloadDescription(SemanticMicroserviceDescription description) {
        if (description.getIpAddress() != null)
            this.description.setIpAddress(description.getIpAddress());
        if (description.getIpAddress() != null || description.getServerPort() != null)
            this.description.setServerPort(description.getServerPort());
        if (description.getUriBase() != null)
            this.description.setUriBase(description.getUriBase());
        if (description.getOntologyBase64() != null)
            this.description.setOntologyBase64(description.getOntologyBase64());

        for (SemanticResource sr : description.getSemanticResources()) {
            SemanticResource osr = srMap.getOrDefault(sr.getEntity(), null);
            if (osr == null) {
                this.description.getSemanticResources().add(sr);
                srMap.put(sr.getEntity(), sr);
            } else {
                SortedSet<String> properties = new TreeSet<>();
                properties.addAll(osr.getProperties());
                properties.addAll(sr.getProperties());
                osr.setProperties(new ArrayList<>(properties));

                for (UriTemplate ut : sr.getUriTemplates()) {
                    String utMethod = ut.getMethod() != null ? ut.getMethod() : "GET";
                    UriTemplate out = osr.getUriTemplates().stream()
                            .filter(u -> {
                                String uMethod = u.getMethod() != null ? u.getMethod() : "GET";
                                return uMethod.equals(utMethod)
                                        && u.getUri().equals(ut.getUri());
                            })
                            .findFirst().orElse(null);
                    if (out == null) {
                        osr.getUriTemplates().add(ut);
                    } else {
                        for (String param : ut.getParameters().keySet())
                            out.getParameters().put(param, ut.getParameters().get(param));
                    }
                }
            }
        }
    }

    public void addMapping(Mapping mapping) {
        overloadDescription(toPartialDescription(mapping));
    }

    public void addMappings(LDEndpoint endpoint) {
        List<Mapping> mappings = endpoint.getMappings();
        ListIterator<Mapping> it = mappings.listIterator(mappings.size());
        while (it.hasPrevious()) addMapping(it.previous());
    }

    public SemanticMicroserviceDescription getDescription() {
        return description;
    }

    public SemanticMicroserviceDescription toPartialDescription(Mapping mapping) {
        SemanticMicroserviceDescription smd = new SemanticMicroserviceDescription();
        HashMap<String, SemanticResource> localSRMap = new HashMap<>();

        ArrayList<Map.Entry<Activator<?>, Selector>> reversedSelectors;
        reversedSelectors = new ArrayList<>(mapping.getSelectors());
        Collections.reverse(reversedSelectors);

        for (Map.Entry<Activator<?>, Selector> entry : reversedSelectors) {
            Activator<?> activator = entry.getKey();
            if (!(activator instanceof PathTemplateActivator))
                continue;
            String template = ((PathTemplateActivator) activator).getPathTemplate();

            Selector selector = entry.getValue();
            /* {var} => http://ex.org/ns#property */
            Map<String, String> parameters = new HashMap<>();
            activator.getVarNames()
                    .forEach(name -> {
                        String property = findVarProperty(name, selector);
                        if (property != null) //do not add nulls!
                            parameters.put(name, property);
                    });

            for (String type : findResourceType(selector)) {
                SemanticResource sr = localSRMap.getOrDefault(type, null);
                boolean isSRNew = sr == null;
                if (isSRNew) {
                    sr = new SemanticResource();
                    sr.setEntity(type);
                }

                UriTemplate ut = findUriTemplate(sr, template);
                boolean isUTNew = ut == null;
                if (isUTNew) {
                    ut = new UriTemplate();
                    ut.setMethod("GET");
                    ut.setUri(template);
                }

                UriTemplate old = findUriTemplate(description, type, template);
                Map<String, String> merged = new HashMap<>();
                if (old != null)
                    merged.putAll(old.getParameters());
                merged.putAll(ut.getParameters());
                merged.putAll(parameters);

                if (merged.keySet().containsAll(activator.getVarNames())) {
                    ut.setParameters(merged);
                    if (isUTNew)
                        sr.getUriTemplates().add(ut);
                    if (isSRNew) {
                        smd.getSemanticResources().add(sr);
                        localSRMap.put(type, sr);
                    }
                }
            }
        }

        return smd;
    }

    private UriTemplate findUriTemplate(SemanticMicroserviceDescription smd, String entity,
                                        String template) {
        for (SemanticResource sr : smd.getSemanticResources()) {
            if (!sr.getEntity().equals(entity))
                continue;
            UriTemplate ut = findUriTemplate(sr, template);
            if (ut != null) return ut;
        }
        return null;
    }

    private UriTemplate findUriTemplate(SemanticResource sr, String template) {
        for (UriTemplate ut : sr.getUriTemplates()) {
            String utMethod = ut.getMethod() == null ? "GET" : ut.getMethod();
            if (ut.getUri().equals(template) && utMethod.equals("GET"))
                return ut;
        }
        return null;
    }

    @Nullable
    private String findVarProperty(String name, Selector selector) {
        final Class<LinkedatorPathVariableProperty> clazz = LinkedatorPathVariableProperty.class;

        Set<LinkedatorPathVariableProperty> properties;
        properties = linkedatorPropertiesCache.getOrDefault(selector, null);
        if (properties == null) {
            properties = ExtractorRegistry.get().extractAndMerge(clazz, selector);
            properties = properties == null ? Collections.emptySet() : properties;
            linkedatorPropertiesCache.put(selector, properties);
        }

        return properties.stream().filter(p -> p.getVariable().equals(name))
                .map(LinkedatorPathVariableProperty::getProperty)
                .findFirst().orElse(null);
    }

    private List<String> findResourceType(Selector selector) {
        Set<ResourceType> set = typePropertiesCache.getOrDefault(selector, null);
        if (set == null) {
            set = ExtractorRegistry.get().extractAndMerge(ResourceType.class, selector);
            set = set == null ? Collections.emptySet() : set;
            typePropertiesCache.put(selector, set);
        }
        return set.stream().filter(ResourceType::isCertain).map(ResourceType::getURI)
                .collect(Collectors.toList());
    }
}
