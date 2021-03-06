package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Activator;
import org.glassfish.jersey.uri.UriTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Implements an activator using {@link UriTemplate}
 */
class UriTemplateActivatorImpl implements Activator<String> {
    protected final @Nonnull
    UriTemplate uriTemplate;

    public UriTemplateActivatorImpl(@Nonnull UriTemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    @Nullable
    @Override
    public Activation<String> tryActivate(@Nonnull String text) {
        if (uriTemplate.toString().endsWith("/") && !text.endsWith("/"))
            text = text + "/";
        if (!uriTemplate.toString().endsWith("/") && text.endsWith("/"))
            text = text.substring(0, text.length()-1);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        return uriTemplate.match(text, map) ? new MapActivation<>(this, map) : null;
    }

    @Nonnull
    @Override
    public List<String> getVarNames() {
        return uriTemplate.getTemplateVariables();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UriTemplateActivator that = (UriTemplateActivator) o;

        return uriTemplate.equals(that.uriTemplate);
    }

    @Override
    public int hashCode() {
        return uriTemplate.hashCode();
    }

    @Override
    public String toString() {
        return String.format("UriTemplateActivator(%s)", uriTemplate.toString());
    }
}
