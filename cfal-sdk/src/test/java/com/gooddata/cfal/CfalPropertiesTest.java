/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Before;
import org.junit.Test;

public class CfalPropertiesTest {
    private CfalProperties instance;

    @Before
    public void setUp() throws Exception {
        this.instance = new CfalProperties();
    }

    @Test
    public void workingOK() throws Exception {
        instance.setCfalDir(null);
        instance.setComponent("component-1");

        for (CfalProperties.CfalServiceType service : CfalProperties.CfalServiceType.values()) {
            instance.setServiceType(service);
        }

        instance.setEnabled(false);
        instance.setEnabled(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComponentEmpty() throws Exception {
        instance.setComponent("   ");
    }

    @Test(expected = NullPointerException.class)
    public void testComponentNull() throws Exception {
        instance.setComponent(null);
    }

    @Test(expected = NullPointerException.class)
    public void testServiceNull() throws Exception {
        instance.setServiceType(null);
    }

    @Test
    public void testCfalDir() throws Exception {
        instance.setCfalDir(null);
        instance.setCfalDir("");
        instance.setCfalDir("     ");
        instance.setCfalDir("/meh");
    }
}
