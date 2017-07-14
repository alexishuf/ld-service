package br.ufsc.inf.lapesd.ldservice.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Activator<T> {
    /**
     * Attempts to activate the pattern against text. If text does not match, returns null.
     */
    @Nullable Activation<T> tryActivate(@Nonnull String text);

    /**
     * Gets all variable names which an {@link Activation} of this {@link Activator} may contain.
     *
     * There is no guarantee that a name in this list will be present in an {@link Activation}.
     * Names are ordered in a stable, sensible sequence for the activator. For template-based
     * activators, the order should be consistent with the order (left-to-right) in which
     * variables appear in the template.
     */
    @Nonnull
    List<String> getVarNames();
}
