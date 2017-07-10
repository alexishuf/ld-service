package br.ufsc.inf.lapesd.ldservice.model;

import javax.annotation.Nonnull;
import java.util.List;

public interface Activation<T> {
    @Nonnull List<String> getVarNames();
    @Nonnull T get(String varName);
}
