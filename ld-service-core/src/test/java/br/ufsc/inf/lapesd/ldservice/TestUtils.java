package br.ufsc.inf.lapesd.ldservice;

import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import static br.ufsc.inf.lapesd.ldservice.priv.Utils.schemaP;

public class TestUtils {
    public static String PREFIXES = "PREFIX foaf: <" + FOAF.getURI() + ">\n" +
            "PREFIX xsd: <" + XSD.getURI() + ">\n" +
            "PREFIX rdf: <" + RDF.getURI() + ">\n" +
            "PREFIX sh: <" + schemaP("") + ">\n";
}
