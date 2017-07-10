package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Activator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link Activator} based on a {@link Pattern}.
 */
public class RxActivator implements Activator<String> {
    private final @Nonnull Pattern pattern;

    public RxActivator(@Nonnull Pattern pattern) {
        this.pattern = pattern;
    }
    public RxActivator(@Nonnull String pattern) {
        this(Pattern.compile(pattern));
    }

    @Nullable
    @Override
    public RxActivation tryActivate(@Nonnull String text) {
        Matcher matcher = this.pattern.matcher(text);
        return !matcher.matches() ? null : new RxActivation(matcher);
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
