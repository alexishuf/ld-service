package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;

import javax.annotation.Nonnull;
import java.util.*;

public class ArrayRow implements Row {
    private int number;
    private final @Nonnull LinkedHashMap<String, Integer> columnIndex;
    private final @Nonnull ArrayList<String> values;

    public ArrayRow(int number, @Nonnull LinkedHashMap<String, Integer> columnIndex,
                    @Nonnull ArrayList<String> values) {
        this.number = number;
        this.columnIndex = columnIndex;
        this.values = values;
    }

    public ArrayRow(int number, @Nonnull LinkedHashMap<String, Integer> columnIndex,
                    @Nonnull Iterator<String> iterator) {
        this.number = number;
        this.columnIndex = columnIndex;
        values = new ArrayList<>(columnIndex.size());
        while (iterator.hasNext())
            values.add(iterator.next());
    }

    @Override
    public int getNumber() {
        return number;
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
