package br.ufsc.inf.lapesd.ldservice.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface Activator<T> {
    /**
     * Attempts to activate the pattern against text. If text does not match, returns null.
     */
    @Nullable Activation<T> tryActivate(@Nonnull String text);
}
