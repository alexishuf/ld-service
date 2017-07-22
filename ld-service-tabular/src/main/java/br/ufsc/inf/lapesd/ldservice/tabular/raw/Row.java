package br.ufsc.inf.lapesd.ldservice.tabular.raw;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;

public interface Row extends Iterable<String> {
    /**
     * Gets the row number, which starts at zero.
     */
    int getNumber();

    @Nonnull
    Collection<String> getColumns();
    boolean has(@Nonnull String column);
    @Nonnull String get(@Nonnull String column);

    @Nonnull
    @Override
    default Iterator<String> iterator() {
        return new Iterator<String>() {
            Iterator<String> columnIt = getColumns().iterator();

            @Override
            public boolean hasNext() {
                return columnIt.hasNext();
            }

            @Override
            public String next() {
                return get(columnIt.next());
            }
        };
    }
}
