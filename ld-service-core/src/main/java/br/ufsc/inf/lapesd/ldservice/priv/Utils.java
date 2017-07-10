package br.ufsc.inf.lapesd.ldservice.priv;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class Utils {
    private static String SH = "http://schema.org/";

    public static Resource schemaR(String localName) {
        return ResourceFactory.createResource(SH + localName);
    }
    public static Property schemaP(String localName) {
        return ResourceFactory.createProperty(SH + localName);
    }
}
