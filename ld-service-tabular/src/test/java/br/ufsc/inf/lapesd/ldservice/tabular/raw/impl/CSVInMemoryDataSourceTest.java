package br.ufsc.inf.lapesd.ldservice.tabular.raw.impl;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CSVInMemoryDataSourceTest {

    private CSVParser resourceCSV(@Nonnull String path, @Nonnull CSVFormat format) throws IOException {
        format = format.withHeader();
        InputStream in = getClass().getResourceAsStream("/tabular-test/" + path);
        return new CSVParser(new InputStreamReader(in, StandardCharsets.UTF_8), format);
    }

    @Test
    public void testSimpleOneKey() throws IOException {
        CSVInMemoryDataSource source = new CSVInMemoryDataSource(
                resourceCSV("simple.csv", CSVFormat.DEFAULT),
                Collections.singletonList("col7A"));
        List<Row> rows = source.select(ImmutableMap.<String, String>builder()
                .put("col7A", "v3A").build());
        Assert.assertEquals(rows.size(), 1);

        List<String> columns = Arrays.asList("col7A", "col4B", "col3C", "col9D");
        Row row = rows.get(0);
        Assert.assertEquals(row.getColumns(), columns);
        Assert.assertTrue(columns.stream().allMatch(row::has));
        Assert.assertEquals(row.get("col7A"), "v3A");
        Assert.assertEquals(row.get("col4B"), "v3B");
        Assert.assertEquals(row.get("col3C"), "v3C");
        Assert.assertEquals(row.get("col9D"), "v3D");
    }

    @Test
    public void testSimpleTwoKeys() throws IOException {
        CSVInMemoryDataSource source = new CSVInMemoryDataSource(
                resourceCSV("simple.csv", CSVFormat.DEFAULT),
                Arrays.asList("col7A", "col3C"));
        List<Row> rows = source.select(ImmutableMap.<String, String>builder()
                .put("col7A", "v1A").put("col3C", "v1C").build());
        Assert.assertEquals(rows.size(), 1);

        List<String> columns = Arrays.asList("col7A", "col4B", "col3C", "col9D");
        Row row = rows.get(0);
        Assert.assertEquals(row.getColumns(), columns);
        Assert.assertTrue(columns.stream().allMatch(row::has));
        Assert.assertEquals(row.get("col7A"), "v1A");
        Assert.assertEquals(row.get("col4B"), "v1B");
        Assert.assertEquals(row.get("col3C"), "v1C");
        Assert.assertEquals(row.get("col9D"), "v1D");
    }

    @Test
    public void testCompoundKeyList() throws IOException {
        CSVInMemoryDataSource source = new CSVInMemoryDataSource(
                resourceCSV("compound-key.csv", CSVFormat.DEFAULT),
                Arrays.asList("a", "b", "c"));

        List<Row> rows = source.select(ImmutableMap.<String, String>builder().put("a", "1")
                .put("b", "2").build());
        Assert.assertEquals(rows.size(), 2);
        Assert.assertEquals(rows.stream().map(r -> r.get("c")).collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList("4", "2")));
    }

    @Test
    public void testCompoundKeySingle() throws IOException {
        CSVInMemoryDataSource source = new CSVInMemoryDataSource(
                resourceCSV("compound-key.csv", CSVFormat.DEFAULT),
                Arrays.asList("a", "b", "c"));

        List<Row> rows = source.select(ImmutableMap.<String, String>builder().put("a", "1")
                .put("b", "2").put("c", "2").build());
        Assert.assertEquals(rows.size(), 1);
        Row row = rows.get(0);
        Assert.assertEquals(row.get("a"), "1");
        Assert.assertEquals(row.get("b"), "2");
        Assert.assertEquals(row.get("c"), "2");
    }
}