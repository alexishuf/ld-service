package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import org.apache.commons.collections4.collection.UnmodifiableCollection;

import javax.annotation.Nonnull;
import java.util.*;

public class ArrayRow implements Row {
    private final @Nonnull LinkedHashMap<String, Integer> columnIndex;
    private final @Nonnull ArrayList<String> values;

    public ArrayRow(@Nonnull LinkedHashMap<String, Integer> columnIndex,
                    @Nonnull ArrayList<String> values) {
        this.columnIndex = columnIndex;
        this.values = values;
    }

    public ArrayRow(@Nonnull LinkedHashMap<String, Integer> columnIndex,
                    @Nonnull Iterator<String> iterator) {
        this.columnIndex = columnIndex;
        values = new ArrayList<>(columnIndex.size());
        while (iterator.hasNext())
            values.add(iterator.next());
    }

    @Nonnull
    @Override
    public Collection<String> getColumns() {
        return columnIndex.keySet();
    }

    @Override
    public boolean has(@Nonnull String column) {
        return columnIndex.containsKey(column);
    }

    @Nonnull
    @Override
    public String get(@Nonnull String column) {
        if (!has(column)) throw new NoSuchElementException(column);
        int idx = columnIndex.get(column);
        if (idx >= values.size()) throw new NoSuchElementException(column);
        return values.get(idx);
    }
}
