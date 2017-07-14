package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link Activation} of an {@link RxActivator}.
 */
public class RxActivation implements Activation<String> {
    private final Matcher matcher;
    private LinkedHashSet<String> vars;

    public RxActivation(@Nonnull RxActivator activator, @Nonnull Matcher matcher) {
        Preconditions.checkArgument(matcher.matches());
        this.matcher = matcher;

        vars = activator.getVarNames().stream().filter(n -> get(matcher, n) != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Nonnull
    @Override
    public List<String> getVarNames() {
        return new ArrayList<>(vars);
    }

    @Override
    public boolean contains(String varName) {
        return vars.contains(varName);
    }

    @Nonnull
    @Override
    public String get(String varName) throws NoSuchElementException {
        String result = getOrDefault(varName, null);
        if (result == null) {
            throw new NoSuchElementException(String.format("No group %s in %s",
                    varName, matcher.pattern().pattern()));
        }
        return result;
    }

    @Override
    public String getOrDefault(String varName, String defaultValue) {
        String result = get(matcher, varName);
        return result == null ? defaultValue : result;
    }

    @Nullable
    private String get(Matcher matcher, String varName) {
        return Pattern.compile("[0-9]+").matcher(varName).matches()
                ? matcher.group(Integer.parseInt(varName))
                : matcher.group(varName);
    }
}
