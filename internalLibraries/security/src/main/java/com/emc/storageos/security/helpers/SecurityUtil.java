/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.security.helpers;

import java.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtil {

    private static Logger log = LoggerFactory.getLogger(SecurityUtil.class);

    private static SecurityService securityService;

    public synchronized static void setSecurityService(SecurityService secService) {
        securityService = secService;
        log.info("{} is injected to SecurityUtil", secService.getClass().getName());
    }

    public static byte[] loadPrivateKeyFromPEMString(String pemKey) throws Exception {
        return securityService.loadPrivateKeyFromPEMString(pemKey);
    }

    public static void clearSensitiveData(byte[] key) {
        securityService.clearSensitiveData(key);
    }

    public static void clearSensitiveData(Key rsaPrivateKey) {
        securityService.clearSensitiveData(rsaPrivateKey);
    }

    public static void clearSensitiveData(KeyPair keyPair) {
        securityService.clearSensitiveData(keyPair);
    }

    public static void clearSensitiveData(Signature signatureFactory) {
        securityService.clearSensitiveData(signatureFactory);
    }

    public static void clearSensitiveData(KeyPairGenerator keyGen) {
        securityService.clearSensitiveData(keyGen);
    }

    public static void clearSensitiveData(SecureRandom random) {
        securityService.clearSensitiveData(random);
    }

    public static void initSecurityProvider() {
        securityService.initSecurityProvider();
    }

    public static String[] getCipherSuite() {
        return securityService.getCipherSuite();
    }
}
