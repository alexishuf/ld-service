package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import br.ufsc.inf.lapesd.ldservice.tabular.raw.RowTestBase;

import java.util.List;

public class SimpleRowTest extends RowTestBase {
    @Override
    protected Row createRow(int number, List<String> columns, List<String> values) {
        return new SimpleRow(number, columns, values);
    }
}
