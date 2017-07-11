# ld-service
A generic JAX-RS endpoint for serving linked data.
 
## Status
This project is still under development, to use it install it locally:
```bash
mvn clean install
```

## Using
Include as a dependency:
```xml
<dependency>
  <groupId>br.ufsc.inf.lapesd.ld-jax-rs</groupId>
  <artifactId>ld-jax-rs-jena</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

Create a LDEndpoint instance and add it to you `Application` subclass. The recommended way is using the `Mapping.Builder` as such:
```java
LDEndpoint endpoint = new LDEndpoint();
endpoint.addMapping(new Mapping.Builder()
        .addRewrite(new UriRewrite(new RxActivator("http://a.example.org/ns#(.*)"), "/a/${1}"))
        .addSelector(new PathTemplateActivator("by_id/{id}"),
                new SPARQLSelector(model, true,
                PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p foaf:account ?a.\n" +
                        "  ?a foaf:accountName \"${id}\"." +
                        "  ?a foaf:accountServiceHomePage <http://a.example.org/>." +
                        "}"))
        .addSelector(new PathTemplateActivator("list"),
                new SPARQLSelector(model, false,
                PREFIXES +
                        "SELECT ?p WHERE {\n" +
                        "  ?p foaf:account ?x.\n" +
                        "  ?x foaf:accountServiceHomePage <http://a.example.org/>.\n" +
                        "}"))
        .addTraverser(new PathTemplateActivator("list"),
                new CBDTraverser().setMaxPath(0))
        .addTransformer(new PathTemplateActivator("by_id/{id}"),
                new JenaReasoningTransformer(reasoner))
        .build());
```

The endpoint exposes data according to one or more `Mapping` instances. Each `Mapping` is simply and aggregation of the following types of objects:

- `UriRewrite` specifies how to dynamically change URIs of the data to URIs relative to the endpoint deployment 
- `Selector` specifies how to obtain a resource (or a list of)   
- `Traverser` specifies how to compute the set of triples that describe a resource
- `Transformer` performs arbitrary transformations on the served triples at the final stage  

All these, but `UriRewrite` are accompanied by an `Activator` object that is used to relate the use of these instances to URI requests. The configuration above can be read as:
 
- When a request to `by_id/{id}` is done, find the first RDF resource whose `account`'s `accountName` is `{id}`
- When a request ot `list` is done, find all resources with an  `account` that has http://a.example.org/ as `accountServiceHomePage`
- For resource members of `list`, add only their properties and values (without recursing further into blank nodes) as their description (the default for lists is no member description at all) 
- For the description returned in `by_id{id}` add perform reasoning using the given `reasoner` and send the description's entailment as result to the clients
