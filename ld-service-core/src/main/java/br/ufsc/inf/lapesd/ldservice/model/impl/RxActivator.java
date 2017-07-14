package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Activator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link Activator} based on a {@link Pattern}.
 */
public class RxActivator implements Activator<String> {
    private final @Nonnull Pattern pattern;
    private List<String> vars;
    private static Pattern namedGroupsRx = Pattern.compile("[^\\\\]\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    public RxActivator(@Nonnull Pattern pattern) {
        this.pattern = pattern;

        /* indexing of capture groups */
        vars = new ArrayList<>();
        vars.add("0");
        for (int i = 0; i < pattern.matcher("").groupCount(); i++)
            vars.add(String.valueOf(i + 1));

        /* named capture groups */
        Matcher matcher = namedGroupsRx.matcher(" " + pattern.pattern());
        while (matcher.find())
            vars.add(matcher.group(1));

        vars = Collections.unmodifiableList(vars);
    }
    public RxActivator(@Nonnull String pattern) {
        this(Pattern.compile(pattern));
    }

    @Nullable
    @Override
    public RxActivation tryActivate(@Nonnull String text) {
        Matcher matcher = this.pattern.matcher(text);
        return !matcher.matches() ? null : new RxActivation(this, matcher);
    }

    /**
     * Get the all variable names that could possibly be used with an {@link Activation}.
     *
     * This list has the same order as groups appear in the regexp. The list has the following
     * sequence:
     * <ol>
     *     <li>Group "0" which corresponds to the whole input string</li>
     *     <li>Groups "1"-"n" which correspond to (named and unamed) capture groups by index.</li>
     *     <li>Named capture groups, in order of appearance</li>
     * </ol>
     */
    @Nonnull
    @Override
    public List<String> getVarNames() {
        return vars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RxActivator that = (RxActivator) o;

        return pattern.equals(that.pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    @Override
    public String toString() {
        return String.format("/%s/", pattern);
    }
}
