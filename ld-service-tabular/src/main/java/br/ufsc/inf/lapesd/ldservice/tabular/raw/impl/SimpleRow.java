package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.*;

public class SimpleRow implements Row {
    private LinkedHashMap<String, String> values = new LinkedHashMap<>();

    public SimpleRow(Map<String, String> map) {
        map.forEach((key, value) -> values.put(key, value));
    }

    public SimpleRow(Collection<String> keys, Collection<String> values) {
        Preconditions.checkArgument(keys.size() == values.size());
        Iterator<String> keyIt = keys.iterator(), valueIt = values.iterator();
        while (keyIt.hasNext()) this.values.put(keyIt.next(), valueIt.next());
    }

    @Nonnull
    @Override
    public Collection<String> getColumns() {
        return Collections.unmodifiableSet(values.keySet());
    }

    @Override
    public boolean has(@Nonnull String column) {
        return values.containsKey(column);
    }

    @Nonnull
    @Override
    public String get(@Nonnull String column) {
        if (!has(column)) throw new NoSuchElementException();
        return values.get(column);
    }
}
