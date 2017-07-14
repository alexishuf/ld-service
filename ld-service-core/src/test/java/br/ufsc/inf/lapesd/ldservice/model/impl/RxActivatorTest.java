package br.ufsc.inf.lapesd.ldservice.model.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Arrays.asList;

@Test
public class RxActivatorTest {

    private RxActivator twoCaptures, namedCapture, optNamedCapture;

    @BeforeClass
    public void setUp() {
        twoCaptures = new RxActivator( "a/([^/]*)/b/([^/]*)");
        namedCapture = new RxActivator("a/([^/]*)/b/(?<two>[^/]*)");
        optNamedCapture = new RxActivator("a/([^/]*)/(b/(?<two>[^/]*))?");
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
    public void testGetIndexedVarNames() {
        Assert.assertEquals(twoCaptures.getVarNames(), asList("0", "1", "2"));
    }

    @Test
    public void testGetNamedVarNames() {
        Assert.assertEquals(namedCapture.getVarNames(), asList("0", "1", "2", "two"));
    }

    @Test
    public void testGetOptNamedVarNames() {
        Assert.assertEquals(optNamedCapture.getVarNames(), asList("0", "1", "2", "3", "two"));
    }

    @Test
    public void testGetIndexedVarNamesAllMatch() {
        RxActivation activation = twoCaptures.tryActivate("a/val1/b/val2");
        Assert.assertNotNull(activation);
        Assert.assertEquals(activation.getVarNames(), asList("0", "1", "2"));
    }

    @Test
    public void testGetNamedVarNamesAllMatch() {
        RxActivation activation = namedCapture.tryActivate("a/val1/b/val2");
        Assert.assertNotNull(activation);
        Assert.assertEquals(activation.getVarNames(), asList("0", "1", "2", "two"));
    }

    @Test
    public void testGetNamedVarNamesNoMatch() {
        RxActivation activation = optNamedCapture.tryActivate("a/val1/");
        Assert.assertNotNull(activation);
        Assert.assertEquals(activation.getVarNames(), asList("0", "1"));
    }

    @Test
    public void testGetGroups() {
        RxActivation activation = twoCaptures.tryActivate("a/val1/b/val2");
        Assert.assertNotNull(activation);
        Assert.assertEquals(activation.get("1"), "val1");
        Assert.assertEquals(activation.get("2"), "val2");
    }
}
