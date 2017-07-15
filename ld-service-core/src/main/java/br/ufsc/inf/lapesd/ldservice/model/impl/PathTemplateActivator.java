package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.PathActivator;
import org.glassfish.jersey.uri.PathTemplate;

import javax.annotation.Nonnull;

/**
 * An activator for RFC 6570 templates applied against path segments.
 */
public class PathTemplateActivator extends UriTemplateActivatorImpl implements PathActivator {
    public PathTemplateActivator(@Nonnull String pathTemplate) {
        super(new PathTemplate(pathTemplate));
    }

    @Nonnull
    public String getPathTemplate() {
        return uriTemplate.getTemplate();
    }
}
