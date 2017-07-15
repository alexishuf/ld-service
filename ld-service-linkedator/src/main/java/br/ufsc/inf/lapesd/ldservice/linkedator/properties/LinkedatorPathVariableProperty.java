package br.ufsc.inf.lapesd.ldservice.linkedator.properties;

import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;

import javax.annotation.Nonnull;

/**
 * Linkedator semantic descriptions associate path template variables with properties in an
 * ontology. This object represents such associations.
 */
public class LinkedatorPathVariableProperty implements SelectorProperty {
    private final @Nonnull String variable;
    private final @Nonnull String property;

    public LinkedatorPathVariableProperty(@Nonnull String variable, @Nonnull String property) {
        this.variable = variable;
        this.property = property;
    }

    @Override
    public Object getKey() {
        return getVariable();
    }

    @Nonnull
    public String getVariable() {
        return variable;
    }

    @Nonnull
    public String getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return String.format("{%s}:<%s>", getVariable(), getProperty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkedatorPathVariableProperty that = (LinkedatorPathVariableProperty) o;

        if (!variable.equals(that.variable)) return false;
        return property.equals(that.property);
    }

    @Override
    public int hashCode() {
        int result = variable.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }
}
