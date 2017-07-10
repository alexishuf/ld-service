package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;

/**
 * {@link Activation} of an {@link RxActivator}.
 */
public class RxActivation implements Activation<String> {
    private final Matcher matcher;
    private List<String> vars;

    public RxActivation(@Nonnull Matcher matcher) {
        Preconditions.checkArgument(matcher.matches());
        this.matcher = matcher;
        List<String> names = new ArrayList<>();
        names.add("0");
        for (int i = 0; i < matcher.groupCount(); i++) names.add(String.valueOf(i + 1));
        vars = Collections.unmodifiableList(names);
    }

    @Nonnull
    @Override
    public List<String> getVarNames() {
        return vars;
    }

    @Nonnull
    @Override
    public String get(String varName) {
        int i = Integer.parseInt(varName);
        if (i < 0 || i > vars.size()) {
            throw new NoSuchElementException(String.format("No group %d in %s",
                    i, matcher.pattern().pattern()));
        }
        return matcher.group(i);
    }
}
