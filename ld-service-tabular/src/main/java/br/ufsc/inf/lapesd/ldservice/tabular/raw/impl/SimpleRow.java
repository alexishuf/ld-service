package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.*;

public class SimpleRow implements Row {
    private LinkedHashMap<String, String> values = new LinkedHashMap<>();
    private int number;

    public SimpleRow(int number, Map<String, String> map) {
        this.number = number;
        map.forEach((key, value) -> values.put(key, value));
    }

    public SimpleRow(int number, Collection<String> keys, Collection<String> values) {
        this.number = number;
        Preconditions.checkArgument(keys.size() == values.size());
        Iterator<String> keyIt = keys.iterator(), valueIt = values.iterator();
        while (keyIt.hasNext()) this.values.put(keyIt.next(), valueIt.next());
    }

    @Override
    public int getNumber() {
        return number;
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
