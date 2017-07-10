package br.ufsc.inf.lapesd.ldservice.model;

import br.ufsc.inf.lapesd.ldservice.model.impl.ActivationHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UriRewrite {
    private Activator<String> activator;
    private String replacement;

    public UriRewrite(@Nonnull Activator<String> activator, @Nonnull String replacement) {
        this.activator = activator;
        this.replacement = replacement;
    }

    public @Nonnull Activator getActivator() {
        return activator;
    }

    public @Nonnull String getReplacement() {
        return replacement;
    }

    public String applyOrDefault(@Nonnull String original, String defaultValue) {
        Activation activation = getActivator().tryActivate(original);
        if (activation == null) return defaultValue;
        return ActivationHelper.replace(replacement, activation);
    }

    public @Nullable String apply(@Nonnull String original) {
        return applyOrDefault(original, null);
    }
}
