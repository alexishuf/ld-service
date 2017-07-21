package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import br.ufsc.inf.lapesd.ldservice.tabular.raw.TabularDataSource;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CSVInMemoryDataSource implements TabularDataSource {
    private LinkedHashMap<String, Integer> columnIndex;
    private ArrayList<Row> rows = new ArrayList<>();
    private Map<String, SetMultimap<String, Integer>> indexes = new HashMap<>();

    public CSVInMemoryDataSource(@WillClose @Nonnull CSVParser parser,
                                 @Nonnull Collection<String> columnsToIndex) throws IOException {
        columnIndex = new LinkedHashMap<>();
        parser.getHeaderMap().forEach((key, value) -> columnIndex.put(key, value));
        columnsToIndex.forEach(c -> indexes.put(c, HashMultimap.create()));
        try {
            for (CSVRecord r : parser) {
                ArrayRow row = new ArrayRow(columnIndex, r.iterator());
                rows.add(row);
                indexes.forEach((col, index) ->
                        index.put(row.get(col), (int)r.getRecordNumber()-1));
            }
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            throw e;
        } finally {
            parser.close();
        }
    }

    @Override
    public List<Row> select(@Nonnull Map<String, String> valuesMap) {
        Preconditions.checkArgument(valuesMap.keySet().stream().allMatch(indexes::containsKey));

        /* sort by number of matches in index */
        TreeMap<Integer, Set<Integer>> indexSizeMap = new TreeMap<>();
        valuesMap.forEach((k, v) -> {
            Set<Integer> rows = indexes.get(k).get(v);
            indexSizeMap.put(rows.size(), rows);
        });

        /* joins all indices */
        SortedSet<Integer> matches = null;
        for (Set<Integer> longs : indexSizeMap.values()) {
            if      (matches == null)   matches = new TreeSet<>(longs);
            else if (matches.isEmpty()) break;
            else                        matches.retainAll(longs);
        }
        return matches == null ? Collections.emptyList()
                : matches.stream().map(rows::get).collect(Collectors.toList());
    }
}
