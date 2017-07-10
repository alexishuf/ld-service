package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import br.ufsc.inf.lapesd.ldservice.model.Activator;
import br.ufsc.inf.lapesd.ldservice.model.UriActivator;
import org.glassfish.jersey.uri.UriTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;

/**
 * An activator for RFC 6570 templates
 */
public class UriTemplateActivator extends UriTemplateActivatorImpl implements UriActivator {
    public UriTemplateActivator(@Nonnull String uriTemplate) {
        super(new UriTemplate(uriTemplate));
    }

}
