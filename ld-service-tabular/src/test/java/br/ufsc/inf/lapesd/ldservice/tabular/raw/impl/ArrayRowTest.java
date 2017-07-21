package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import br.ufsc.inf.lapesd.ldservice.tabular.raw.RowTestBase;

import java.util.*;

public class ArrayRowTest extends RowTestBase {
    @Override
    protected Row createRow(List<String> columns, List<String> values) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) map.put(columns.get(i), i);
        return new ArrayRow(map, new ArrayList<>(values));
    }
}
