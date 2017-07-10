package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UriTemplateActivatorTest {
    @Test
    public void testMatch() {
        UriTemplateActivator activator = new UriTemplateActivator("http://example.org/{res}");
        Activation<String> activation = activator.tryActivate("http://example.org/res1");
        Assert.assertNotNull(activation);
        Assert.assertEquals(activation.get("res"), "res1");
    }

    @Test
    public void testNoMatch() {
        UriTemplateActivator activator = new UriTemplateActivator("http://example.org/{res}");
        Activation<String> activation = activator.tryActivate("http://example.com/res1");
        Assert.assertNull(activation);
    }

    @Test
    public void testMatchPath() {
        UriTemplateActivator activator = new UriTemplateActivator("http://example.org/{res}");
        Activation<String> activation = activator.tryActivate("/res1");
        Assert.assertNull(activation);
    }
}
