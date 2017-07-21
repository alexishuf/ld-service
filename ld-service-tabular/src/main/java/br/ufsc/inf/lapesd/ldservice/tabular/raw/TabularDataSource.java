package br.ufsc.inf.lapesd.ldservice.tabular.raw;

import java.util.List;
import java.util.Map;

public interface TabularDataSource {
    List<Row> select(Map<String, String> valuesMap);
}
