package br.ufsc.inf.lapesd.ldservice.tabular;

import org.apache.jena.rdf.model.Property;

import java.util.HashSet;
import java.util.Set;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;

public class TabularConstants {
    public static final Property row = createProperty("http://github.com/alexishuf/ld-service/tabular/row");
    public static final Set<Property> magicProperties;

    static {
        magicProperties = new HashSet<>();
        magicProperties.add(row);
    }

    public static Property asMagicProperty(String column) {
        return magicProperties.stream()
                .filter(p -> p.getURI().equals(column)).findFirst().orElse(null);
    }
}
