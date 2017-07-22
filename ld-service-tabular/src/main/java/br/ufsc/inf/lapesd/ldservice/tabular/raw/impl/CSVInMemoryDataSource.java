package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.TabularConstants;
import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import br.ufsc.inf.lapesd.ldservice.tabular.raw.TabularDataSource;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
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
                ArrayRow row = new ArrayRow((int)r.getRecordNumber()-1, columnIndex, r.iterator());
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
        indexes.put(TabularConstants.row.getURI(), new RowIndex());
    }

    @Override
    public List<Row> select(@Nonnull Map<String, String> valuesMap) {
        Preconditions.checkArgument(valuesMap.keySet().stream().allMatch(indexes::containsKey));

        /* special case: no filter */
        if (valuesMap.isEmpty())
            return rows;

        /* sort by number of matches in index */
        ArrayList<Set<Integer>> setsBySize = new ArrayList<>();
        valuesMap.forEach((k, v) -> setsBySize.add(indexes.get(k).get(v)));
        setsBySize.sort(Comparator.comparing(Set::size));

        /* joins all indices */
        SortedSet<Integer> matches = null;
        for (Set<Integer> longs : setsBySize) {
            if      (matches == null)   matches = new TreeSet<>(longs);
            else if (matches.isEmpty()) break;
            else                        matches.retainAll(longs);
        }
        return matches == null ? Collections.emptyList()
                : matches.stream().map(rows::get).collect(Collectors.toList());
    }

    private static class RowIndex implements SetMultimap<String, Integer> {
        @Override
        public Set<Integer> get(@Nullable String s) {
            return s == null ? Collections.emptySet()
                    : Collections.singleton(Integer.parseInt(s));
        }

        @Override
        public Set<Integer> removeAll(@Nullable Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Integer> replaceValues(String s, Iterable<? extends Integer> iterable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Map.Entry<String, Integer>> entries() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Collection<Integer>> asMap() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(@Nullable Object o) {
            return o != null && (o instanceof String)
                    && Pattern.matches("\\d+", (String)o);
        }

        @Override
        public boolean containsValue(@Nullable Object o) {
            return containsKey(o);
        }

        @Override
        public boolean containsEntry(@Nullable Object o, @Nullable Object o1) {
            return Objects.equals(o, o1) && containsKey(o);
        }

        @Override
        public boolean put(@Nullable String s, @Nullable Integer integer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(@Nullable Object o, @Nullable Object o1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean putAll(@Nullable String s, Iterable<? extends Integer> iterable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean putAll(Multimap<? extends String, ? extends Integer> multimap) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Multiset<String> keys() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Integer> values() {
            throw new UnsupportedOperationException();
        }
    }
}
