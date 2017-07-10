package br.ufsc.inf.lapesd.ldservice.model.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

@Test
public class RxActivatorTest {

    private RxActivator twoCaptures;

    @BeforeMethod
    public void setUp() {
        twoCaptures = new RxActivator("a/([^/]*)/b/([^/]*)");
    }

    @Test
    public void testNonMatch() {
        Assert.assertNull(twoCaptures.tryActivate("asd"));
    }

    @Test
    public void testMatch() {
        Assert.assertNotNull(twoCaptures.tryActivate("a/val1/b/val2"));
    }

    @Test
    public void testGetVarNames() {
        RxActivation activation = twoCaptures.tryActivate("a/val1/b/val2");
        Assert.assertNotNull(activation);
        Assert.assertEquals(activation.getVarNames(), Arrays.asList("0", "1", "2"));
    }

    @Test
    public void testGetGroups() {
        RxActivation activation = twoCaptures.tryActivate("a/val1/b/val2");
        Assert.assertNotNull(activation);
        Assert.assertEquals(activation.get("1"), "val1");
        Assert.assertEquals(activation.get("2"), "val2");
    }
}
