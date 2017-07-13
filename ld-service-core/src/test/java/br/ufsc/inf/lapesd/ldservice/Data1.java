package br.ufsc.inf.lapesd.ldservice;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.InputStream;

public class Data1 {
    public static String A_NS = "http://a.example.org/ns#";
    public static String B_NS = "http://b.example.org/ns#";
    public static String S_NS = "http://s.example.org/ns#";

    public static Model load() {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = Data1.class.getClassLoader().getResourceAsStream("data-1.ttl");
        RDFDataMgr.read(model, in, Lang.TURTLE);
        return model;
    }
}
