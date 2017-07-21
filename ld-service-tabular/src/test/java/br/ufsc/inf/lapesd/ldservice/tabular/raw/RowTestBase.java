package br.ufsc.inf.lapesd.ldservice.tabular.raw;

import br.ufsc.inf.lapesd.ldservice.tabular.raw.Row;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public abstract class RowTestBase {

    @DataProvider
    public static Object[][] columnsData() {
        return new Object[][] {
                {asList("a", "b")},
                {asList("c", "b", "a")},
                {asList("z", "1")},
                {Collections.singletonList("z")},
                {Collections.emptyList()}
        };
    }

    @DataProvider
    public static Object[][] valuesData() {
        return new Object[][] {
                {Collections.emptyList(), Collections.emptyList()},
                {Collections.singletonList("a"), Collections.singletonList("1")},
                {asList("z", "a"), asList("z1", "a1")},
                {asList("1", "a"), asList("4", "x9")},
        };
    }

    protected abstract Row createRow(List<String> columns, List<String> values);

    @Test(dataProvider = "columnsData")
    public void testColumns(List<String> columns) {
        Row row = createRow(columns, IntStream.range(0, columns.size())
                .mapToObj(String::valueOf).collect(Collectors.toList()));
        Assert.assertEquals(new ArrayList<>(row.getColumns()), columns);
        Assert.assertTrue(columns.stream().allMatch(row::has));
    }

    @Test(dataProvider = "valuesData")
    public void testValues(List<String> columns, List<String> values) {
        Row row = createRow(columns, values);
        Assert.assertEquals(columns.size(), values.size());

        List<String> actual = columns.stream().map(row::get).collect(Collectors.toList());
        Assert.assertEquals(actual, values);

        actual = new ArrayList<>();
        row.iterator().forEachRemaining(actual::add);
        Assert.assertEquals(actual, values);
    }
}
