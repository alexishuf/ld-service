package br.ufsc.inf.lapesd.ldservice.tabular;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

public interface TabularSemanticMapping {
    @Nonnull
    Resource map(Row row);
    @Nonnull
    String toColumn(Property property) throws NoSuchElementException;
    @Nonnull
    Property toProperty(String column) throws NoSuchElementException;
}
