/*
 * Copyright (c) 2012 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.systemservices;

import com.emc.storageos.coordinator.exceptions.DecodingException;
import com.emc.storageos.coordinator.client.model.ConfigVersion;
import org.junit.Assert;
import org.junit.Test;

public class ConfigVersionTest {
    @Test
    public void testEncode() {
        {
            ConfigVersion cf = new ConfigVersion();
            Assert.assertTrue("".equals(cf.encodeAsString()));
        }

        {
            ConfigVersion cf1 = new ConfigVersion("");
            Assert.assertTrue("".equals(cf1.encodeAsString()));

            ConfigVersion cf2 = new ConfigVersion("abc");
            Assert.assertTrue("abc".equals(cf2.encodeAsString()));
        }
    }

    @Test
    public void testDecode() {
        {
            ConfigVersion cf = new ConfigVersion();
            ConfigVersion cf1 = null;
            try {
                cf1 = cf.decodeFromString(cf.encodeAsString());
                Assert.assertTrue(cf1.getConfigVersion() == null);
            } catch (DecodingException e) {
                Assert.assertTrue(false);
            }
        }

        {
            ConfigVersion cf = new ConfigVersion("");
            ConfigVersion cf1 = null;
            try {
                cf1 = cf.decodeFromString(cf.encodeAsString());
                Assert.assertTrue(cf1.getConfigVersion() == null);
            } catch (DecodingException e) {
                Assert.assertTrue(false);
            }
        }

        {
            ConfigVersion cf = new ConfigVersion("abc");
            ConfigVersion cf1 = null;
            try {
                cf1 = cf.decodeFromString(cf.encodeAsString());
                Assert.assertTrue("abc".equals(cf1.getConfigVersion()));
            } catch (DecodingException e) {
                Assert.assertTrue(false);
            }
        }
    }

}
